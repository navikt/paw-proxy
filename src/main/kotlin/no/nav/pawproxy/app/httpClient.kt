package no.nav.pawproxy.app

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

suspend inline fun <reified T> HttpClient.post(url: URL): T = withContext(Dispatchers.IO) {
    request {
        url("$url")
        method = HttpMethod.Post
    }
}

suspend inline fun <reified T> HttpClient.get(url: String): T = withContext(Dispatchers.IO) {
    request {
        url(url)
        method = HttpMethod.Get
    }
}
