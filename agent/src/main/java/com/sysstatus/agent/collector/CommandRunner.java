package com.sysstatus.agent.collector;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface CommandRunner {
    String run(List<String> command) throws IOException, InterruptedException;
}
