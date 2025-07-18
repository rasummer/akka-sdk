/*
 * Copyright (C) 2021-2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akkajavasdk.components.workflowentities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Deposit {
  public final String to;
  public final int amount;

  @JsonCreator
  public Deposit(@JsonProperty("to") String to, @JsonProperty("amount") int amount) {
    this.to = to;
    this.amount = amount;
  }
}
