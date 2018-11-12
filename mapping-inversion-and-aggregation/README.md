# SalesRegion - Agent Mapping inversion and aggregation
## Problem description
Kafka-streams application for a continuous transformation of changes applied to a table called 
`SALES_REGION` defined by the following ddl statements:
```sql
create table SALES_REGION
(
	REGION_ID bigint unsigned auto_increment,
	REGION_NAME varchar(40) not null,
	PARENT_REGION_ID int null,
	REGION_HIERARCHY_LEVEL int not null,
	AGENT_LAST_NAME varchar(40) not null,
	AGENT_FIRST_NAME varchar(40) not null,
	AGENT_EMAIL_ADRESS varchar(40) not null,
	constraint REGION_ID
		unique (REGION_ID)
)
;

alter table SALES_REGION
	add primary key (REGION_ID)
;
```
to a stream of document-styled events of form:
```json
{
  "name": "LegacySalesAgentCreated|LegacySalesAgentUpdated|LegacySalesAgentDeleted",
  "body": {
    "id": "agent id",
    "emailAddress": "agent email address",
    "firstName": "agent first name",
    "lastName": "agent last name",
    "regionIds": [
      4,
      5
    ]
  }
}
```
The source table contains basic information about sales regions (region id is it's primary key).
Among basic region features like name, parent or hierarchy level, `SALES_REGION` table is the only 
source of information about mappings between sales agents and regions.
Sales agents data is present in the table only as a series of mentions and is strongly denormalized.
To implement a streaming transformation, producing events that carry basic information about agents 
and contain list of assigned regions for each agent, the following challenges need to be accomplished:
- One of agent properties needs to be selected as a primary key for agent entity
- Input records have to be aggregated (stateful transformation is necessary)
- Strategy for choosing more important input record is needed in case of having multiple records 
with same primary key, differing by some other fields values (like first name or last name). 

## Kafka-streams implementation
Stream processing topology for solving problems defined above might be visualised with drawing:

And implemented like this:
```java
  private static Topology createTopology() {
    final StreamsBuilder builder = new StreamsBuilder();
    final HashingService hashingService = new HashingServiceImpl();


    // Step 1 - load SalesRegion changes as a table
    KTable<ChangeKey<KeyPayload>, Change<SalesRegion>> legacyRegions =
        builder.table(
            SALES_REGION_TOPIC,
            SalesRegion.consumedWithKey(),
            SalesRegion.materializedAs(LEGACY_REGIONS_STORE));
    
    // Step 2 - continuously group region changes by property chosen to become agent primary key
    KTable<String, SalesRegionChangesGrouped> agentsRegions =
        legacyRegions
            .groupBy(
                (key, value) -> new KeyValue<>(hashingService.hash(getAgentEmail(value)), value),
                SalesRegion.serializedWithStringKey())
            .aggregate(
                SalesRegionChangesGrouped::new,
                SalesRegionChangesGrouped.adder,
                SalesRegionChangesGrouped.subtractor,
                SalesRegionChangesGrouped.materializedAs(AGENTS_REGIONS_STORE));


    builder.addStateStore(LegacySalesAgentEventTransformer.stateStoreBuilder);
    // Step 3 - transform grouped changes of regions belonging to same agent, to event form
    agentsRegions
        .toStream()
        .transform(
            LegacySalesAgentEventTransformer::new,
            LegacySalesAgentEventTransformer.LEGACY_SALES_AGENTS_STORE)
        .to(LEGACY_AGENTS_TOPIC, LegacySalesAgentEvent.produced);

    return builder.build();
  }
```
The first step of processing is loading records as a table from SALES_REGION topic.
This topic contains the replication stream of changes on database table content, captured by debezium connector.  

Second step is grouping regions table by a field that identifies agent object and aggregating them.
As it have been already mentioned, agent data stored in SALES_REGION table has no primary key.
In this example email address property has been chosen as the identifier for agent entity.
Legacy agent events published as the output of this transformation should be ready for being
consumed by any micro-service that uses same kafka cluster. 
If it's not desired to expose logic of the legacy agent normalization process for other modules,
it might be a good idea to calculate some hash function of the property elected to become primary key,
as shown on the first snippet.

Last step of this topology is transforming group of regions belonging to same agent to an object of type `Event<LegacySalesAgent>`,
where `Event` and `LegacySalesAgent` are data classes defined below:
```java
@Data
@AllArgsConstructor
public abstract class Event<T> {
  private String name;
  private T body;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LegacySalesAgent {
  private String id;
  private String emailAddress;
  private String firstName;
  private String lastName;
  private List<Integer> regionIds;

  static Serde<LegacySalesAgent> jsonSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(), new JsonTypeDeserializer<>(LegacySalesAgent.class));
  }
}
```
This transformation is implemented using stateful transformer of class `LegacySalesAgentEventTransformer`
```java
public class LegacySalesAgentEventTransformer
    implements Transformer<
        String, SalesRegionChangesGrouped, KeyValue<String, LegacySalesAgentEvent>> {
  static final String LEGACY_SALES_AGENTS_STORE = "LegacySalesAgents";
  static final StoreBuilder<KeyValueStore<String, LegacySalesAgent>> stateStoreBuilder =
      Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(LEGACY_SALES_AGENTS_STORE),
          Serdes.String(),
          LegacySalesAgent.jsonSerde());
  private static final Logger log = LoggerFactory.getLogger(LegacySalesAgentEventTransformer.class);
  private KeyValueStore<String, LegacySalesAgent> legacySalesAgentsStore = null;

  @Override
  public void init(ProcessorContext context) {
    this.legacySalesAgentsStore =
        (KeyValueStore<String, LegacySalesAgent>) context.getStateStore(LEGACY_SALES_AGENTS_STORE);
  }

  @Override
  public KeyValue<String, LegacySalesAgentEvent> transform(
      String agentId, SalesRegionChangesGrouped groupedRegionChanges) {
    log.info(
        "Transforming grouped region changes into an LegacySalesAgent event: [agentId={}, regionChanges={}]",
        agentId,
        groupedRegionChanges);

    if (newAgentShouldBeCreated(agentId, groupedRegionChanges)) {
      log.info("Sending LegacySalesAgentCreated event: [agentId={}]", agentId);
      LegacySalesAgent salesAgent = legacySalesAgent(agentId, groupedRegionChanges);
      legacySalesAgentsStore.put(agentId, salesAgent);
      return new KeyValue<>(
          agentId, new LegacySalesAgentEvent("LegacySalesAgentCreated", salesAgent));
    } else if (existingAgentShouldBeUpdated(agentId, groupedRegionChanges)) {
      log.info("Sending LegacySalesAgentUpdated event: [agentId={}]", agentId);
      LegacySalesAgent salesAgent = legacySalesAgent(agentId, groupedRegionChanges);
      legacySalesAgentsStore.put(agentId, salesAgent);
      return new KeyValue<>(
          agentId, new LegacySalesAgentEvent("LegacySalesAgentUpdated", salesAgent));
    } else if (existingAgentShouldBeDeleted(agentId, groupedRegionChanges)) {
      log.info("Sending LegacySalesAgentDeleted event: [agentId={}]", agentId);
      legacySalesAgentsStore.delete(agentId);
      return new KeyValue<>(
          agentId,
          new LegacySalesAgentEvent(
              "LegacySalesAgentDeleted", LegacySalesAgent.builder().id(agentId).build()));
    } else {
      log.info("No need to send LegacySalesAgent event: [agentId={}]", agentId);
      return null;
    }
  }

  private boolean existingAgentShouldBeDeleted(
      String agentId, SalesRegionChangesGrouped groupedRegionChanges) {
    return legacySalesAgentsStore.get(agentId) != null
        && groupedRegionChanges.getRecords().isEmpty();
  }

  private boolean existingAgentShouldBeUpdated(
      String agentId, SalesRegionChangesGrouped groupedRegionChanges) {
    LegacySalesAgent currentAgentState = legacySalesAgentsStore.get(agentId);
    return currentAgentState != null
        && !groupedRegionChanges.getRecords().isEmpty()
        && !Objects.equals(currentAgentState, legacySalesAgent(agentId, groupedRegionChanges));
  }

  private boolean newAgentShouldBeCreated(
      String agentId, SalesRegionChangesGrouped groupedRegionChanges) {
    return legacySalesAgentsStore.get(agentId) == null
        && !groupedRegionChanges.getRecords().isEmpty();
  }

  private LegacySalesAgent legacySalesAgent(
      String agentId, SalesRegionChangesGrouped agentRegionsChanges) {
    SalesRegion mostImportantRegion = agentRegionsChanges.mostImportantRegion();
    return LegacySalesAgent.builder()
        .id(agentId)
        .emailAddress(mostImportantRegion.getAgentEmailAddress())
        .firstName(mostImportantRegion.getAgentFirstName())
        .lastName(mostImportantRegion.getAgentLastName())
        .regionIds(new ArrayList<>(agentRegionsChanges.getRegionIds()))
        .build();
  }

  @Override
  public void close() {
    // not needed
  }
}
```
This code is responsible for three tasks:
- determining type of event that should be send
- converting raw groups of region changes (objects of type `SalesRegionChangesGrouped`) to events
- filtering out all non-meaningful changes
Determining event type is done with methods:
`newAgentShouldBeCreated`, `existingAgentShouldBeUpdated` and `existingAgentShouldBeDeleted`.  
Because single output event consists of data from multiple input records,
event type cannot be simply deduced from source database operation.
In presented example, following semantics of Create, Update and Delete operations for Sales Agent objects has been adopted:
- agent should get created on processing of first region change that mentions him
- agent should get updated on processing any change captured from source database that modifies it's attributes
- agent should get deleted on processing the change that makes its regionIds array empty
The flow looks very similar in all cases - check what is the current state of LegacySalesAgent object,
update state accordingly to currently processed payload and create an output event.

## Demo
Open terminal, clone github repository and enter project directory. 
Setup local dockerized environment with `docker-compose up -d`
This project uses debezium docker images for kafka, zookeeper, kafka-connect and mysql.
Now you can connect to the dockerized mysql database (use 'password' string when asked for password)
```bash
docker-compose exec mysql mysql -u root -p
```
and execute query to view initially loaded data:
```sql
SELECT * FROM SALES_REGION
```
```
+-----------+-------------+------------------+------------------------+-----------------+------------------+-------------------------+
| REGION_ID | REGION_NAME | PARENT_REGION_ID | REGION_HIERARCHY_LEVEL | AGENT_LAST_NAME | AGENT_FIRST_NAME | AGENT_EMAIL_ADDRESS     |
+-----------+-------------+------------------+------------------------+-----------------+------------------+-------------------------+
|         1 | Trondheim   |               11 |                      2 | Bergman         | John             | jbergman@supersales.eu  |
|         2 | Oslo        |               11 |                      2 | Bergman         | John A           | jbergman@supersales.eu  |
|         3 | Munich      |               22 |                      2 | Heideger        | Martin           | mheideger@supersales.eu |
|         4 | Harburg     |               22 |                      2 | Mueller         | Joseph           | jmueller@supersales.eu  |
|         5 | Berlin      |               22 |                      2 | Mueller         | Joseph           | jmueller@supersales.eu  |
|         6 | Warszawa    |               33 |                      2 | Przybysz        | Karol            | kprzybysz@supersales.eu |
|         7 | Poznan      |               33 |                      2 | Nowak           | Jan              | jnowak@supersales.eu    |
|        11 | Norway      |               -1 |                      1 | Bergman         | John A           | jbergman@supersales.eu  |
|        22 | Germany     |               -1 |                      1 | Neuer           | Franz            | fneuer@supersales.eu    |
|        33 | Poland      |               -1 |                      1 | Nowak           | Jan              | jnowak@supersales.eu    |
+-----------+-------------+------------------+------------------------+-----------------+------------------+-------------------------+
```

To load debezium mysql connector, use curl:
```bash
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @debezium-config.json 
```

Build application with `mvn clean package` and run it with ` java -jar target/mapping-inversion-and-aggregation-0.1.jar `

After application is started, consume messages from `LegacyAgents` topic to see how has been the initial content of SALES_REGION table transformed to output events:
```bash
docker-compose exec kafka /kafka/bin/kafka-console-consumer.sh  --bootstrap-server kafka:9092 --topic LegacyAgents --from-beginning | jq
```
```json
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "11f1e30768f9fbd496c78495d4f10c4b9d1670915066b17817f700ece58e5031",
    "emailAddress": "mheideger@supersales.eu",
    "firstName": "Heideger",
    "lastName": "Martin",
    "regionIds": [
      3
    ]
  }
}
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "5bf319e352456aeb2c7941c7adfca07d26b43e1d87824d83a809b7eed8fcfd56",
    "emailAddress": "jmueller@supersales.eu",
    "firstName": "Mueller",
    "lastName": "Joseph",
    "regionIds": [
      4,
      5
    ]
  }
}
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "b46f4550cffbc776b8e63b1e5c231e01d24ce95b0bc10e8b06ebeada935cd91a",
    "emailAddress": "kprzybysz@supersales.eu",
    "firstName": "Przybysz",
    "lastName": "Karol",
    "regionIds": [
      6
    ]
  }
}
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "13deac2c2ed441e3529c8ddec5c79b6bcfc897e3dfd34909d35d5b8662d2615e",
    "emailAddress": "jbergman@supersales.eu",
    "firstName": "Bergman",
    "lastName": "John Arne",
    "regionIds": [
      1,
      2,
      11
    ]
  }
}
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "19d1a5a210d69700ebadc9680e1e0afad011e74ba6900ee3162a5d768d1ab7d4",
    "emailAddress": "fneuer@supersales.eu",
    "firstName": "Neuer",
    "lastName": "Franz",
    "regionIds": [
      22
    ]
  }
}
{
  "name": "LegacySalesAgentCreated",
  "body": {
    "id": "a4521d9dae6536fa0d4fbb32f7d1a8e932d807b46de6cf11cc179af4d2b5f09a",
    "emailAddress": "jnowak@supersales.eu",
    "firstName": "Nowak",
    "lastName": "Jan",
    "regionIds": [
      7,
      33
    ]
  }
}

```
Now execute the following statement to see what happens when region's assigned agent gets changed:
```mysql
UPDATE SALES_REGION set AGENT_EMAIL_ADDRESS = 'kprzybysz@supersales.eu', AGENT_FIRST_NAME = 'Karol', AGENT_LAST_NAME = 'Przybysz' WHERE REGION_ID = 33;
```
This statement makes region with id=33 (Poland), gets assigned to agent with email: 'kprzybysz@supersales.eu' and belongs no more to regions of agent with email 'jnowak@supersales.eu'.
As an effect, two new events should appear in LegacyAgents topic
```json
{
  "name": "LegacySalesAgentUpdated",
  "body": {
    "id": "a4521d9dae6536fa0d4fbb32f7d1a8e932d807b46de6cf11cc179af4d2b5f09a",
    "emailAddress": "jnowak@supersales.eu",
    "firstName": "Nowak",
    "lastName": "Jan",
    "regionIds": [
      7
    ]
  }
}
{
  "name": "LegacySalesAgentUpdated",
  "body": {
    "id": "b46f4550cffbc776b8e63b1e5c231e01d24ce95b0bc10e8b06ebeada935cd91a",
    "emailAddress": "kprzybysz@supersales.eu",
    "firstName": "Karol",
    "lastName": "Przybysz",
    "regionIds": [
      6,
      33
    ]
  }
}
```
Delete event may get triggered for an agent either when all its regions get assigned to other agents
or when all its regions' records get deleted from the table.
Let's try with the latter option:
```mysql
DELETE FROM SALES_REGION WHERE REGION_ID = 7;
```
The following deletion event should be produced to LegacyAgents topic
```json
{
  "name": "LegacySalesAgentDeleted",
  "body": {
    "id": "a4521d9dae6536fa0d4fbb32f7d1a8e932d807b46de6cf11cc179af4d2b5f09a"
  }
}
```