/*
 * Copyright (C) 2021-2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.impl

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class WorkflowEntityDescriptorFactorySpec extends AnyWordSpec with Matchers {

  "Workflow descriptor factory" should {
    "validate a Workflow must be declared as public" in {
      intercept[ValidationException] {
        Validations.validate(classOf[NotPublicComponents.NotPublicWorkflow]).failIfInvalid()
      }.getMessage should include("NotPublicWorkflow is not marked with `public` modifier. Components must be public.")
    }
  }

}
