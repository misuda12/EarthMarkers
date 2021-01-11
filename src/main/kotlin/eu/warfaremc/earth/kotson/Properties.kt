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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

operator fun JsonObject.getValue(thisRef: Any?, property: KProperty<*>): JsonElement = jsonObject[property.name]
operator fun JsonObject.setValue(thisRef: Any?, property: KProperty<*>, value: JsonElement) { jsonObject[property.name] = value }

class JsonObjectDelegate<T : Any>(
        private val jsonObject: JsonObject,
        private val get: (JsonElement) -> T,
        private val set: (T) -> JsonElement,
        private val key: String? = null,
        private val default: (() -> T)? = null
) : ReadWriteProperty<Any?, T> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val element = jsonObject[key ?: property.name]
        if (element === null)
        {
            val default =  this.default
            if (default === null)
                throw NoSuchElementException("'$key' not found")
            else return default.invoke()
        }
        return get(element)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        jsonObject[key ?: property.name] = set(value)
    }
}

val JsonElement.byString     : JsonObjectDelegate<String>
    get() = JsonObjectDelegate(this.jsonObject, { it.string     }, { it.toJson() } )
val JsonElement.byBoolean    : JsonObjectDelegate<Boolean>
    get() = JsonObjectDelegate(this.jsonObject, { it.boolean    }, { it.toJson() } )
val JsonElement.byByte       : JsonObjectDelegate<Byte>
    get() = JsonObjectDelegate(this.jsonObject, { it.byte       }, { it.toJson() } )
val JsonElement.byChar       : JsonObjectDelegate<Char>
    get() = JsonObjectDelegate(this.jsonObject, { it.char       }, { it.toJson() } )
val JsonElement.byShort      : JsonObjectDelegate<Short>
    get() = JsonObjectDelegate(this.jsonObject, { it.short      }, { it.toJson() } )
val JsonElement.byInt        : JsonObjectDelegate<Int>
    get() = JsonObjectDelegate(this.jsonObject, { it.int        }, { it.toJson() } )
val JsonElement.byLong       : JsonObjectDelegate<Long>
    get() = JsonObjectDelegate(this.jsonObject, { it.long       }, { it.toJson() } )
val JsonElement.byFloat      : JsonObjectDelegate<Float>
    get() = JsonObjectDelegate(this.jsonObject, { it.float      }, { it.toJson() } )
val JsonElement.byDouble     : JsonObjectDelegate<Double>
    get() = JsonObjectDelegate(this.jsonObject, { it.double     }, { it.toJson() } )
val JsonElement.byNumber     : JsonObjectDelegate<Number>
    get() = JsonObjectDelegate(this.jsonObject, { it.number     }, { it.toJson() } )
val JsonElement.byBigInteger : JsonObjectDelegate<BigInteger>
    get() = JsonObjectDelegate(this.jsonObject, { it.bigInteger }, { it.toJson() } )
val JsonElement.byBigDecimal : JsonObjectDelegate<BigDecimal>
    get() = JsonObjectDelegate(this.jsonObject, { it.bigDecimal }, { it.toJson() } )
val JsonElement.byJsonArray  : JsonObjectDelegate<JsonArray>
    get() = JsonObjectDelegate(this.jsonObject, { it.jsonArray  }, { it          } )
val JsonElement.byJsonObject : JsonObjectDelegate<JsonObject>
    get() = JsonObjectDelegate(this.jsonObject, { it.jsonObject }, { it          } )

fun JsonElement.byString     ( key: String? = null, default: ( () -> String     )? = null ): JsonObjectDelegate<String>
        = JsonObjectDelegate(this.jsonObject, { it.string     }, { it.toJson() }, key, default )
fun JsonElement.byBoolean    ( key: String? = null, default: ( () -> Boolean    )? = null ): JsonObjectDelegate<Boolean>
        = JsonObjectDelegate(this.jsonObject, { it.boolean    }, { it.toJson() }, key, default )
fun JsonElement.byByte       ( key: String? = null, default: ( () -> Byte       )? = null ): JsonObjectDelegate<Byte>
        = JsonObjectDelegate(this.jsonObject, { it.byte       }, { it.toJson() }, key, default )
fun JsonElement.byChar       ( key: String? = null, default: ( () -> Char       )? = null ): JsonObjectDelegate<Char>
        = JsonObjectDelegate(this.jsonObject, { it.char       }, { it.toJson() }, key, default )
fun JsonElement.byShort      ( key: String? = null, default: ( () -> Short      )? = null ): JsonObjectDelegate<Short>
        = JsonObjectDelegate(this.jsonObject, { it.short      }, { it.toJson() }, key, default )
