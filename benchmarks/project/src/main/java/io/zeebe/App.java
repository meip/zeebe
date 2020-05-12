/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.Topology;
import io.zeebe.config.AppCfg;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class App implements Runnable {

  protected static final Logger LOG = LoggerFactory.getLogger(App.class);

  static void createApp(Function<AppCfg, Runnable> appFactory) {
    final Config config = ConfigFactory.load().getConfig("app");
    LOG.info("Starting app with config: {}", config.root().render());
    final AppCfg appCfg = ConfigBeanFactory.create(config, AppCfg.class);
    appFactory.apply(appCfg).run();
  }

  protected void printTopology(ZeebeClient client) {
    while (true) {
      try {
        final Topology topology = client.newTopologyRequest().send().join();
        topology
            .getBrokers()
            .forEach(
                b -> {
                  LOG.info("Broker {} - {}", b.getNodeId(), b.getAddress());
                  b.getPartitions()
                      .forEach(p -> LOG.info("{} - {}", p.getPartitionId(), p.getRole()));
                });
        break;
      } catch (Exception e) {
        // retry
      }
    }
  }
}