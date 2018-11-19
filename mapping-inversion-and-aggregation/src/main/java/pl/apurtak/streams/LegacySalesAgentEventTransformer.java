package pl.apurtak.streams;

import java.util.ArrayList;
import java.util.Objects;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class LegacySalesAgentEventTransformer
    implements Transformer<
        String, SalesRegionChangesGrouped, KeyValue<String, LegacySalesAgentEvent>> {
  static final String LEGACY_SALES_AGENTS_STORE = "LegacySalesAgents";
  static final StoreBuilder<KeyValueStore<String, LegacySalesAgent>> stateStoreBuilder =
      Stores.keyValueStoreBuilder(
          Stores.persistentKeyValueStore(LEGACY_SALES_AGENTS_STORE),
          Serdes.String(),
          LegacySalesAgent.jsonSerde());
  private KeyValueStore<String, LegacySalesAgent> legacySalesAgentsStore = null;

  @Override
  public void init(ProcessorContext context) {
    this.legacySalesAgentsStore =
        (KeyValueStore<String, LegacySalesAgent>) context.getStateStore(LEGACY_SALES_AGENTS_STORE);
  }

  @Override
  public KeyValue<String, LegacySalesAgentEvent> transform(
      String agentId, SalesRegionChangesGrouped groupedRegionChanges) {
    if (newAgentShouldBeCreated(agentId, groupedRegionChanges)) {
      LegacySalesAgent salesAgent = legacySalesAgent(agentId, groupedRegionChanges);
      legacySalesAgentsStore.put(agentId, salesAgent);
      return new KeyValue<>(
          agentId, new LegacySalesAgentEvent("LegacySalesAgentCreated", salesAgent));
    } else if (existingAgentShouldBeUpdated(agentId, groupedRegionChanges)) {
      LegacySalesAgent salesAgent = legacySalesAgent(agentId, groupedRegionChanges);
      legacySalesAgentsStore.put(agentId, salesAgent);
      return new KeyValue<>(
          agentId, new LegacySalesAgentEvent("LegacySalesAgentUpdated", salesAgent));
    } else if (existingAgentShouldBeDeleted(agentId, groupedRegionChanges)) {
      legacySalesAgentsStore.delete(agentId);
      return new KeyValue<>(
          agentId,
          new LegacySalesAgentEvent(
              "LegacySalesAgentDeleted", LegacySalesAgent.builder().id(agentId).build()));
    } else {
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
