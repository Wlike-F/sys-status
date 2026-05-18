package com.sysstatus.server.servernode;

public record ServerCreateResponse(
        Long id,
        String registerToken,
        String installCommand
) {
}