fun JsonElement.byInt        ( key: String? = null, default: ( () -> Int        )? = null ): JsonObjectDelegate<Int>
        = JsonObjectDelegate(this.jsonObject, { it.int        }, { it.toJson() }, key, default )
fun JsonElement.byLong       ( key: String? = null, default: ( () -> Long       )? = null ): JsonObjectDelegate<Long>
        = JsonObjectDelegate(this.jsonObject, { it.long       }, { it.toJson() }, key, default )
fun JsonElement.byFloat      ( key: String? = null, default: ( () -> Float      )? = null ): JsonObjectDelegate<Float>
        = JsonObjectDelegate(this.jsonObject, { it.float      }, { it.toJson() }, key, default )
fun JsonElement.byDouble     ( key: String? = null, default: ( () -> Double     )? = null ): JsonObjectDelegate<Double>
        = JsonObjectDelegate(this.jsonObject, { it.double     }, { it.toJson() }, key, default )
fun JsonElement.byNumber     ( key: String? = null, default: ( () -> Number     )? = null ): JsonObjectDelegate<Number>
        = JsonObjectDelegate(this.jsonObject, { it.number     }, { it.toJson() }, key, default )
fun JsonElement.byBigInteger ( key: String? = null, default: ( () -> BigInteger )? = null ): JsonObjectDelegate<BigInteger>
        = JsonObjectDelegate(this.jsonObject, { it.bigInteger }, { it.toJson() }, key, default )
fun JsonElement.byBigDecimal ( key: String? = null, default: ( () -> BigDecimal )? = null ): JsonObjectDelegate<BigDecimal>
        = JsonObjectDelegate(this.jsonObject, { it.bigDecimal }, { it.toJson() }, key, default )
fun JsonElement.byJsonArray  ( key: String? = null, default: ( () -> JsonArray  )? = null ): JsonObjectDelegate<JsonArray>
        = JsonObjectDelegate(this.jsonObject, { it.jsonArray  }, { it          }, key, default )
fun JsonElement.byJsonObject ( key: String? = null, default: ( () -> JsonObject )? = null ): JsonObjectDelegate<JsonObject>
        = JsonObjectDelegate(this.jsonObject, { it.jsonObject }, { it          }, key, default )

class NullableJsonObjectDelegate<T : Any?>(
        private val jsonObject: JsonObject,
        private val get: (JsonElement) -> T?,
        private val set: (T?) -> JsonElement,
        private val key: String? = null,
        private val default: (() -> T)? = null
) : ReadWriteProperty<Any?, T?> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val element = jsonObject[key ?: property.name]
        if (element === null)
            return default?.invoke()
        return get(element)
    }
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        jsonObject[key ?: property.name] = set(value)
    }
}

val JsonElement.byNullableString     : NullableJsonObjectDelegate<String?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.string     }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableBoolean    : NullableJsonObjectDelegate<Boolean?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.boolean    }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableByte       : NullableJsonObjectDelegate<Byte?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.byte       }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableChar       : NullableJsonObjectDelegate<Char?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.char       }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableShort      : NullableJsonObjectDelegate<Short?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.short      }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableInt        : NullableJsonObjectDelegate<Int?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.int        }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableLong       : NullableJsonObjectDelegate<Long?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.long       }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableFloat      : NullableJsonObjectDelegate<Float?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.float      }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableDouble     : NullableJsonObjectDelegate<Double?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.double     }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableNumber     : NullableJsonObjectDelegate<Number?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.number     }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableBigInteger : NullableJsonObjectDelegate<BigInteger?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.bigInteger }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableBigDecimal : NullableJsonObjectDelegate<BigDecimal?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.bigDecimal }, { it?.toJson() ?: jsonNull } )
val JsonElement.byNullableArray      : NullableJsonObjectDelegate<JsonArray?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.jsonArray  }, { it           ?: jsonNull } )
val JsonElement.byNullableObject     : NullableJsonObjectDelegate<JsonObject?>
    get() = NullableJsonObjectDelegate(this.jsonObject, { it.jsonObject }, { it           ?: jsonNull } )

