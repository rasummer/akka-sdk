package user.registry;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import akka.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;
import user.registry.application.UserEntity;
import user.registry.domain.User;
import user.registry.domain.UserEvent;

public class UserEntityTest {

  @Test
  public void testCreationAndUpdate() {
    var userTestKit = EventSourcedTestKit.of(__ -> new UserEntity());

    var creationRes = userTestKit
      .method(UserEntity::createUser)
      .invoke(new UserEntity.Create("John", "Belgium", "john@acme.com"));

    var created = creationRes.getNextEventOfType(UserEvent.UserWasCreated.class);
    assertThat(created.name()).isEqualTo("John");
    assertThat(created.email()).isEqualTo("john@acme.com");

    var updateRes = userTestKit
      .method(UserEntity::changeEmail)
      .invoke(new UserEntity.ChangeEmail("john.doe@acme.com"));
    var emailChanged = updateRes.getNextEventOfType(UserEvent.EmailAssigned.class);
    assertThat(emailChanged.newEmail()).isEqualTo("john.doe@acme.com");
  }

  @Test
  public void updateNonExistentUser() {
    var userTestKit = EventSourcedTestKit.of(__ -> new UserEntity());

    var updateRes = userTestKit
      .method(UserEntity::changeEmail)
      .invoke(new UserEntity.ChangeEmail("john.doe@acme.com"));
    assertThat(updateRes.isError()).isTrue();
    assertThat(updateRes.getError()).isEqualTo("User not found");
  }
}
