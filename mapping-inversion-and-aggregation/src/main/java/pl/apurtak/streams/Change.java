package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Change<T> {
  private Payload<T> payload;

  public Change() {
  }

  public Payload<T> getPayload() {
    return this.payload;
  }

  public void setPayload(Payload<T> payload) {
    this.payload = payload;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Change)) {
      return false;
    }
    final Change other = (Change) o;
    if (!other.canEqual((Object) this)) {
      return false;
    }
    final Object this$payload = this.getPayload();
    final Object other$payload = other.getPayload();
    if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $payload = this.getPayload();
    result = result * PRIME + ($payload == null ? 43 : $payload.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof Change;
  }

  public String toString() {
    return "Change(payload=" + this.getPayload() + ")";
  }
}
