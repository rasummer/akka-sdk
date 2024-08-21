/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.platform.javasdk.client;

import akka.annotation.DoNotInherit;

import java.util.concurrent.CompletionStage;
import akka.platform.javasdk.Metadata;

/**
 * One argument component call representation, not executed until invoked. Used for component
 * methods that cannot be deferred.
 *
 * @param <R> The type of value returned by executing the call
 * @param <A1> the argument type of the call
 *     <p>Not for user extension or instantiation, returned by the SDK component client
 */
@DoNotInherit
public interface ComponentInvokeOnlyMethodRef1<A1, R> {
  ComponentMethodRef1<A1, R> withMetadata(Metadata metadata);

  CompletionStage<R> invokeAsync(A1 arg);
}
