package com.example.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.agent.MemoryProvider;
import akka.javasdk.agent.ModelProvider;
import akka.javasdk.annotations.ComponentId;

public interface MyAgentMore {
  @ComponentId("my-agent")
  public class MyAgentWithModel extends Agent {

    // tag::model[]
    public Effect<String> query(String question) {
      return effects()
        .model(
          ModelProvider.openAi() // <1>
            .withApiKey(System.getenv("OPENAI_API_KEY"))
            .withModelName("gpt-4o")
            .withTemperature(0.6)
            .withMaxTokens(10000)
        )
        .systemMessage("You are a helpful...")
        .userMessage(question)
        .thenReply();
    }
    // end::model[]
  }

  @ComponentId("my-agent-memory")
  public class MyAgentNoMemory extends Agent {

    // tag::no-memory[]
    public Effect<String> ask(String question) {
      return effects()
        .memory(MemoryProvider.none())
        .systemMessage("You are a helpful...")
        .userMessage(question)
        .thenReply();
    }
    // end::no-memory[]
  }

  @ComponentId("my-agent-readlast")
  public class MyAgentReadLastMemory extends Agent {

    // tag::read-last[]
    public Effect<String> ask(String question) {
      return effects()
        .memory(MemoryProvider.limitedWindow().readLast(5))
        .systemMessage("You are a helpful...")
        .userMessage(question)
        .thenReply();
    }
    // end::read-last[]
  }
}
