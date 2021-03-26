package com.github.tommytroen.oauth2

import com.nimbusds.oauth2.sdk.auth.ClientAuthentication

typealias BearerToken = String
typealias AsyncTokenClient = suspend (GrantRequest) -> AccessTokenResponse
typealias BlockingTokenClient = (GrantRequest) -> AccessTokenResponse

//todo cache
class AsyncOAuth2Client(config: Config) {
    val tokenEndpointUrl: String = config.tokenEndpointUrl.invoke()
    val clientAuthentication: ClientAuthentication = config.clientAuthConfig.invoke()
    val tokenClient: AsyncTokenClient = config.tokenClient

    suspend fun requestToken(grant: Grant) = tokenClient.invoke(grant.toGrantRequest())

    suspend fun <T> withBearerToken(grant: Grant, block: suspend (BearerToken) -> T): T =
        requestToken(grant).let { block.invoke(it.access_token) }

    private fun Grant.toGrantRequest() = GrantRequest(
        tokenEndpointUrl,
        clientAuthentication,
        grantType,
        formParams()
    )

    class Config internal constructor() {
        var tokenEndpointUrl: () -> String = {
            throw NotImplementedError("you must specify a valid token endpoint url")
        }
        var clientAuthConfig: ClientAuthConfig = {
            throw NotImplementedError("you must specify a valid client authentication config")
        }
        var tokenClient: AsyncTokenClient = {
            throw NotImplementedError("you must specify a token client function")
        }

        fun tokenEndpointUrl(tokenEndpointUrl: String): Config = apply {
            this.tokenEndpointUrl = { tokenEndpointUrl }
        }

        fun clientAuthConfig(clientAuthConfig: ClientAuthConfig): Config = apply {
            this.clientAuthConfig = clientAuthConfig
        }

        fun tokenClient(tokenClient: AsyncTokenClient): Config = apply {
            this.tokenClient = tokenClient
        }

        fun build(): AsyncOAuth2Client = AsyncOAuth2Client(this)
    }
}


class BlockingOAuth2Client(config: Config) {
    val tokenEndpointUrl: String = config.tokenEndpointUrl.invoke()
    val clientAuthentication: ClientAuthentication = config.clientAuthConfig.invoke()
    val blockingTokenClient: BlockingTokenClient = config.tokenClient

    fun requestToken(grant: Grant) = blockingTokenClient.invoke(grant.toGrantRequest())

    fun <T> withBearerToken(grant: Grant, block: (BearerToken) -> T): T =
        requestToken(grant).let {
            block.invoke(it.access_token)
        }

    private fun Grant.toGrantRequest() = GrantRequest(
        tokenEndpointUrl,
        clientAuthentication,
        grantType,
        formParams()
    )

    class Config internal constructor() {
        var tokenEndpointUrl: () -> String = {
            throw NotImplementedError("you must specify a valid token endpoint url")
        }
        var clientAuthConfig: ClientAuthConfig = {
            throw NotImplementedError("you must specify a valid client authentication config")
        }
        var tokenClient: BlockingTokenClient = {
            throw NotImplementedError("you must specify a token client function")
        }

        fun tokenEndpointUrl(tokenEndpointUrl: String): Config = apply {
            this.tokenEndpointUrl = { tokenEndpointUrl }
        }

        fun clientAuthConfig(clientAuthConfig: ClientAuthConfig): Config = apply {
            this.clientAuthConfig = clientAuthConfig
        }

        fun tokenClient(tokenClient: BlockingTokenClient): Config = apply {
            this.tokenClient = tokenClient
        }

        fun build(): BlockingOAuth2Client = BlockingOAuth2Client(this)
    }
}

fun oauth2Client(config: BlockingOAuth2Client.Config.() -> Unit): BlockingOAuth2Client = BlockingOAuth2Client.Config().apply(config).build()
fun asyncOAuth2Client(config: AsyncOAuth2Client.Config.() -> Unit): AsyncOAuth2Client = AsyncOAuth2Client.Config().apply(config).build()

suspend fun AsyncOAuth2Client.tokenExchange(initialBearerToken: BearerToken, audience: String) =
    requestToken(
        TokenExchangeGrant(initialBearerToken, audience)
    )

suspend fun AsyncOAuth2Client.onBehalfOf(initialBearerToken: BearerToken, scope: String) =
    requestToken(
        OnBehalfOfGrant(initialBearerToken, scope)
    )

suspend fun AsyncOAuth2Client.clientCredentials(scope: String) =
    requestToken(
        ClientCredentialsGrant(scope)
    )

fun BlockingOAuth2Client.tokenExchange(initialBearerToken: BearerToken, audience: String) =
    requestToken(
        TokenExchangeGrant(initialBearerToken, audience)
    )

fun BlockingOAuth2Client.onBehalfOf(initialBearerToken: BearerToken, scope: String) =
    requestToken(
        OnBehalfOfGrant(initialBearerToken, scope)
    )

fun BlockingOAuth2Client.clientCredentials(scope: String) =
    requestToken(
        ClientCredentialsGrant(scope)
    )


suspend fun <T> withBearerToken(
    asyncOAuth2Client: AsyncOAuth2Client.Config.() -> Unit,
    grant: Grant,
    block: suspend (BearerToken) -> T
) =
    AsyncOAuth2Client.Config().apply(asyncOAuth2Client).build()
        .withBearerToken(
            grant,
            block
        )

fun <T> withBearerTokenBlocking(
    oAuth2Client: BlockingOAuth2Client.Config.() -> Unit,
    grant: Grant,
    block: (BearerToken) -> T
) =
    BlockingOAuth2Client.Config().apply(oAuth2Client).build()
        .withBearerToken(
            grant,
            block
        )


