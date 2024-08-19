/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.action;

import akka.platform.javasdk.annotations.Consume;
import akka.platform.javasdk.consumer.Consumer;
import akka.platform.javasdk.eventsourcedentity.TestESEvent;
import akka.platform.javasdk.eventsourcedentity.TestEventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import akka.platform.javasdk.annotations.ComponentId;

@ComponentId("tracing-action")
public class TestTracing extends Consumer {

  Logger logger = LoggerFactory.getLogger(TestTracing.class);

  @Consume.FromEventSourcedEntity(value = TestEventSourcedEntity.class, ignoreUnknown = true)
  public Effect<String> consume(TestESEvent.Event2 event) {
    logger.info("registering a logging event");
    return effects().reply(
        messageContext().metadata().traceContext().traceParent().orElse("not-found"));
  }
}
