package com.sysstatus.server.shared;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {
    private static final int TOKEN_BYTES = 24;

    private final SecureRandom secureRandom = new SecureRandom();

    public String nextToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
