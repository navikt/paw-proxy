package no.nav.pawproxy.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <reified T> HttpClient.forwardPost(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    withContext(Dispatchers.IO) {
        request {
            url(url)
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            apply(block)
        }
    }

suspend inline fun HttpClient.forwardPostWithCustomContentType(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    withContext(Dispatchers.IO) {
        request {
            url(url)
            method = HttpMethod.Post
            apply(block)
        }
    }

suspend inline fun <reified T> HttpClient.forwardGet(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    withContext(Dispatchers.IO) {
        request {
            url(url)
            method = HttpMethod.Get
            apply(block)
        }
    }
