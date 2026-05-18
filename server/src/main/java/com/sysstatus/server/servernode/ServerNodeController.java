package com.sysstatus.server.servernode;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysstatus.server.shared.ApiResponse;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/servers")
public class ServerNodeController {
    private final ServerNodeService service;

    public ServerNodeController(ServerNodeService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<ServerCreateResponse> create(@Valid @RequestBody CreateServerRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping
    public ApiResponse<List<ServerNodeDto>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<ServerNodeDto> get(@PathVariable("id") long id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping("/{id}/snapshot/latest")
    public ApiResponse<JsonNode> latestSnapshot(@PathVariable("id") long id) {
        return ApiResponse.ok(service.latestSnapshot(id));
    }

    @PostMapping("/{id}/register-token")
    public ApiResponse<RegisterTokenResponse> regenerateToken(@PathVariable("id") long id) {
        return ApiResponse.ok(service.regenerateToken(id));
    }
}
