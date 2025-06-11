package eu.europa.ec.eudi.statium.misc

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

public class IOSClient {
    private val client: HttpClient = HttpClient(Darwin)

    public fun getClient() : HttpClient {
        return client
    }
}