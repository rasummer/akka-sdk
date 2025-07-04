/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.impl

import akka.annotation.InternalApi
import akka.javasdk.Context

/**
 * INTERNAL API
 */
@InternalApi
private[akka] trait InternalContext {

  /**
   * Intended to be used by component calls, initially to give to the called component access to the trace parent from
   * the caller. It's empty by default because only actions and workflows can to call other components. Of the two, only
   * actions have traces and can pass them around using `protected final Component components()`.
   */
  def componentCallMetadata: MetadataImpl = MetadataImpl.Empty
}

/**
 * INTERNAL API
 */
@InternalApi
abstract class AbstractContext extends Context with InternalContext {}
