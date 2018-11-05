package pl.apurtak.streams;

import java.util.StringJoiner;

public abstract class Event<T> {
  private String name;
  private T body;

  public Event(String name, T body) {
    this.name = name;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Event<?> event = (Event<?>) o;

    if (!name.equals(event.name)) {
      return false;
    }
    return body != null ? body.equals(event.body) : event.body == null;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (body != null ? body.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("body=" + body)
        .toString();
  }
}
