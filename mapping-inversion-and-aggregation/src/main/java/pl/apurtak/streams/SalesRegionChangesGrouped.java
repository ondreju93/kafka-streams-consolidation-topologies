package pl.apurtak.streams;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

public class SalesRegionChangesGrouped {
  static final Aggregator<String, Change<SalesRegion>, SalesRegionChangesGrouped> adder =
      (key, value, aggregate) -> {
        aggregate.records.put(value.getPayload().getBefore().getRegionId(), value);
        return aggregate;
      };
  static final Aggregator<String, Change<SalesRegion>, SalesRegionChangesGrouped> subtractor =
      (key, value, aggregate) -> {
        aggregate.records.remove(value.getPayload().getAfter().getRegionId());
        return aggregate;
      };
  private final Map<Integer, Change<SalesRegion>> records = new LinkedHashMap<>();

  public SalesRegionChangesGrouped() {
    // empty
  }

  static final Materialized<String, SalesRegionChangesGrouped, KeyValueStore<Bytes, byte[]>>
      materializedAs(String name) {
    return Materialized.<String, SalesRegionChangesGrouped, KeyValueStore<Bytes, byte[]>>as(name)
        .withKeySerde(Serdes.String())
        .withValueSerde(jsonSerde());
  }

  private static Serde<SalesRegionChangesGrouped> jsonSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(), new JsonTypeDeserializer<>(SalesRegionChangesGrouped.class));
  }

  public Map<Integer, Change<SalesRegion>> getRecords() {
    return records;
  }
}
