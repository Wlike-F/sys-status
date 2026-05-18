package com.sysstatus.server.servernode;

public record RegisterTokenResponse(
        Long id,
        String registerToken,
        String installCommand
) {
}
