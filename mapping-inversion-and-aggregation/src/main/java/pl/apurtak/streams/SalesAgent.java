package pl.apurtak.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SalesAgent {
  private String id;
  private String emailAddress;
  private String name;
  private List<Integer> regionIds = new ArrayList<>();

  public SalesAgent(String id, String emailAddress, String name,
      List<Integer> regionIds) {
    this.id = id;
    this.emailAddress = emailAddress;
    this.name = name;
    this.regionIds = regionIds;
  }

  public SalesAgent() {
    // empty
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Integer> getRegionIds() {
    return regionIds;
  }

  public void setRegionIds(List<Integer> regionIds) {
    this.regionIds = regionIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SalesAgent that = (SalesAgent) o;

    if (!id.equals(that.id)) {
      return false;
    }
    if (!emailAddress.equals(that.emailAddress)) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }
    return regionIds.equals(that.regionIds);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + emailAddress.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + regionIds.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SalesAgent.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("emailAddress='" + emailAddress + "'")
        .add("name='" + name + "'")
        .add("regionIds=" + regionIds)
        .toString();
  }
}
