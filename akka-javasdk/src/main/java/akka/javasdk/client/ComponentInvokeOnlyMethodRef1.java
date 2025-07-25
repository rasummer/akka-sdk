/*
 * Copyright (C) 2021-2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.client;

import akka.annotation.DoNotInherit;
import akka.javasdk.Metadata;
import java.util.concurrent.CompletionStage;

/**
 * One argument component call representation, not executed until invoked. Used for component
 * methods that cannot be deferred.
 *
 * <p>Not for user extension or instantiation, returned by the SDK component client
 *
 * @param <R> The type of value returned by executing the call
 * @param <A1> the argument type of the call
 */
@DoNotInherit
public interface ComponentInvokeOnlyMethodRef1<A1, R> {
  ComponentMethodRef1<A1, R> withMetadata(Metadata metadata);

  CompletionStage<R> invokeAsync(A1 arg);

  R invoke(A1 arg);
}
