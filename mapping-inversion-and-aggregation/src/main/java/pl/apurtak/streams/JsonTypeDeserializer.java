package pl.apurtak.streams;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonTypeDeserializer<T> implements Deserializer<T> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JavaType type;

  public JsonTypeDeserializer(JavaType type) {
    this.type = type;
  }

  public JsonTypeDeserializer(Class<T> clazz) {
    this(TypeFactory.defaultInstance().constructType(clazz));
  }

  @Override
  public void configure(Map<String, ?> props, boolean isKey) {
    // intentionally empty
  }

  @Override
  public T deserialize(String topic, byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return null;
    }

    T data;
    try {
      data = objectMapper.readerFor(type).readValue(bytes);
    } catch (Exception e) {
      throw new SerializationException(e);
    }

    return data;
  }

  @Override
  public void close() {
    // intentionally empty
  }
}
