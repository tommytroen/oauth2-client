package com.github.tommytroen.oauth2

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost
import com.nimbusds.oauth2.sdk.auth.JWTAuthenticationClaimsSet
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.util.URLUtils
import java.sql.Date
import java.time.Duration
import java.time.Instant
import java.util.UUID

typealias ClientAuthConfig = () -> ClientAuthentication

object ClientAuth {

    class Public(private val clientId: String) : ClientAuthConfig {
        override fun invoke(): ClientAuthentication =
            object : ClientAuthentication(ClientAuthenticationMethod.NONE, ClientID(clientId)) {
                override fun applyTo(httpRequest: HTTPRequest?) {
                    httpRequest?.apply {
                        queryParameters["client_id"] = listOf(clientID.value)
                        query = URLUtils.serializeParameters(queryParameters)
                    }
                }
            }
    }

    class Basic(
        private val clientId: String,
        private val clientSecret: String
    ) : ClientAuthConfig {
        override fun invoke(): ClientSecretBasic = ClientSecretBasic(ClientID(clientId), Secret(clientSecret))
    }

    class Post(
        private val clientId: String,
        private val clientSecret: String
    ) : ClientAuthConfig {
        override fun invoke(): ClientSecretPost = ClientSecretPost(ClientID(clientId), Secret(clientSecret))
    }

    class PrivateKeyJwt @JvmOverloads constructor(
        val clientId: String,
        val audience: String,
        val rsaKey: RSAKey,
        val expiry: Duration = Duration.ofSeconds(120),
        val includeNbfAndIat: Boolean = true
    ) : ClientAuthConfig {

        override fun invoke(): PrivateKeyJWT = build()

        private fun build(): PrivateKeyJWT =
            build(
                jwtClaimsSet = JWTClaimsSet.Builder()
                    .apply {
                        val now = Instant.now()
                        subject(clientId)
                        issuer(clientId)
                        audience(audience)
                        expirationTime(Date.from(now.plusSeconds(expiry.toSeconds())))
                        claim("jti", UUID.randomUUID().toString())
                        if (includeNbfAndIat) {
                            notBeforeTime(Date.from(now))
                            issueTime(Date.from(now))
                        }
                    }.build(),
                rsaKey = rsaKey
            )

        private fun build(jwtClaimsSet: JWTClaimsSet, rsaKey: RSAKey): PrivateKeyJWT =
            PrivateKeyJWT(
                JWTAuthenticationClaimsSet.parse(jwtClaimsSet),
                JWSAlgorithm.RS256,
                rsaKey.toRSAPrivateKey(),
                rsaKey.keyID,
                null
            )
    }
}
