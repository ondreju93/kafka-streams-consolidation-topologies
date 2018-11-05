package pl.apurtak.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesRegion {

  @JsonProperty("REGION_ID")
  private int regionId;

  @JsonProperty("REGION_NAME")
  private String regionName;

  @JsonProperty("PARENT_REGION_ID")
  private int parentRegionId;

  @JsonProperty("REGION_HIERARCHY_LEVEL")
  private int regionHierarchyLevel;

  @JsonProperty("AGENT_LAST_NAME")
  private String agentLastName;

  @JsonProperty("AGENT_FIRST_NAME")
  private String agentFirstName;

  @JsonProperty("AGENT_EMAIL_ADDRESS")
  private String agentEmailAddress;

  public SalesRegion() {
  }

  static Consumed<ChangeKey<KeyPayload>, Change<SalesRegion>> consumedWithKey() {
    return Consumed.with(changeKeySerde(), changeSerde());
  }

  static Serialized<String, Change<SalesRegion>> serializedWithStringKey() {
    return Serialized.with(Serdes.String(), changeSerde());
  }

  static Materialized<ChangeKey<KeyPayload>, Change<SalesRegion>, KeyValueStore<Bytes, byte[]>>
      materializedAs(String storeName) {
    return Materialized
        .<ChangeKey<KeyPayload>, Change<SalesRegion>, KeyValueStore<Bytes, byte[]>>as(storeName)
        .withKeySerde(changeKeySerde())
        .withValueSerde(changeSerde());
  }

  private static Serde<Change<SalesRegion>> changeSerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(),
        new JsonTypeDeserializer<>(
            TypeFactory.defaultInstance()
                .constructParametricType(Change.class, SalesRegion.class)));
  }

  private static Serde<ChangeKey<KeyPayload>> changeKeySerde() {
    return Serdes.serdeFrom(
        new JsonSerializer<>(),
        new JsonTypeDeserializer<>(
            TypeFactory.defaultInstance()
                .constructParametricType(ChangeKey.class, KeyPayload.class)));
  }

  public int getRegionId() {
    return this.regionId;
  }

  public String getRegionName() {
    return this.regionName;
  }

  public int getParentRegionId() {
    return this.parentRegionId;
  }

  public int getRegionHierarchyLevel() {
    return this.regionHierarchyLevel;
  }

  public String getAgentLastName() {
    return this.agentLastName;
  }

  public String getAgentFirstName() {
    return this.agentFirstName;
  }

  public String getAgentEmailAddress() {
    return this.agentEmailAddress;
  }

  public void setRegionId(int regionId) {
    this.regionId = regionId;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public void setParentRegionId(int parentRegionId) {
    this.parentRegionId = parentRegionId;
  }

  public void setRegionHierarchyLevel(int regionHierarchyLevel) {
    this.regionHierarchyLevel = regionHierarchyLevel;
  }

  public void setAgentLastName(String agentLastName) {
    this.agentLastName = agentLastName;
  }

  public void setAgentFirstName(String agentFirstName) {
    this.agentFirstName = agentFirstName;
  }

  public void setAgentEmailAddress(String agentEmailAddress) {
    this.agentEmailAddress = agentEmailAddress;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SalesRegion)) {
      return false;
    }
    final SalesRegion other = (SalesRegion) o;
    if (!other.canEqual((Object) this)) {
      return false;
    }
    if (this.getRegionId() != other.getRegionId()) {
      return false;
    }
    final Object this$regionName = this.getRegionName();
    final Object other$regionName = other.getRegionName();
    if (this$regionName == null ? other$regionName != null
        : !this$regionName.equals(other$regionName)) {
      return false;
    }
    if (this.getParentRegionId() != other.getParentRegionId()) {
      return false;
    }
    if (this.getRegionHierarchyLevel() != other.getRegionHierarchyLevel()) {
      return false;
    }
    final Object this$agentLastName = this.getAgentLastName();
    final Object other$agentLastName = other.getAgentLastName();
    if (this$agentLastName == null ? other$agentLastName != null
        : !this$agentLastName.equals(other$agentLastName)) {
      return false;
    }
    final Object this$agentFirstName = this.getAgentFirstName();
    final Object other$agentFirstName = other.getAgentFirstName();
    if (this$agentFirstName == null ? other$agentFirstName != null
        : !this$agentFirstName.equals(other$agentFirstName)) {
      return false;
    }
    final Object this$agentEmailAddress = this.getAgentEmailAddress();
    final Object other$agentEmailAddress = other.getAgentEmailAddress();
    if (this$agentEmailAddress == null ? other$agentEmailAddress != null
        : !this$agentEmailAddress.equals(other$agentEmailAddress)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getRegionId();
    final Object $regionName = this.getRegionName();
    result = result * PRIME + ($regionName == null ? 43 : $regionName.hashCode());
    result = result * PRIME + this.getParentRegionId();
    result = result * PRIME + this.getRegionHierarchyLevel();
    final Object $agentLastName = this.getAgentLastName();
    result = result * PRIME + ($agentLastName == null ? 43 : $agentLastName.hashCode());
    final Object $agentFirstName = this.getAgentFirstName();
    result = result * PRIME + ($agentFirstName == null ? 43 : $agentFirstName.hashCode());
    final Object $agentEmailAddress = this.getAgentEmailAddress();
    result = result * PRIME + ($agentEmailAddress == null ? 43 : $agentEmailAddress.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof SalesRegion;
  }

  public String toString() {
    return "SalesRegion(regionId=" + this.getRegionId() + ", regionName=" + this.getRegionName()
        + ", parentRegionId=" + this.getParentRegionId() + ", regionHierarchyLevel=" + this
        .getRegionHierarchyLevel() + ", agentLastName=" + this.getAgentLastName()
        + ", agentFirstName=" + this.getAgentFirstName() + ", agentEmailAddress=" + this
        .getAgentEmailAddress() + ")";
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static final class KeyPayload {
    @JsonProperty("REGION_ID")
    private int regionId;

    public KeyPayload() {
    }

    public int getRegionId() {
      return this.regionId;
    }

    public void setRegionId(int regionId) {
      this.regionId = regionId;
    }

    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof KeyPayload)) {
        return false;
      }
      final KeyPayload other = (KeyPayload) o;
      if (this.getRegionId() != other.getRegionId()) {
        return false;
      }
      return true;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = result * PRIME + this.getRegionId();
      return result;
    }

    public String toString() {
      return "SalesRegion.KeyPayload(regionId=" + this.getRegionId() + ")";
    }
  }
}
