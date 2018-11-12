package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Change<T> {
  private Payload<T> payload;
}
