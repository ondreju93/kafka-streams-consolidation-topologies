package pl.apurtak.streams;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Event<T> {
  private String name;
  private T body;
}