fun JsonElement.byNullableString     (key: String? = null, default: ( () -> String     )? = null): NullableJsonObjectDelegate<String?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.string     }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableBoolean    (key: String? = null, default: ( () -> Boolean    )? = null): NullableJsonObjectDelegate<Boolean?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.boolean    }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableByte       (key: String? = null, default: ( () -> Byte       )? = null): NullableJsonObjectDelegate<Byte?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.byte       }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableChar       (key: String? = null, default: ( () -> Char       )? = null): NullableJsonObjectDelegate<Char?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.char       }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableShort      (key: String? = null, default: ( () -> Short      )? = null): NullableJsonObjectDelegate<Short?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.short      }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableInt        (key: String? = null, default: ( () -> Int        )? = null): NullableJsonObjectDelegate<Int?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.int        }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableLong       (key: String? = null, default: ( () -> Long       )? = null): NullableJsonObjectDelegate<Long?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.long       }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableFloat      (key: String? = null, default: ( () -> Float      )? = null): NullableJsonObjectDelegate<Float?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.float      }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableDouble     (key: String? = null, default: ( () -> Double     )? = null): NullableJsonObjectDelegate<Double?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.double     }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableNumber     (key: String? = null, default: ( () -> Number     )? = null): NullableJsonObjectDelegate<Number?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.number     }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableBigInteger (key: String? = null, default: ( () -> BigInteger )? = null): NullableJsonObjectDelegate<BigInteger?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.bigInteger }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableBigDecimal (key: String? = null, default: ( () -> BigDecimal )? = null): NullableJsonObjectDelegate<BigDecimal?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.bigDecimal }, { it?.toJson() ?: jsonNull }, key, default )
fun JsonElement.byNullableJsonArray  (key: String? = null, default: ( () -> JsonArray  )? = null): NullableJsonObjectDelegate<JsonArray?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.jsonArray  }, { it           ?: jsonNull }, key, default )
fun JsonElement.byNullableJsonObject (key: String? = null, default: ( () -> JsonObject )? = null): NullableJsonObjectDelegate<JsonObject?>
        = NullableJsonObjectDelegate(this.jsonObject, { it.jsonObject }, { it           ?: jsonNull }, key, default )

class JsonArrayDelegate<T : Any>(
        private val jsonArray: JsonArray,
        private val index: Int,
        private val get: (JsonElement) -> T,
        private val set: (T) -> JsonElement
) : ReadWriteProperty<Any?, T> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get(jsonArray[index])
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { jsonArray[index] = set(value) }
}

fun JsonElement.byString     (index: Int): JsonArrayDelegate<String>
        = JsonArrayDelegate(this.jsonArray, index, { it.string     }, { it.toJson() } )
fun JsonElement.byBoolean    (index: Int): JsonArrayDelegate<Boolean>
        = JsonArrayDelegate(this.jsonArray, index, { it.boolean    }, { it.toJson() } )
fun JsonElement.byByte       (index: Int): JsonArrayDelegate<Byte>
        = JsonArrayDelegate(this.jsonArray, index, { it.byte       }, { it.toJson() } )
fun JsonElement.byChar       (index: Int): JsonArrayDelegate<Char>
        = JsonArrayDelegate(this.jsonArray, index, { it.char       }, { it.toJson() } )
fun JsonElement.byShort      (index: Int): JsonArrayDelegate<Short>
        = JsonArrayDelegate(this.jsonArray, index, { it.short      }, { it.toJson() } )
fun JsonElement.byInt        (index: Int): JsonArrayDelegate<Int>
        = JsonArrayDelegate(this.jsonArray, index, { it.int        }, { it.toJson() } )
fun JsonElement.byLong       (index: Int): JsonArrayDelegate<Long>
        = JsonArrayDelegate(this.jsonArray, index, { it.long       }, { it.toJson() } )
fun JsonElement.byFloat      (index: Int): JsonArrayDelegate<Float>
        = JsonArrayDelegate(this.jsonArray, index, { it.float      }, { it.toJson() } )
fun JsonElement.byDouble     (index: Int): JsonArrayDelegate<Double>
        = JsonArrayDelegate(this.jsonArray, index, { it.double     }, { it.toJson() } )
fun JsonElement.byNumber     (index: Int): JsonArrayDelegate<Number>
        = JsonArrayDelegate(this.jsonArray, index, { it.number     }, { it.toJson() } )
fun JsonElement.byBigInteger (index: Int): JsonArrayDelegate<BigInteger>
        = JsonArrayDelegate(this.jsonArray, index, { it.bigInteger }, { it.toJson() } )
fun JsonElement.byBigDecimal (index: Int): JsonArrayDelegate<BigDecimal>
        = JsonArrayDelegate(this.jsonArray, index, { it.bigDecimal }, { it.toJson() } )
fun JsonElement.byJsonArray  (index: Int): JsonArrayDelegate<JsonArray>
        = JsonArrayDelegate(this.jsonArray, index, { it.jsonArray  }, { it          } )
fun JsonElement.byJsonObject (index: Int): JsonArrayDelegate<JsonObject>
        = JsonArrayDelegate(this.jsonArray, index, { it.jsonObject }, { it          } )