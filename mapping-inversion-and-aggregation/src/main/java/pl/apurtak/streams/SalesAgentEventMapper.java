package pl.apurtak.streams;

import org.apache.kafka.streams.kstream.ValueMapperWithKey;

public class SalesAgentEventMapper implements ValueMapperWithKey<String, SalesRegionChangesGrouped, SalesAgentEvent> {
  @Override
  public SalesAgentEvent apply(String agentId, SalesRegionChangesGrouped agentRegions) {
    // TODO implement mapper
    return null;
  }
}
