/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.consumer;

import akka.platform.javasdk.impl.ComponentOptions;
import akka.platform.javasdk.impl.consumer.ConsumerOptionsImpl;

/** Options for Consumers */
public interface ConsumerOptions extends ComponentOptions {

  /** Create default options for a Consumer. */
  static ConsumerOptions defaults() {
    return new ConsumerOptionsImpl();
  }
}
