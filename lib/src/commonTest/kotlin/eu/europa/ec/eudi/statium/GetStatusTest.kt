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

import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class GetStatusTest {

    @Test
    @Ignore
    fun testGetTokenStatusList() =
        doTest(
            expectedStatus = Status.Valid,
            statusReference = StatusReference(
                index = StatusIndex(90),
                uri = "https://issuer.eudiw.dev/token_status_list/FC/eu.europa.ec.eudi.pid.1/372baa1f-2145-4e99-93df-7b6e9746db05",
            ),
            Clock.fixed(Instant.parse("2025-03-27T13:02:23Z")),
        )
}

fun Clock.Companion.fixed(at: Instant): Clock = object : Clock {
    override fun now(): Instant = at
}

private fun doTest(expectedStatus: Status, statusReference: StatusReference, clock: Clock = Clock.System) = runTest {
    with(getStatus(clock) { HttpClient() }) {
        val status = statusReference.status(at = null).getOrThrow()
        assertEquals(expectedStatus, status)
    }
}

private fun CoroutineScope.getStatus(clock: Clock, httpClientFactory: () -> HttpClient): GetStatus {
    val verifySignature = VerifyStatusListTokenSignature.Ignore
    val getStatusListToken = GetStatusListToken.usingJwt(clock, httpClientFactory, verifySignature, kotlin.time.Duration.ZERO)
    val decompress = platformDecompress(coroutineContext)
    return GetStatus(getStatusListToken, decompress)
}
