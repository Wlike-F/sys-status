package com.sysstatus.server.servernode;

import jakarta.validation.constraints.NotBlank;

public record CreateServerRequest(
        @NotBlank String name,
        @NotBlank String host,
        @NotBlank String osType,
        String gpuType,
        String location,
        String description
) {
}
