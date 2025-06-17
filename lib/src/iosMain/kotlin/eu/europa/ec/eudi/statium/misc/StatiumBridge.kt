package eu.europa.ec.eudi.statium.misc

import eu.europa.ec.eudi.statium.GetStatus
import eu.europa.ec.eudi.statium.GetStatusListToken
import eu.europa.ec.eudi.statium.Status
import eu.europa.ec.eudi.statium.StatusReference
import eu.europa.ec.eudi.statium.VerifyStatusListTokenSignature
import eu.europa.ec.eudi.statium.platformDecompress
import io.ktor.client.HttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

public class StatusChecker {

    private val scope = MainScope()

    /**
     * Checks the token status list.
     * Safe to call from iOS (uses callback instead of suspend).
     */
    public fun checkStatus(
        statusReference: StatusReference,
        atTime: String,
        callback: (StatusCheckResult) -> Unit
    ) {
        scope.launch {
            try {
                val instant = Instant.parse(atTime)
                val clock = Clock.fixed(instant)
                val getStatus = createGetStatus(clock) { HttpClient() }
                val actualStatus = getStatus.run {
                    statusReference.status(at = null).getOrThrow()
                }
                val matches = actualStatus == Status.Valid
                callback(StatusCheckResult.Success(matches))

            } catch (e: Exception) {
                callback(StatusCheckResult.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    // Create GetStatus using provided clock and HttpClient
    private fun createGetStatus(clock: Clock, httpClientFactory: () -> HttpClient): GetStatus {
        val verifySignature = VerifyStatusListTokenSignature.Ignore
        val getStatusListToken = GetStatusListToken.usingJwt(
            clock,
            httpClientFactory,
            verifySignature,
            Duration.ZERO
        )
        val decompress = platformDecompress()
        return GetStatus(getStatusListToken, decompress)
    }

    public companion object {
        public fun Clock.Companion.fixed(at: Instant): Clock = object : Clock {
            override fun now(): Instant = at
        }
    }
}
