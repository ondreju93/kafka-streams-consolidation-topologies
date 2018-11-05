package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;
import org.slf4j.Logger;

public enum Operation {
  CREATE("CREATE"),
  UPDATE("UPDATE"),
  DELETE("DELETE");

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(Operation.class);
  private final String value;

  Operation(String value) {
    this.value = value;
  }

  @JsonCreator
  static Operation fromString(String value) {
    return Stream.of(values())
        .filter(operation -> value.equalsIgnoreCase(operation.value))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }
}
