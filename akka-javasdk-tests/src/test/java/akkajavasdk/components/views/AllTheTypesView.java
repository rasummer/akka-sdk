/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akkajavasdk.components.views;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ComponentId("all_the_field_types_view")
public class AllTheTypesView extends View {

  // indexable fields of AllTheTypesKvEntity.AllTheTypes
  public record AllTheQueryableTypes(
      int intValue,
      long longValue,
      float floatValue,
      double doubleValue,
      boolean booleanValue,
      String stringValue,
      Integer wrappedInt,
      Long wrappedLong,
      Float wrappedFloat,
      Double wrappedDouble,
      Boolean wrappedBoolean,
      Instant instant,
      Optional<String> optionalString,
      List<String> repeatedString,
      // AllTheTypesKvEntity.AllTheTypes.nestedMessage.email
      // Note: nested classes not supported in query parameter
      String nestedEmail
      // FIXME indexing on enums not supported yet: AllTheTypesKvEntity.AnEnum anEnum
      // Note: recursive structures cannot be indexed
  ) {}


  @Consume.FromKeyValueEntity(AllTheTypesKvEntity.class)
  public static class Events extends TableUpdater<AllTheTypesKvEntity.AllTheTypes> { }

  @Query("""
      SELECT * FROM events WHERE
      intValue = :intValue AND
      longValue = :longValue AND
      floatValue = :floatValue AND
      doubleValue = :doubleValue AND
      booleanValue = :booleanValue AND
      stringValue = :stringValue AND
      wrappedInt = :wrappedInt AND
      wrappedLong = :wrappedLong AND
      wrappedFloat = :wrappedFloat AND
      wrappedDouble = :wrappedDouble AND
      wrappedBoolean = :wrappedBoolean AND
      instant = :instant AND
      optionalString = :optionalString AND
      repeatedString = :repeatedString AND
      nestedMessage.email = :nestedEmail
      """)
  public QueryStreamEffect<AllTheTypesKvEntity.AllTheTypes> specificRow(AllTheQueryableTypes query) { return queryStreamResult(); }

  @Query("SELECT * FROM events")
  public QueryStreamEffect<AllTheTypesKvEntity.AllTheTypes> allRows() {
    return queryStreamResult();
  }

}
