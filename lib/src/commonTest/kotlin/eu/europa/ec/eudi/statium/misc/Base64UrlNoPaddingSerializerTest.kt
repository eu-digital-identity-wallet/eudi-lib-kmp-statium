/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.statium.misc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class Base64UrlNoPaddingSerializerTest {

    // Test with a String type
    private val stringSerializer = Base64UrlNoPaddingSerializer(
        serialName = "String",
        toByteArray = { it.encodeToByteArray() },
        fromByteArray = { it.decodeToString() },
    )

    // Test with an Int type
    private val intSerializer = Base64UrlNoPaddingSerializer(
        serialName = "Int",
        toByteArray = { it.toString().encodeToByteArray() },
        fromByteArray = { it.decodeToString().toInt() },
    )

    // Test with a custom data class
    @Serializable
    data class TestData(val name: String, val value: Int)

    // Create a JSON format with our custom serializer
    private val stringFormat = Json {
        serializersModule = kotlinx.serialization.modules.SerializersModule {
            contextual(String::class, stringSerializer)
        }
    }

    private val intFormat = Json {
        serializersModule = kotlinx.serialization.modules.SerializersModule {
            contextual(Int::class, intSerializer)
        }
    }

    @Test
    fun testStringSerializationDeserialization() {
        val original = "Hello, World!"
        val serialized = stringFormat.encodeToString(stringSerializer, original)

        // Verify the serialized string is a Base64 URL-encoded string
        assertEquals('"' + Base64UrlNoPadding.encode(original.encodeToByteArray()) + '"', serialized)

        // Verify deserialization works correctly
        val deserialized = stringFormat.decodeFromString(stringSerializer, serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun testIntSerializationDeserialization() {
        val original = 12345
        val serialized = intFormat.encodeToString(intSerializer, original)

        // Verify the serialized string is a Base64 URL-encoded string
        assertEquals('"' + Base64UrlNoPadding.encode(original.toString().encodeToByteArray()) + '"', serialized)

        // Verify deserialization works correctly
        val deserialized = intFormat.decodeFromString(intSerializer, serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun testCustomDataClass() {
        // Create a serializer for TestData
        val testDataSerializer = Base64UrlNoPaddingSerializer(
            serialName = "TestData",
            toByteArray = { Json.encodeToString(TestData.serializer(), it).encodeToByteArray() },
            fromByteArray = { Json.decodeFromString(TestData.serializer(), it.decodeToString()) },
        )

        val testDataFormat = Json {
            serializersModule = kotlinx.serialization.modules.SerializersModule {
                contextual(TestData::class, testDataSerializer)
            }
        }

        val original = TestData("test", 123)
        val serialized = testDataFormat.encodeToString(testDataSerializer, original)

        // Verify deserialization works correctly
        val deserialized = testDataFormat.decodeFromString(testDataSerializer, serialized)
        assertEquals(original, deserialized)
    }
}
