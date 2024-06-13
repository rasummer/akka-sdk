/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package kalix.javasdk.impl.effect

import com.google.protobuf.any.{ Any => ScalaPbAny }
import com.google.protobuf.{ Any => JavaPbAny }
import kalix.javasdk.impl.MetadataImpl
import kalix.protocol.component

object EffectSupport {

  def asProtocol(messageReply: MessageReplyImpl[JavaPbAny]): component.Reply =
    component.Reply(
      Some(ScalaPbAny.fromJavaProto(messageReply.message)),
      MetadataImpl.toProtocol(messageReply.metadata))

}
