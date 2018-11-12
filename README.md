# kafka-streams-consolidation-topologies
Project collects topologies for solving sample continuous data consolidation problems.

All topologies work on a custom test mysql database called `legacy_sales` that represent ugly and denormalized legacy system database

Available topologies:
 - mapping-inversion-and-aggregation - continously reverse and aggregate flat records carrying information about mapping between sales regions and sales agents
