package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerializer<T> implements Serializer<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    // empty
  }

  @Override
  public byte[] serialize(String topic, T data) {
    if (data == null) {
      return new byte[0];
    } else {
      try {
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception e) {
        throw new SerializationException("Error serializing JSON message", e);
      }
    }
  }

  @Override
  public void close() {
    // empty
  }
}
