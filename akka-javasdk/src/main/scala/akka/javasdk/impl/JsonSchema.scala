/*
 * Copyright (C) 2021-2024 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.javasdk.impl

import akka.annotation.InternalApi
import akka.http.javadsl.model.HttpEntity
import akka.http.javadsl.model.HttpRequest
import akka.javasdk.annotations.Description
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaArray
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaBoolean
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaDataType
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaInteger
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaNumber
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaObject
import akka.runtime.sdk.spi.SpiJsonSchema.JsonSchemaString

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Optional

/**
 * INTERNAL API
 */
@InternalApi
private[impl] object JsonSchema {

  final case class TypedParameter(
      name: String,
      parameterType: Type,
      schemaType: JsonSchemaDataType,
      required: Boolean) {

    def description: Option[String] = schemaType.description

    def parameterClass: Class[_] = parameterType match {
      case c: Class[_]          => c
      case p: ParameterizedType => p.getRawType.asInstanceOf[Class[_]]
    }
  }

  def jsonSchemaFor(method: Method): JsonSchemaObject = {
    val parameters = typedParametersFor(method)
    new JsonSchemaObject(
      description = None,
      properties = parameters.map { case TypedParameter(name, _, schemaType, _) => name -> schemaType }.toMap,
      required = parameters.collect { case TypedParameter(name, _, _, true) => name }.sorted)
  }

  def jsonSchemaFor(value: Class[_]): JsonSchemaObject = {
    jsonSchemaTypeFor(value, None)._1.asInstanceOf[JsonSchemaObject]
  }

  def typedParametersFor(method: Method): Seq[TypedParameter] = {
    val parameterAnnotations = method.getParameterAnnotations.toVector
    val genericParameterTypes = method.getGenericParameterTypes.toVector
    method.getParameters.toVector
      .zip(parameterAnnotations)
      .zip(genericParameterTypes)
      .map { case ((parameter, annotations), genericParameterType) =>
        typedParameter(parameter, annotations, genericParameterType)
      }
  }

  private def typedParameter(
      parameter: Parameter,
      annotations: Array[Annotation],
      genericType: Type): TypedParameter = {
    val description = annotations.collectFirst { case annotation: Description => annotation.value() }
    val (schemaType, required) = jsonSchemaTypeFor(genericType, description)
    TypedParameter(parameter.getName, genericType, schemaType, required)
  }

  private def number(description: Option[String]) = new JsonSchemaNumber(description)
  private def integer(description: Option[String]) = new JsonSchemaInteger(description)
  private def boolean(description: Option[String]) = new JsonSchemaBoolean(description)

  private final val typeNameMap: Map[String, Option[String] => JsonSchemaDataType] = Map(
    "short" -> integer,
    "byte" -> integer,
    "char" -> integer,
    "int" -> integer,
    "long" -> integer,
    "double" -> number,
    "float" -> number,
    "boolean" -> boolean,
    "java.lang.Short" -> integer,
    "java.lang.Byte" -> integer,
    "java.lang.Char" -> integer,
    "java.lang.Integer" -> integer,
    "java.lang.Long" -> integer,
    "java.lang.Double" -> number,
    "java.lang.Float" -> number,
    "java.lang.Boolean" -> boolean)

  private def emptyObject(description: Option[String]) =
    new JsonSchemaObject(description, properties = Map.empty, required = Seq.empty)

  // Note: we don't support recursive types, not quite sure if we should though
  private def jsonSchemaTypeFor(genericFieldType: Type, description: Option[String]): (JsonSchemaDataType, Boolean) = {
    typeNameMap.get(genericFieldType.getTypeName) match {
      case Some(jsTypeFactory) => (jsTypeFactory(description), true)
      case None =>
        val clazz = genericFieldType match {
          case c: Class[_]          => c
          case p: ParameterizedType => p.getRawType.asInstanceOf[Class[_]]
        }
        genericFieldType match {
          case _ if clazz == classOf[HttpRequest] => // for body parameter
            (emptyObject(description), false) // unknown if body is required
          case _ if clazz == classOf[HttpEntity.Strict] => // for body parameter
            (emptyObject(description), true)
          case _ if clazz == classOf[String] =>
            (new JsonSchemaString(description), true)
          case p: ParameterizedType if clazz == classOf[Optional[_]] =>
            val (jsonFieldType, _) = jsonSchemaTypeFor(p.getActualTypeArguments.head, description)
            (jsonFieldType, false)
          case p: ParameterizedType if classOf[java.util.Collection[_]].isAssignableFrom(clazz) =>
            (new JsonSchemaArray(items = jsonSchemaTypeFor(p.getActualTypeArguments.head, None)._1, description), true)
          case _ =>
            // Note: for now the top level can only be a class
            val properties = clazz.getDeclaredFields.toVector.map { field: Field =>
              val description = field.getAnnotation(classOf[Description]) match {
                case null       => None
                case annotation => Some(annotation.value())
              }

              field.getName -> jsonSchemaTypeFor(field.getGenericType, description)
            }.toMap

            val jsObjectSchema = new JsonSchemaObject(
              description = description,
              properties = properties.map { case (key, (schemaType, _)) => key -> schemaType },
              // All fields that are not wrapped in Optional are listed as required
              required = properties.collect { case (key, (_, required)) if required => key }.toSeq.sorted)

            (jsObjectSchema, true)
        }
    }
  }

}
