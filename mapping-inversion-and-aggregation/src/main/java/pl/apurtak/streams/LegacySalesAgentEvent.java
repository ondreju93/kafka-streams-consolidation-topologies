package pl.apurtak.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Produced;

public class LegacySalesAgentEvent extends Event<LegacySalesAgent> {

  static final Produced<String, LegacySalesAgentEvent> produced =
      Produced.with(Serdes.String(), jsonSerde());

  public LegacySalesAgentEvent(String name, LegacySalesAgent body) {
    super(name, body);
  }

  static Serde<LegacySalesAgentEvent> jsonSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(), new JsonTypeDeserializer<>(LegacySalesAgentEvent.class));
  }
}
