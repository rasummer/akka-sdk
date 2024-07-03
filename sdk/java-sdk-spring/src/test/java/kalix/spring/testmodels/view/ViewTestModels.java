/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package kalix.spring.testmodels.view;

import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.JWT;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Consume;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import kalix.javasdk.view.View;
import kalix.spring.testmodels.eventsourcedentity.Employee;
import kalix.spring.testmodels.eventsourcedentity.EmployeeEvent;
import kalix.spring.testmodels.eventsourcedentity.EventSourcedEntitiesTestModels.EmployeeEntity;
import kalix.spring.testmodels.valueentity.AssignedCounter;
import kalix.spring.testmodels.valueentity.AssignedCounterState;
import kalix.spring.testmodels.valueentity.Counter;
import kalix.spring.testmodels.valueentity.CounterState;
import kalix.spring.testmodels.valueentity.TimeTrackerEntity;
import kalix.spring.testmodels.valueentity.User;
import kalix.spring.testmodels.valueentity.UserEntity;

public class ViewTestModels {

  // common query parameter for views in this file
  public record ByEmail(String email) {
  }


  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class) // when types are annotated, it's implicitly a transform = false
  public static class UserByEmailWithGet extends View<User> {

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null; // TODO: user should not implement this. we need to find a nice API for this
    }
  }
  @ViewId("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithoutTableAnnotation extends View<User> {

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null; // TODO: user should not implement this. we need to find a nice API for this
    }
  }

  @ViewId("users_view")
  @Table(" ")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithEmptyTableAnnotation extends View<User> {

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null; // TODO: user should not implement this. we need to find a nice API for this
    }
  }

  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithoutViewIdAnnotation extends View<User> {


    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null; // TODO: user should not implement this. we need to find a nice API for this
    }
  }

  @ViewId(" ")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithEmptyViewIdAnnotation extends View<User> {

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(String email) {
      return null; // TODO: user should not implement this. we need to find a nice API for this
    }
  }


  @ViewId("users_view")
  @Table("users_view")
  public static class TransformedUserView extends View<TransformedUser> {

    // when methods are annotated, it's implicitly a transform = true
    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class TransformedUserViewWithDeletes extends View<TransformedUser> {

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class TransformedUserViewWithMethodLevelJWT extends View<TransformedUser> {

    // when methods are annotated, it's implicitly a transform = true
    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    @JWT(
        validate = JWT.JwtMethodMode.BEARER_TOKEN,
        bearerTokenIssuer = {"a", "b"},
        staticClaims = {
            @JWT.StaticClaim(claim = "role", value = "admin"),
            @JWT.StaticClaim(claim = "aud", value = "${ENV}.kalix.io")
        })
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  @JWT(
    validate = JWT.JwtMethodMode.BEARER_TOKEN,
    bearerTokenIssuer = {"a", "b"},
    staticClaims = {
        @JWT.StaticClaim(claim = "role", value = "admin"),
        @JWT.StaticClaim(claim = "aud", value = "${ENV}.kalix.io")
    })
  public static class ViewWithServiceLevelJWT extends View<User> {
    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null;
    }
  }



  /**
   * This should be illegal. Either we subscribe at type level, and it's a transform = false. Or we
   * subscribe at method level, and it's a transform = true.
   */
  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithSubscriptionsInMixedLevels extends View<TransformedUser> {

    // when methods are annotated, it's implicitly a transform = true
    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class) //it's implicitly a transform = false
  public static class TransformedViewWithoutSubscriptionOnMethodLevel extends View<TransformedUser> {

    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithSubscriptionsInMixedLevelsHandleDelete extends View<User> {

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<User> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewWithoutSubscriptionButWithHandleDelete extends View<TransformedUser> {

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewDuplicatedHandleDeletesAnnotations extends View<TransformedUser> {

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete2() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewHandleDeletesWithParam extends View<TransformedUser> {

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete(User user) {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewWithHandleDeletesFalseOnMethodLevel extends View<TransformedUser> {

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = false)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewDuplicatedVESubscriptions extends View<TransformedUser> {

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
        .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(UserEntity.class)
    public Effect<TransformedUser> onChange2(User user) {
      return effects()
          .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  public static class ViewDuplicatedESSubscriptions extends View<TransformedUser> {

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<TransformedUser> onChange(User user) {
      return effects()
        .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<TransformedUser> onChange2(User user) {
      return effects()
        .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
    }

    @Consume.FromValueEntity(value = UserEntity.class, handleDeletes = true)
    public Effect<TransformedUser> onDelete() {
      return effects().deleteState();
    }

    @Query("SELECT * FROM users_view WHERE email = :email")
    public TransformedUser getUser(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithNoQuery extends View<TransformedUser> {}

  @ViewId("users_view")
  @Table("users_view")
  @Consume.FromValueEntity(UserEntity.class)
  public static class ViewWithTwoQueries extends View<User> {

    @Query("SELECT * FROM users_view WHERE email = :email")
    public User getUserByEmail(ByEmail byEmail) {
      return null;
    }

    @Query("SELECT * FROM users_view WHERE email = :email AND name = :name")
    public User getUserByNameAndEmail(ByNameAndEmail byEmail) {
      return null;
    }
  }



  @ViewId("users_view")
  @Table(value = "employees_view")
  public static class SubscribeToEventSourcedEvents extends View<Employee> {

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<Employee> onCreated(EmployeeEvent.EmployeeCreated created) {
      return effects()
          .updateState(new Employee(created.firstName, created.lastName, created.email));
    }

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<Employee> onUpdated(EmployeeEvent.EmployeeEmailUpdated updated) {
      return effects().ignore();
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table(value = "employees_view")
  public static class SubscribeToSealedEventSourcedEvents extends View<Employee> {

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<Employee> handle(EmployeeEvent event) {
      return switch (event) {
        case EmployeeEvent.EmployeeCreated created ->
           effects()
              .updateState(new Employee(created.firstName, created.lastName, created.email));
        case EmployeeEvent.EmployeeEmailUpdated updated ->
           effects().ignore();
      };
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table(value = "employees_view")
  public static class SubscribeToEventSourcedWithMissingHandler extends View<Employee> {

    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public Effect<Employee> onCreated(EmployeeEvent.EmployeeCreated created) {
      return effects()
          .updateState(new Employee(created.firstName, created.lastName, created.email));
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }


  @ViewId("users_view")
  @Table(value = "employees_view")
  @Consume.FromEventSourcedEntity(value = EmployeeEntity.class, ignoreUnknown = false)
  public static class TypeLevelSubscribeToEventSourcedEventsWithMissingHandler extends View<Employee> {

    public Effect<Employee> onEvent(EmployeeEvent.EmployeeCreated created) {
      return effects()
          .updateState(new Employee(created.firstName, created.lastName, created.email));
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table(value = "employees_view")
  @Acl(allow = @Acl.Matcher(service = "test"))
  public static class ViewWithServiceLevelAcl extends View<Employee> {

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("users_view")
  @Table(value = "employees_view")
  public static class ViewWithMethodLevelAcl extends View<Employee> {

    @Query("SELECT * FROM employees_view WHERE email = :email")
    @Acl(allow = @Acl.Matcher(service = "test"))
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }


  @ViewId("users_view")
  @Table(value = "users_view_collection")
  @Consume.FromValueEntity(UserEntity.class)
  public static class UserByEmailWithCollectionReturn extends View<User> {

    @Query(value = "SELECT * AS users FROM users_view WHERE name = :name")
    public UserCollection getUser(ByEmail name) {
      return null;
    }
  }


  @ViewId("users_view")
  public static class MultiTableViewValidation {
    @Consume.FromValueEntity(UserEntity.class)
    public static class ViewTableWithoutTableAnnotation extends View<User> {}

    @Table(" ")
    @Consume.FromValueEntity(UserEntity.class)
    public static class ViewTableWithEmptyTableAnnotation extends View<User> {}

    @Table("users_view")
    @Consume.FromValueEntity(UserEntity.class)
    public static class ViewTableWithMixedLevelSubscriptions extends View<TransformedUser> {
      @Consume.FromValueEntity(UserEntity.class)
      public Effect<TransformedUser> onChange(User user) {
        return effects()
            .updateState(new TransformedUser(user.lastName + ", " + user.firstName, user.email));
      }
    }
  }

  @ViewId("multi-table-view-without-query")
  public static class MultiTableViewWithoutQuery {
    @Table("users_view")
    public static class Users extends View<User> {}
  }

  public static class MultiTableViewWithoutViewId {

    @Table("users_view")
    public static class Users extends View<User> {}

    @Query("SELECT * FROM users_view")
    public User query1() {
      return null;
    }
  }

  @ViewId(" ")
  public static class MultiTableViewWithEmptyViewId {

    @Table("users_view")
    public static class Users extends View<User> {}

    @Query("SELECT * FROM users_view")
    public User query1() {
      return null;
    }
  }

  @ViewId("users_multi_view")
  public static class MultiTableViewWithViewIdInInnerView {

    @ViewId("users_view")
    @Table("users_view")
    public static class Users extends View<User> {}

    @Query("SELECT * FROM users_view")
    public User query1() {
      return null;
    }
  }

  @ViewId("users_multi_view")
  @Table("users_multi_view")
  public static class MultiTableViewWithTableName {

    @Table("users_view")
    public static class Users extends View<User> {}

    @Query("SELECT * FROM users_view")
    public User query1() {
      return null;
    }
  }

  @ViewId("multi-table-view-with-multiple-queries")
  public static class MultiTableViewWithMultipleQueries {
    @Query("SELECT * FROM users_view")
    public User query1() {
      return null;
    }

    @Query("SELECT * FROM users_view")
    public User query2() {
      return null;
    }

    @Table("users_view")
    public static class Users extends View<User> {}
  }

  @ViewId("multi-table-view-with-join-query")
  public static class MultiTableViewWithJoinQuery {

    @Query("""
      SELECT employees.*, counters.* as counters
      FROM employees
      JOIN assigned ON assigned.assigneeId = employees.email
      JOIN counters ON assigned.counterId = counters.id
      WHERE employees.email = :email
      """)
    public EmployeeCounters get(ByEmail byEmail) {
      return null;
    }

    @Table("employees")
    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public static class Employees extends View<Employee> {
      public Effect<Employee> onCreated(EmployeeEvent.EmployeeCreated created) {
        return effects()
            .updateState(new Employee(created.firstName, created.lastName, created.email));
      }

      public Effect<Employee> onUpdated(EmployeeEvent.EmployeeEmailUpdated updated) {
        return effects().ignore();
      }
    }

    @Table("counters")
    @Consume.FromValueEntity(Counter.class)
    public static class Counters extends View<CounterState> {}

    @Table("assigned")
    @Consume.FromValueEntity(AssignedCounter.class)
    public static class Assigned extends View<AssignedCounterState> {}
  }

  @ViewId("multi-table-view-with-join-query")
  public static class MultiTableViewWithDuplicatedVESubscriptions {

    @Query("SELECT * FROM users_view")
    public EmployeeCounters get(ByEmail byEmail) {
      return null;
    }

    @Table("employees")
    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public static class Employees extends View<Employee> {
      public Effect<Employee> onEvent(EmployeeEvent.EmployeeCreated created) {
        return effects()
          .updateState(new Employee(created.firstName, created.lastName, created.email));
      }
    }

    @Table("counters")
    @Consume.FromValueEntity(Counter.class)
    public static class Counters extends View<CounterState> {}

    @Table("assigned")
    public static class Assigned extends View<CounterState> {
      @Consume.FromValueEntity(Counter.class)
      public Effect<CounterState> onEvent(CounterState counterState) {
        return effects().ignore();
      }

      @Consume.FromValueEntity(Counter.class)
      public Effect<CounterState> onEvent2(CounterState counterState) {
        return effects().ignore();
      }
    }
  }

  @ViewId("multi-table-view-with-join-query")
  public static class MultiTableViewWithDuplicatedESSubscriptions {

    @Query("SELECT * FROM users_view")
    public EmployeeCounters get(ByEmail byEmail) {
      return null;
    }

    @Table("employees")
    @Consume.FromEventSourcedEntity(EmployeeEntity.class)
    public static class Employees extends View<Employee> {
      public Effect<Employee> onEvent(EmployeeEvent.EmployeeCreated created) {
        return effects()
          .updateState(new Employee(created.firstName, created.lastName, created.email));
      }
    }

    @Table("counters")
    @Consume.FromValueEntity(Counter.class)
    public static class Counters extends View<CounterState> {}

    @Table("assigned")
    public static class Assigned extends View<Employee> {
      @Consume.FromEventSourcedEntity(EmployeeEntity.class)
      public Effect<Employee> onEvent(CounterState counterState) {
        return effects().ignore();
      }

      @Consume.FromEventSourcedEntity(EmployeeEntity.class)
      public Effect<Employee> onEvent2(CounterState counterState) {
        return effects().ignore();
      }
    }
  }

  @ViewId("time-tracker-view")
  @Table("time-tracker-view")
  @Consume.FromValueEntity(TimeTrackerEntity.class)
  public static class TimeTrackerView extends View<TimeTrackerEntity.TimerState> {

    @Query(value = "SELECT * FROM time-tracker-view WHERE name = :name")
    public TimeTrackerEntity.TimerState query2() {
      return null;
    }
  }


  @ViewId("employee_view")
  @Table(value = "employee_table")
  @Consume.FromTopic(value = "source", consumerGroup = "cg")
  public static class TopicTypeLevelSubscriptionView extends View<Employee> {

    public Effect<Employee> onCreate(EmployeeEvent.EmployeeCreated evt) {
      return effects()
        .updateState(new Employee(evt.firstName, evt.lastName, evt.email));
    }

    public Effect<Employee> onEmailUpdate(EmployeeEvent.EmployeeEmailUpdated eeu) {
      var employee = viewState();
      return effects().updateState(new Employee(employee.firstName(), employee.lastName(), eeu.email));
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }

  @ViewId("employee_view")
  @Table(value = "employee_table")
  public static class TopicSubscriptionView extends View<Employee> {

    @Consume.FromTopic(value = "source", consumerGroup = "cg")
    public Effect<Employee> onCreate(EmployeeEvent.EmployeeCreated evt) {
      return effects()
        .updateState(new Employee(evt.firstName, evt.lastName, evt.email));
    }

    @Consume.FromTopic(value = "source", consumerGroup = "cg")
    public Effect<Employee> onEmailUpdate(EmployeeEvent.EmployeeEmailUpdated eeu) {
      var employee = viewState();
      return effects().updateState(new Employee(employee.firstName(), employee.lastName(), eeu.email));
    }

    @Query("SELECT * FROM employees_view WHERE email = :email")
    public Employee getEmployeeByEmail(ByEmail byEmail) {
      return null;
    }
  }
}
