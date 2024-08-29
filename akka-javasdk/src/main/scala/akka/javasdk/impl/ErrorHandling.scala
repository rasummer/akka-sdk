/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.impl

import akka.annotation.InternalApi
import org.slf4j.MDC

import java.util.UUID

/**
 * INTERNAL API
 */
@InternalApi
private[javasdk] object ErrorHandling {

  case class BadRequestException(msg: String) extends RuntimeException(msg)

  val CorrelationIdMdcKey = "correlationID"

  def withCorrelationId[T](block: String => T): T = {
    val correlationId = UUID.randomUUID().toString
    MDC.put(CorrelationIdMdcKey, correlationId)
    try {
      block(correlationId)
    } finally {
      MDC.remove(CorrelationIdMdcKey)
    }
  }

}
