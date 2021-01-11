/*
 * This file is part of Millennium, licensed under the MIT License.
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
import com.google.gson.stream.JsonWriter

fun Number.toJson(): JsonPrimitive = JsonPrimitive(this)

fun Char.toJson(): JsonPrimitive = JsonPrimitive(this)

fun Boolean.toJson(): JsonPrimitive = JsonPrimitive(this)

fun String.toJson(): JsonPrimitive = JsonPrimitive(this)

internal fun Any?.toJsonElement(): JsonElement {
    if (this == null)
        return jsonNull

    return when (this) {
        is JsonElement -> this
        is String -> toJson()
        is Number -> toJson()
        is Char -> toJson()
        is Boolean -> toJson()
        else -> throw IllegalArgumentException("$this cannot be converted to JSON")
    }
}

private fun jsonArray(values: Iterator<Any?>): JsonArray {
    val array = JsonArray()
    for (value in values)
        array.add(value.toJsonElement())
    return array
}

fun jsonArray(vararg values: Any?) = jsonArray(values.iterator())
fun jsonArray(values: Iterable<*>) = jsonArray(values.iterator())
fun jsonArray(values: Sequence<*>) = jsonArray(values.iterator())

fun Iterable<*>.toJsonArray() = jsonArray(this)
fun Sequence<*>.toJsonArray() = jsonArray(this)

private fun jsonObject(values: Iterator<Pair<String, *>>): JsonObject {
    val obj = JsonObject()
    for ((key, value) in values) {
        obj.add(key, value.toJsonElement())
    }
    return obj
}

fun jsonObject(vararg values: Pair<String, *>) = jsonObject(values.iterator())
fun jsonObject(values: Iterable<Pair<String, *>>) = jsonObject(values.iterator())
fun jsonObject(values: Sequence<Pair<String, *>>) = jsonObject(values.iterator())

fun Iterable<Pair<String, *>>.toJsonObject() = jsonObject(this)
fun Sequence<Pair<String, *>>.toJsonObject() = jsonObject(this)

fun JsonObject.shallowCopy(): JsonObject = JsonObject().apply { this@shallowCopy.entrySet().forEach { put(it) } }
fun JsonArray.shallowCopy(): JsonArray = JsonArray().apply { addAll(this@shallowCopy) }

private fun JsonElement.deepCopy(): JsonElement {
    return when (this) {
        is JsonNull, is JsonPrimitive -> this
        is JsonObject -> deepCopy()
        is JsonArray -> deepCopy()
        else -> throw IllegalArgumentException("Unknown JsonElement: $this")
    }
}

fun JsonObject.deepCopy(): JsonObject = JsonObject().apply {
    this@deepCopy.entrySet().forEach{ add(it.key, it.value.deepCopy()) } }
fun JsonArray.deepCopy(): JsonArray = JsonArray().apply { this@deepCopy.forEach { add(it.deepCopy()) } }

fun JsonWriter.value(value: Any) : JsonWriter {
    return when (value) {
        is Boolean -> value(value)
        is Double -> value(value)
        is Long -> value(value)
        is Number -> value(value)
        is String -> value(value)
        else -> throw IllegalArgumentException("$this cannot be written on JsonWriter")
    }
}