/*
 * This file is part of Noodle, licensed under the MIT License.
 *
 * Copyright (C) Millennium & Team
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.warfaremc.earth.kotson

import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger

private fun <T : Any> JsonElement?.nullOr(getNotNull: JsonElement.() -> T) : T? =
        if (this == null || isJsonNull) null else getNotNull()

val JsonElement.string: String
    get() = asString
val JsonElement?.nullString: String?
    get() = nullOr { string }

val JsonElement.boolean: Boolean
    get() = asBoolean
val JsonElement?.nullBoolean: Boolean?
    get() = nullOr { boolean }

val JsonElement.byte: Byte
    get() = asByte
val JsonElement?.nullByte: Byte?
    get() = nullOr { byte }

val JsonElement.char: Char
    get() = asCharacter
val JsonElement?.nullChar: Char?
    get() = nullOr { char }

val JsonElement.short: Short
    get() = asShort
val JsonElement?.nullShort: Short?
    get() = nullOr { short }

val JsonElement.int: Int
    get() = asInt
val JsonElement?.nullInt: Int?
    get() = nullOr { int }

val JsonElement.long: Long
    get() = asLong
val JsonElement?.nullLong: Long?
    get() = nullOr { long }

val JsonElement.float: Float
    get() = asFloat
val JsonElement?.nullFloat: Float?
    get() = nullOr { float }

val JsonElement.double: Double
    get() = asDouble
val JsonElement?.nullDouble: Double?
    get() = nullOr { double }

val JsonElement.number: Number
    get() = asNumber
val JsonElement?.nullNumber: Number?
    get() = nullOr { number }

val JsonElement.bigInteger: BigInteger
    get() = asBigInteger
val JsonElement?.nullBigInteger: BigInteger?
    get() = nullOr { bigInteger }

val JsonElement.bigDecimal: BigDecimal
    get() = asBigDecimal
val JsonElement?.nullBigDecimal: BigDecimal?
    get() = nullOr { bigDecimal }

val JsonElement.jsonArray: JsonArray
    get() = asJsonArray
val JsonElement?.nullJsonArray: JsonArray?
    get() = nullOr { jsonArray }

val JsonElement.jsonObject: JsonObject
    get() = asJsonObject
val JsonElement?.nullJsonObject: JsonObject?
    get() = nullOr { jsonObject }

val jsonNull: JsonNull = JsonNull.INSTANCE

operator fun JsonElement.get(key: String): JsonElement = jsonObject.getNotNull(key)
operator fun JsonElement.get(index: Int): JsonElement = jsonArray.get(index)

fun JsonObject.getNotNull(key: String): JsonElement = get(key) ?: throw NoSuchElementException("'$key' is not found")

operator fun JsonObject.contains(key: String): Boolean = has(key)

fun JsonObject.entrySetSize(): Int = entrySet().size
fun JsonObject.isEmpty(): Boolean = entrySet().isEmpty()
fun JsonObject.isNotEmpty(): Boolean = entrySet().isNotEmpty()
fun JsonObject.keys(): Collection<String> = entrySet().map { it.key }
fun JsonObject.forEach(operation: (String, JsonElement) -> Unit): Unit = entrySet()
        .forEach { operation(it.key, it.value) }
fun JsonObject.toMap(): Map<String, JsonElement> = entrySet().associateBy({ it.key }, { it.value })

fun JsonObject.addProperty(property: String, value: JsonElement?) = add(property, value)
fun JsonObject.addProperty(property: String, value: Any?, context: JsonSerializationContext) =
        add(property, context.serialize(value))
fun JsonObject.addPropertyIfNotNull(property: String, value: String?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Char?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Boolean?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Number?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: JsonElement?) = value?.let { addProperty(property, value) }
fun JsonObject.addPropertyIfNotNull(property: String, value: Any?, context: JsonSerializationContext) =
        value?.let { addProperty(property, value, context) }

operator fun JsonArray.contains(value: Any): Boolean = contains(value.toJsonElement())