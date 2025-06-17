package eu.europa.ec.eudi.statium.misc

public sealed class StatusCheckResult {
    public data class Success(val match: Boolean) : StatusCheckResult()
    public data class Failure(val message: String) : StatusCheckResult()
}

