package com.github.tommytroen.oauth2.java;

import com.github.tommytroen.oauth2.BlockingOAuth2Client;
import com.github.tommytroen.oauth2.TokenExchangeGrant;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BlockingOAuth2ClientTest {

    @Disabled
    @Test
    void test() {

        var client = new BlockingOAuth2Client.Config().tokenEndpointUrl("").build();

        String result = client.withBearerToken(
                new TokenExchangeGrant("", ""),
                bearerToken -> "the result"
        );

    }

    @NotNull
    private Function1<String, String> getStringStringFunction1() {
        return bearerToken -> "sd";
    }

}
