package pl.apurtak.streams;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

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
