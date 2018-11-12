package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload<T> {
  private T before;
  private T after;
  // TODO use enum
  private Operation op;
}
