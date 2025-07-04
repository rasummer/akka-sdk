/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package com.example;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;

interface Outer {

  @ComponentId("nested-event-sourced")
  class NestedEventSourcedEntity extends EventSourcedEntity<NestedEventSourcedEntity.State, NestedEventSourcedEntity.Event> {


    record State(String value) {
    }

    record Event(String value) {
    }


    @Override
    public State applyEvent(Event event) {
      return new State(event.value);
    }
  }
}
