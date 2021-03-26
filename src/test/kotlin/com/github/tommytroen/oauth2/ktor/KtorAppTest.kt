package com.github.tommytroen.oauth2.ktor

import com.github.tommytroen.oauth2.AccessTokenResponse
import com.github.tommytroen.oauth2.ClientAuth
import com.github.tommytroen.oauth2.GrantRequest
import com.github.tommytroen.oauth2.TokenExchangeGrant
import com.github.tommytroen.oauth2.asyncOAuth2Client
import com.github.tommytroen.oauth2.withBearerToken
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.request.ApplicationRequest
import io.ktor.routing.get
import io.ktor.routing.routing

class KtorAppTest {

    val httpClient: HttpClient = HttpClient()
    val func: suspend (GrantRequest) -> AccessTokenResponse = {
        httpClient.request(it.toHttpRequestBuilder())
    }

    fun Application.app() {

        routing {
            install(Authentication) {
                jwt {

                }
            }
            get {

                val client = asyncOAuth2Client {
                    this.tokenEndpointUrl = { "http://localhost/token" }
                    this.clientAuthConfig = ClientAuth.Basic("", "")
                    this.tokenClient = func
                }

                client.withBearerToken(TokenExchangeGrant("", "")) {

                }

                withBearerToken(
                    {
                        tokenEndpointUrl = {""}
                    },
                    TokenExchangeGrant("", "")
                ) {

                }
                /*
                withBearerToken(
                    {
                        tokenEndpointUrl = {""}
                        clientAuthConfig = ClientAuth.Basic("", "")
                        tokenClient = { httpClient.request(it.toHttpRequestBuilder()) }
                    },
                    TokenExchangeGrant("", "")
                ) {

                }*/
            }
        }
    }


    fun ApplicationRequest.bearerToken(): String =
        this.headers["Authorization"]?.split("Bearer ")?.takeIf { it.size == 2 }?.get(1)
            ?: throw RuntimeException("no bearertoken found in auth header")
}
