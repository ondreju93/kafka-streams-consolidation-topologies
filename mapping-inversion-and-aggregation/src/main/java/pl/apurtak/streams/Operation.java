package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum Operation {
  CREATE("c"),
  UPDATE("u"),
  DELETE("d");

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

  @JsonValue
  public String getValue() {
    return value;
  }
}