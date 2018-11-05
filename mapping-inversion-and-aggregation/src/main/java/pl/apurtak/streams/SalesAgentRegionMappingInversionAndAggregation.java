/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.apurtak.streams;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.apurtak.streams.SalesRegion.KeyPayload;

public class SalesAgentRegionMappingInversionAndAggregation {
  private static final Logger log =
      LoggerFactory.getLogger(SalesAgentRegionMappingInversionAndAggregation.class);

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "SalesAgentRegionMapping");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    final Topology topology = createTopology();
    final KafkaStreams streams = new KafkaStreams(topology, props);
    final CountDownLatch latch = new CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread("streams-shutdown-hook") {
              @Override
              public void run() {
                streams.close();
                latch.countDown();
              }
            });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

  private static Topology createTopology() {
    final StreamsBuilder builder = new StreamsBuilder();
    final HashingService hashingService = new HashingServiceImpl();

    KTable<ChangeKey<KeyPayload>, Change<SalesRegion>> legacyRegions =
        builder.table(
            "dbserver1.legacy_sales.SALES_REGION",
            SalesRegion.consumedWithKey(),
            SalesRegion.materializedAs("LegacyRegions"));

    //
    //    legacyRegions.toStream()
    //        .peek((key, value) -> log.info("Processing record: [key={}, value={}]", key, value));

    KTable<String, SalesRegionChangesGrouped> agentsRegions = legacyRegions
        .groupBy(
            (key, value) ->
                new KeyValue<>(hashingService.hash(getAgentEmail(value)), value),
            SalesRegion.serializedWithStringKey())
        .aggregate(
            SalesRegionChangesGrouped::new,
            SalesRegionChangesGrouped.adder,
            SalesRegionChangesGrouped.subtractor,
            SalesRegionChangesGrouped.materializedAs("AgentsRegions"));

    agentsRegions.toStream()
        .mapValues(new SalesAgentEventMapper())
        .to("LegacyAgents", SalesAgentEvent.produced);

    return builder.build();
  }

  private static String getAgentEmail(Change<SalesRegion> value) {
    return value.getPayload().getAfter().getAgentEmailAddress();
  }
}