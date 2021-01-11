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

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.Reader

inline fun <reified T : Any> Gson.getAdapter(): TypeAdapter<T> = getAdapter(object: TypeToken<T>() {})
inline fun <reified T : Any> Gson.getGenericAdapter(): TypeAdapter<T> = getAdapter(T::class.java)

inline fun <reified T : Any> Gson.fromJson(json: String): T = fromJson(json, typeToken<T>())
inline fun <reified T : Any> Gson.fromJson(json: Reader): T = fromJson(json, typeToken<T>())
inline fun <reified T : Any> Gson.fromJson(json: JsonReader): T = fromJson(json, typeToken<T>())
inline fun <reified T : Any> Gson.fromJson(json: JsonElement): T = fromJson(json, typeToken<T>())

inline fun <reified T : Any> Gson.typedToJson(source: T): String = toJson(source, typeToken<T>())
inline fun <reified T : Any> Gson.typedToJson(source: T, writer: Appendable): Unit =
        toJson(source, typeToken<T>(), writer)
inline fun <reified T : Any> Gson.typedToJson(source: T, writer: JsonWriter): Unit =
        toJson(source, typeToken<T>(), writer)
inline fun <reified T : Any> Gson.typedToJsonTree(source: T): JsonElement = toJsonTree(source, typeToken<T>())

fun JsonReader.nextIntOrNull(): Int? {
    return if (this.peek() != JsonToken.NULL)
        this.nextInt()
    else {
        this.nextNull()
        null
    }
}

fun JsonReader.nextBooleanOrNull(): Boolean?  {
    return if (this.peek() != JsonToken.NULL)
        this.nextBoolean()
    else {
        this.nextNull()
        null
    }
}

fun JsonReader.nextDoubleOrNull(): Double?  {
    return if (this.peek() != JsonToken.NULL)
        this.nextDouble()
    else {
        this.nextNull()
        null
    }
}

fun JsonReader.nextLongOrNull(): Long?  {
    return if (this.peek() != JsonToken.NULL)
        this.nextLong()
    else {
        this.nextNull()
        null
    }
}

fun JsonReader.nextStringOrNull(): String?  {
    return if (this.peek() != JsonToken.NULL)
        this.nextString()
    else {
        this.nextNull()
        null
    }
}