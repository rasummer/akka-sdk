package com.example;

import akka.javasdk.testkit.AkkaSdkTestKitSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;


/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Java SDK.
 * <p>
 * This test will initiate a Kalix Runtime using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 * <p>
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
public class IntegrationTest extends AkkaSdkTestKitSupport {

  private Duration timeout = Duration.of(5, SECONDS);

  @Test
  public void testTracingPropagation() {
    // TODO
  }

  @Test
  public void testExternalTracingPropagation() {
    // TODO
  }
}
