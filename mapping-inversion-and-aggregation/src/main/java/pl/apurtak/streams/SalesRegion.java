package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesRegion {

  @JsonProperty("REGION_ID")
  private int regionId;

  @JsonProperty("REGION_NAME")
  private String regionName;

  @JsonProperty("PARENT_REGION_ID")
  private int parentRegionId;

  @JsonProperty("REGION_HIERARCHY_LEVEL")
  private int regionHierarchyLevel;

  @JsonProperty("AGENT_LAST_NAME")
  private String agentLastName;

  @JsonProperty("AGENT_FIRST_NAME")
  private String agentFirstName;

  @JsonProperty("AGENT_EMAIL_ADDRESS")
  private String agentEmailAddress;

  static Consumed<ChangeKey<KeyPayload>, Change<SalesRegion>> consumedWithKey() {
    return Consumed.with(changeKeySerde(), changeSerde());
  }

  static Serialized<String, Change<SalesRegion>> serializedWithStringKey() {
    return Serialized.with(Serdes.String(), changeSerde());
  }

  static Materialized<ChangeKey<KeyPayload>, Change<SalesRegion>, KeyValueStore<Bytes, byte[]>>
      materializedAs(String storeName) {
    return Materialized
        .<ChangeKey<KeyPayload>, Change<SalesRegion>, KeyValueStore<Bytes, byte[]>>as(storeName)
        .withKeySerde(changeKeySerde())
        .withValueSerde(changeSerde());
  }

  private static Serde<Change<SalesRegion>> changeSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(),
        new JsonTypeDeserializer<>(
            TypeFactory.defaultInstance()
                .constructParametricType(Change.class, SalesRegion.class)));
  }

  private static Serde<ChangeKey<KeyPayload>> changeKeySerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(),
        new JsonTypeDeserializer<>(
            TypeFactory.defaultInstance()
                .constructParametricType(ChangeKey.class, KeyPayload.class)));
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  static final class KeyPayload {
    @JsonProperty("REGION_ID")
    private int regionId;
  }
}
