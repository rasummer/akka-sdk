/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.client;

import akka.annotation.DoNotInherit;
import akka.platform.javasdk.DeferredCall;
import akka.platform.javasdk.Metadata;

import java.util.concurrent.CompletionStage;

/**
 * One argument component call representation, not executed until invoked or by some mechanism using
 * the deferred call (like a timer executing it later for example)
 *
 * @param <A1> the argument type of the call
 * @param <R> The type of value returned by executing the call
 *     <p>Not for user extension or instantiation, returned by the SDK component client
 */
@DoNotInherit
public interface ComponentMethodRef1<A1, R> {
  ComponentMethodRef1<A1, R> withMetadata(Metadata metadata);

  DeferredCall<A1, R> deferred(A1 arg);

  CompletionStage<R> invokeAsync(A1 arg);
}
