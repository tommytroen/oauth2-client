package com.github.tommytroen.oauth2

import com.nimbusds.common.contenttype.ContentType
import com.nimbusds.oauth2.sdk.GrantType
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.util.URLUtils
import java.net.URI

open class Grant(
    val grantType: GrantType,
    private val params: Map<String, String> = emptyMap()
) {
    fun formParams(): Map<String, String> =
        params.toMutableMap().apply {
            putIfAbsent(OAuth2ParameterNames.GRANT_TYPE, grantType.value)
        }
}

data class TokenExchangeGrant(
    val initialBearerToken: String,
    val audience: String
) : Grant(
    grantType = GrantType("urn:ietf:params:oauth:grant-type:token-exchange"),
    params = mapOf(
        OAuth2ParameterNames.SUBJECT_TOKEN_TYPE to "urn:ietf:params:oauth:token-type:jwt",
        OAuth2ParameterNames.SUBJECT_TOKEN to initialBearerToken,
        OAuth2ParameterNames.AUDIENCE to audience
    )
)

data class OnBehalfOfGrant(
    val initialBearerToken: String,
    val scope: String
) : Grant(
    grantType = GrantType.JWT_BEARER,
    params = mapOf(
        OAuth2ParameterNames.SCOPE to scope,
        OAuth2ParameterNames.REQUESTED_TOKEN_USE to "on_behalf_of",
        OAuth2ParameterNames.ASSERTION to initialBearerToken
    )
)

data class ClientCredentialsGrant(val scope: String) : Grant(
    grantType = GrantType.CLIENT_CREDENTIALS,
    params = mapOf(
        OAuth2ParameterNames.SCOPE to scope,
    )
)

data class GrantRequest(
    private val tokenEndpointUrl: String,
    private val clientAuth: ClientAuthentication,
    private val grantType: GrantType,
    private val params: Map<String, String>
) {
    private val httpRequest: HTTPRequest = createHttpRequest()

    val url: String = tokenEndpointUrl
    val headers: MutableMap<String, MutableList<String>> = httpRequest.headerMap
    val method: String = httpRequest.method.name
    val parameters: MutableMap<String, String> = httpRequest.queryParameters.toSingleValueMap()

    private fun createHttpRequest() =
        HTTPRequest(
            HTTPRequest.Method.POST,
            tokenEndpointUrl.uriWithoutQuery()
        ).apply {
            entityContentType = ContentType.APPLICATION_URLENCODED
        }.also {
            clientAuth.applyTo(it)
        }.also {
            val map = it.queryParameters.apply { putAll(params.toMultiValueMap()) }
            map.putIfAbsent(OAuth2ParameterNames.GRANT_TYPE, listOf(grantType.value))
            it.query = URLUtils.serializeParameters(map)
        }

    private fun Map<String, String>.toMultiValueMap(): Map<String, List<String>> =
        entries.associate { it.key to listOf(it.value) }

    private fun Map<String, List<String>>.toSingleValueMap(): MutableMap<String, String> =
        entries.filterNot { it.value.isEmpty() }
            .associate { it.key to it.value.first() }
            .toMutableMap()

    private fun String.uriWithoutQuery(): URI = URI.create(split("?")[0])
}

data class AccessTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String
)

object OAuth2ParameterNames {
    const val GRANT_TYPE = "grant_type"
    const val CLIENT_ID = "client_id"
    const val CLIENT_SECRET = "client_secret"
    const val ASSERTION = "assertion"
    const val REQUESTED_TOKEN_USE = "requested_token_use"
    const val SCOPE = "scope"
    const val CLIENT_ASSERTION_TYPE = "client_assertion_type"
    const val CLIENT_ASSERTION = "client_assertion"
    const val SUBJECT_TOKEN_TYPE = "subject_token_type"
    const val SUBJECT_TOKEN = "subject_token"
    const val AUDIENCE = "audience"
    const val RESOURCE = "resource"
}
