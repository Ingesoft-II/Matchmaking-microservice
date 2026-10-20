package com.killerprotocol.matchmaking.adapter.in.scheduler;

import com.killerprotocol.matchmaking.application.port.in.RunCycleUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CycleScheduler {
    private final RunCycleUseCase runCycle;
    public CycleScheduler(RunCycleUseCase runCycle) { this.runCycle = runCycle; }
    @Scheduled(fixedDelayString = "${matchmaking.cycle-delay-ms:2000}")
    public void tick() { runCycle.runCycle(); }
}
