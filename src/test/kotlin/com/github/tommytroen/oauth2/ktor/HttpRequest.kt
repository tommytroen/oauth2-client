package com.github.tommytroen.oauth2.ktor

import com.github.tommytroen.oauth2.AccessTokenResponse
import com.github.tommytroen.oauth2.GrantRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters

// TODO fetch Principal or bearer token from header and supply all methods without initialbearertoken
class Ktor(httpClient: HttpClient) {
    val tokenClient: suspend (GrantRequest) -> AccessTokenResponse = {
        httpClient.request(it.toHttpRequestBuilder())
    }
}

fun GrantRequest.toHttpRequestBuilder(): HttpRequestBuilder =
    HttpRequestBuilder().apply {
        url(this@toHttpRequestBuilder.url)
        this@toHttpRequestBuilder.headers.forEach {
            header(it.key, it.value)
        }
        method = HttpMethod.parse(this@toHttpRequestBuilder.method)
        body = FormDataContent(Parameters.build {
            this@toHttpRequestBuilder.parameters.forEach {
                append(it.key, it.value)
            }
        })
    }

internal suspend fun HttpClient.tokenRequest(
    grantRequest: GrantRequest
): String =
    submitForm(
        url = grantRequest.url,
        formParameters = Parameters.build {
            grantRequest.parameters.forEach {
                append(it.key, it.value)
            }
        }
    ) {
        grantRequest.headers.forEach {
            header(it.key, it.value)
        }
    }
