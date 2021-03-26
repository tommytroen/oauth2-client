package com.github.tommytroen.oauth2

import com.nimbusds.oauth2.sdk.GrantType
import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GrantRequestTest {

    @Test
    fun `doh`(){
        GrantRequest(
            tokenEndpointUrl = "https://tokenstuff/token?param1=value1",
            clientAuth = ClientAuth.Basic("client1", "secret").invoke(),
            grantType = GrantType.CLIENT_CREDENTIALS,
            params = mapOf(
                "scope" to "scope1"
            )
        ).asClue {
            it.headers shouldContainExactly mapOf(
                "Authorization" to listOf("Basic Y2xpZW50MTpzZWNyZXQ="),
                "Content-Type" to listOf("application/x-www-form-urlencoded; charset=UTF-8")
            )
            it.method shouldBe "POST"
            it.parameters shouldContainExactly mapOf(
                "grant_type" to "client_credentials",
                "scope" to "scope1"
            )
        }
    }

}
