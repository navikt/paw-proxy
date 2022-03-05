package no.nav.pawproxy.app

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <reified T> HttpClient.forward_post(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T =
    withContext(Dispatchers.IO) {
            request<T> {
                url(url)
                method = HttpMethod.Post
                apply(block)
            }
    }

suspend inline fun <reified T> HttpClient.get(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T =
    withContext(Dispatchers.IO) {
        request {
            url(url)
            method = HttpMethod.Get
            apply(block)
        }
    }
