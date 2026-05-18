package com.sysstatus.server.agent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysstatus.common.agent.AgentHeartbeatRequest;
import com.sysstatus.common.agent.AgentRegisterRequest;
import com.sysstatus.common.agent.AgentRegisterResponse;
import com.sysstatus.common.agent.AgentSnapshotRequest;
import com.sysstatus.common.agent.AgentStatusResponse;
import com.sysstatus.server.shared.ApiResponse;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ApiResponse<AgentRegisterResponse> register(@RequestBody AgentRegisterRequest request) {
        return ApiResponse.ok(service.register(request));
    }

    @PostMapping("/heartbeat")
    public ApiResponse<AgentStatusResponse> heartbeat(@RequestBody AgentHeartbeatRequest request) {
        return ApiResponse.ok(service.heartbeat(request));
    }

    @PostMapping("/snapshots")
    public ApiResponse<AgentStatusResponse> snapshot(@RequestBody AgentSnapshotRequest request) {
        return ApiResponse.ok(service.snapshot(request));
    }
}
