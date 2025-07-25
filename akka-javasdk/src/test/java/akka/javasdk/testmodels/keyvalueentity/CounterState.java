/*
 * Copyright (C) 2021-2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.testmodels.keyvalueentity;

import akka.javasdk.annotations.Migration;

@Migration(CounterStateMigration.class)
public class CounterState {

  public final String id;
  public final int value;

  public CounterState(String id, int value) {
    this.id = id;
    this.value = value;
  }

  public CounterState increase(int increaseBy) {
    return new CounterState(id, value + increaseBy);
  }
}
