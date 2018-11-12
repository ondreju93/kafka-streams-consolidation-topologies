package pl.apurtak.streams;

import static java.util.Comparator.comparing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Data
public class SalesRegionChangesGrouped {
  static final Aggregator<String, Change<SalesRegion>, SalesRegionChangesGrouped> adder =
      (key, value, aggregate) -> {
        if (Operation.DELETE == value.getPayload().getOp()) {
          aggregate.records.remove(value.getPayload().getBefore().getRegionId());
        } else {
          aggregate.records.put(value.getPayload().getAfter().getRegionId(), value);
        }
        return aggregate;
      };

  static final Aggregator<String, Change<SalesRegion>, SalesRegionChangesGrouped> subtractor =
      (key, value, aggregate) -> {
        aggregate.records.remove(value.getPayload().getAfter().getRegionId());
        return aggregate;
      };

  private final Map<Integer, Change<SalesRegion>> records = new LinkedHashMap<>();

  static Materialized<String, SalesRegionChangesGrouped, KeyValueStore<Bytes, byte[]>>
      materializedAs(String name) {
    return Materialized.<String, SalesRegionChangesGrouped, KeyValueStore<Bytes, byte[]>>as(name)
        .withKeySerde(Serdes.String())
        .withValueSerde(jsonSerde());
  }

  private static Serde<SalesRegionChangesGrouped> jsonSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(), new JsonTypeDeserializer<>(SalesRegionChangesGrouped.class));
  }

  @JsonIgnore
  Collection<Integer> getRegionIds() {
    return getRecords().keySet();
  }

  @JsonIgnore
  SalesRegion mostImportantRegion() {
    return getRecords()
        .entrySet()
        .stream()
        .map(Entry::getValue)
        .map(Change::getPayload)
        .map(Payload::getAfter)
        .sorted(comparing(SalesRegion::getRegionHierarchyLevel))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
