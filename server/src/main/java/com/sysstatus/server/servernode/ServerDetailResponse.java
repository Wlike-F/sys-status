package com.sysstatus.server.servernode;

public record ServerDetailResponse(
        ServerNodeDto server,
        ServerSnapshotDetail snapshot
) {
}
