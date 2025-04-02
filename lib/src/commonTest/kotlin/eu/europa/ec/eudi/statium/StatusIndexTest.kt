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
package eu.europa.ec.eudi.statium

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatusIndexTest {

    @Test
    fun testCreationWithPositiveValue() {
        val value = 42
        val statusIndex = StatusIndex(value)
        assertEquals(value, statusIndex.value)
    }

    @Test
    fun testCreationWithZeroValue() {
        val value = 0
        val statusIndex = StatusIndex(value)
        assertEquals(value, statusIndex.value)
    }

    @Test
    fun testCreationWithNegativeValueFails() {
        val value = -1
        assertFailsWith<IllegalArgumentException> {
            StatusIndex(value)
        }
    }

    @Test
    fun testToString() {
        val value = 123
        val statusIndex = StatusIndex(value)
        assertEquals(value.toString(), statusIndex.toString())
    }
}
