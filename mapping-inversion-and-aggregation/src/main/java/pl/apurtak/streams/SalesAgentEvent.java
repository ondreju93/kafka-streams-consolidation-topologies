package pl.apurtak.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Produced;

public class SalesAgentEvent extends Event<SalesAgent> {

  static final Produced<String, SalesAgentEvent> produced =
      Produced.with(Serdes.String(), jsonSerde());

  public SalesAgentEvent(String name, SalesAgent body) {
    super(name, body);
  }

  static Serde<SalesAgentEvent> jsonSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(), new JsonTypeDeserializer<>(SalesAgentEvent.class));
  }
}
