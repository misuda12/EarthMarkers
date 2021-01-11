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
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

@Suppress("PROTECTED_CALL_FROM_PUBLIC_INLINE")
inline fun <reified T: Any> gsonTypeToken(): Type  = object : TypeToken<T>() {} .type

fun ParameterizedType.isWildcard(): Boolean {
    var hasWildcard = false
    var hasSpecific = false
    var hasBase = false

    val cls = this.rawType as Class<*>
    cls.typeParameters.forEachIndexed { index, variable ->
        val argument = actualTypeArguments[index]

        if (argument is WildcardType) {
            val hit = variable.bounds.firstOrNull { it in argument.upperBounds }
            if (hit != null)
                if (hit == Any::class.java)
                    hasWildcard = true
                else hasBase = true
            else hasSpecific = true
        }
        else hasSpecific = true
    }

    if (hasWildcard && hasSpecific)
        throw IllegalArgumentException("Either none or all type parameters can be wildcard in $this")
    return hasWildcard || (hasBase && !hasSpecific)
}

fun removeTypeWildcards(type: Type): Type {

    if (type is ParameterizedType) {
        val arguments = type.actualTypeArguments
                .map { if (it is WildcardType) it.upperBounds[0] else it }
                .map { removeTypeWildcards(it) }
                .toTypedArray()
        return TypeToken.getParameterized(type.rawType, *arguments).type
    }
    return type
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
inline fun <reified T: Any> typeToken(): Type {
    val type = gsonTypeToken<T>()

    if (type is ParameterizedType && type.isWildcard())
        return type.rawType

    return removeTypeWildcards(type)
}

data class SerializerArgument<T>(
        val source: T,
        val type: Type,
        val context: Context
) {
    class Context(val gsonContext: JsonSerializationContext) : JsonSerializationContext by gsonContext {
        inline fun <reified T: Any> typedSerialize(src: T) = gsonContext.serialize(src, typeToken<T>())
    }
}

data class DeserializerArgument(
        val json: JsonElement,
        val type: Type,
        val context: Context
) {
    class Context(val gsonContext: JsonDeserializationContext): JsonDeserializationContext by gsonContext {
        inline fun <reified T: Any> deserialize(json: JsonElement) = gsonContext.deserialize<T>(json, typeToken<T>())
    }
}

fun <T: Any> jsonSerializer(serializer: (argument: SerializerArgument<T>) -> JsonElement): JsonSerializer<T>
        = JsonSerializer { source,
                           type,
                           context -> serializer(SerializerArgument(source, type, SerializerArgument.Context(context))) }

fun <T: Any> jsonDeserializer(deserializer: (argument: DeserializerArgument) -> T?): JsonDeserializer<T>
        = JsonDeserializer<T> { json,
                                type,
                                context -> deserializer(DeserializerArgument(json, type, DeserializerArgument.Context(context))) }

fun <T: Any> instanceCreator(creator: (type: Type) -> T): InstanceCreator<T>
        = InstanceCreator { creator(it) }

interface TypeAdapterBuilder<T: Any, R : T?> {
    fun read (function: JsonReader.() -> R)
    fun write(function: JsonWriter.(value: T) -> Unit)
}

internal class TypeAdapterBuilderImpl<T: Any, R : T?>(
        init: TypeAdapterBuilder<T, R>.() -> Unit
) : TypeAdapterBuilder<T, R> {

    private var reader: (JsonReader.() -> R)? = null
    private var writer: (JsonWriter.(value: T) -> Unit)? = null

    override fun read(function: JsonReader.() -> R) {
        reader = function
    }

    override fun write(function: JsonWriter.(value: T) -> Unit) {
        writer = function
    }

    fun build(): TypeAdapter<T> = object : TypeAdapter<T>() {
        override fun read (reader0: JsonReader) = reader!!.invoke(reader0)
        override fun write(writer0: JsonWriter, value: T) = writer!!.invoke(writer0, value)
    }

    init {
        init()
        if (reader == null || writer == null)
            throw IllegalArgumentException("You must define both a read and a write function")
    }
}

fun <T: Any> typeAdapter(init: TypeAdapterBuilder<T, T>.() -> Unit): TypeAdapter<T>
        = TypeAdapterBuilderImpl(init).build()
fun <T: Any> nullableTypeAdapter(init: TypeAdapterBuilder<T, T?>.() -> Unit): TypeAdapter<T>
        = TypeAdapterBuilderImpl<T, T?>(init).build().nullSafe()

inline fun <reified T: Any> GsonBuilder.registerTypeAdapter(typeAdapter: Any): GsonBuilder
        = this.registerTypeAdapter(typeToken<T>(), typeAdapter)
inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(serializer: JsonSerializer<T>): GsonBuilder
        = this.registerTypeAdapter<T>(serializer as Any)
inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(deserializer: JsonDeserializer<T>): GsonBuilder
        = this.registerTypeAdapter<T>(deserializer as Any)
inline fun <reified T: Any> GsonBuilder.registerTypeHierarchyAdapter(typeAdapter: Any): GsonBuilder
        = this.registerTypeHierarchyAdapter(T::class.java, typeAdapter)

interface RegistrationBuilder<T: Any, R : T?> : TypeAdapterBuilder<T, R> {
    fun serialize(serializer: (argument: SerializerArgument<T>) -> JsonElement)
    fun deserialize(deserializer: (DeserializerArgument) -> T?)
    fun createInstances(creator: (type: Type) -> T)
}

internal class RegistrationBuilderImpl<T: Any>(
    val registeredType: Type,
    init: RegistrationBuilder<T, T>.() -> Unit,
    protected val register: (typeAdapter: Any) -> Unit
) : RegistrationBuilder<T, T> {

    protected enum class API { SD, RW }

    private var api: API? = null

    private var reader: (JsonReader.() -> T)? = null
    private var writer: (JsonWriter.(value: T) -> Unit)? = null

    private fun checkAPI(api: API) {
        if (this.api != null && this.api != api)
            throw IllegalArgumentException("You cannot use serialize/deserialize and read/write for the same type")
        this.api = api
    }

    override fun serialize(serializer: (argument: SerializerArgument<T>) -> JsonElement) {
        checkAPI(API.SD)
        register(jsonSerializer(serializer))
    }

    override fun deserialize(deserializer: (DeserializerArgument) -> T?) {
        checkAPI(API.SD)
        register(jsonDeserializer(deserializer))
    }

    override fun createInstances(creator: (type: Type) -> T) = register(instanceCreator(creator))

    override fun read(function: JsonReader.() -> T) {
        reader = function
        registerTypeAdapter()
    }

    override fun write(function: JsonWriter.(value: T) -> Unit) {
        writer = function
        registerTypeAdapter()
    }

    private fun registerTypeAdapter() {
        checkAPI(API.RW)
        val reader = this.reader
        val writer = this.writer
        if (reader == null || writer == null)
            return
        register(typeAdapter<T> { read(reader) ; write(writer) })
        this.reader = null
        this.writer = null
    }

    init {
        init()
        if (reader != null)
            throw IllegalArgumentException("You cannot define a read function without a write function")
        if (writer != null)
            throw IllegalArgumentException("You cannot define a write function without a read function")
    }
}

fun <T : Any> GsonBuilder.registerTypeAdapterBuilder(type: Type,
                                                     init: RegistrationBuilder<T, T>.() -> Unit): GsonBuilder {
    RegistrationBuilderImpl(type, init) { registerTypeAdapter(type, it) }
    return this
}

inline fun <reified T: Any> GsonBuilder.registerTypeAdapter(noinline init: RegistrationBuilder<T, T>.() -> Unit): GsonBuilder
        = registerTypeAdapterBuilder(typeToken<T>(), init)

fun <T: Any> GsonBuilder.registerNullableTypeAdapterBuilder(type: Type,
                                                            init: TypeAdapterBuilder<T, T?>.() -> Unit): GsonBuilder {
    registerTypeAdapter(type, nullableTypeAdapter(init))
    return this
}

inline fun <reified T: Any> GsonBuilder.registerNullableTypeAdapter(noinline init: TypeAdapterBuilder<T, T?>.() -> Unit): GsonBuilder
        = registerNullableTypeAdapterBuilder(typeToken<T>(), init)


fun <T: Any> GsonBuilder.registerTypeHierarchyAdapterBuilder(type: Class<T>,
                                                             init: RegistrationBuilder<T, T>.() -> Unit): GsonBuilder {
    RegistrationBuilderImpl(type, init) { registerTypeHierarchyAdapter(type, it) }
    return this
}

inline fun <reified T: Any> GsonBuilder.registerTypeHierarchyAdapter(noinline init: RegistrationBuilder<T, T>.() -> Unit): GsonBuilder
        = registerTypeHierarchyAdapterBuilder(T::class.java, init)


fun <T: Any> GsonBuilder.registerNullableTypeHierarchyAdapterBuilder(type: Class<T>, init: TypeAdapterBuilder<T, T?>.() -> Unit): GsonBuilder {
    registerTypeHierarchyAdapter(type, nullableTypeAdapter(init))
    return this
}

inline fun <reified T: Any> GsonBuilder.registerNullableTypeHierarchyAdapter(noinline init: TypeAdapterBuilder<T, T?>.() -> Unit): GsonBuilder
        = registerNullableTypeHierarchyAdapterBuilder(T::class.java, init)