package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload<T> {
  private T before;
  private T after;
  // TODO use enum
  private String op;

  public Payload() {
  }

  public T getBefore() {
    return this.before;
  }

  public T getAfter() {
    return this.after;
  }

  public String getOp() {
    return this.op;
  }

  public void setBefore(T before) {
    this.before = before;
  }

  public void setAfter(T after) {
    this.after = after;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Payload)) {
      return false;
    }
    final Payload other = (Payload) o;
    if (!other.canEqual((Object) this)) {
      return false;
    }
    final Object this$before = this.getBefore();
    final Object other$before = other.getBefore();
    if (this$before == null ? other$before != null : !this$before.equals(other$before)) {
      return false;
    }
    final Object this$after = this.getAfter();
    final Object other$after = other.getAfter();
    if (this$after == null ? other$after != null : !this$after.equals(other$after)) {
      return false;
    }
    final Object this$op = this.getOp();
    final Object other$op = other.getOp();
    if (this$op == null ? other$op != null : !this$op.equals(other$op)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $before = this.getBefore();
    result = result * PRIME + ($before == null ? 43 : $before.hashCode());
    final Object $after = this.getAfter();
    result = result * PRIME + ($after == null ? 43 : $after.hashCode());
    final Object $op = this.getOp();
    result = result * PRIME + ($op == null ? 43 : $op.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof Payload;
  }

  public String toString() {
    return "Payload(before=" + this.getBefore() + ", after=" + this.getAfter() + ", op=" + this
        .getOp() + ")";
  }
}
