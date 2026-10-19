package com.killerprotocol.matchmaking.config;

import com.killerprotocol.matchmaking.application.port.in.CancelUseCase;
import com.killerprotocol.matchmaking.application.port.in.FindMatchUseCase;
import com.killerprotocol.matchmaking.application.port.in.GetTicketUseCase;
import com.killerprotocol.matchmaking.application.port.in.ReconnectUseCase;
import com.killerprotocol.matchmaking.application.port.in.RunCycleUseCase;
import com.killerprotocol.matchmaking.application.port.out.EventPublisher;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.application.port.out.RatingProvider;
import com.killerprotocol.matchmaking.application.port.out.ServerAllocator;
import com.killerprotocol.matchmaking.application.service.CancelService;
import com.killerprotocol.matchmaking.application.service.FindMatchService;
import com.killerprotocol.matchmaking.application.service.GetTicketService;
import com.killerprotocol.matchmaking.application.service.MatchBuilder;
import com.killerprotocol.matchmaking.application.service.ReconnectService;
import com.killerprotocol.matchmaking.application.service.RunCycleService;
import com.killerprotocol.matchmaking.domain.builder.DefaultPartidaBuilderFactory;
import com.killerprotocol.matchmaking.domain.builder.PartidaBuilderFactory;
import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.policy.ConfiguredRatingPolicyFactory;
import com.killerprotocol.matchmaking.domain.policy.PolicySettings;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicies;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicy;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicyCreator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchmakingConfig {

    @Bean
    FindMatchUseCase findMatchUseCase(MatchmakingStore store, RatingProvider ratings) {
        return new FindMatchService(store, ratings);
    }

    @Bean
    CancelUseCase cancelUseCase(MatchmakingStore store) {
        return new CancelService(store);
    }

    @Bean
    GetTicketUseCase getTicketUseCase(MatchmakingStore store) {
        return new GetTicketService(store);
    }

    @Bean
    ReconnectUseCase reconnectUseCase(MatchmakingStore store) {
        return new ReconnectService(store);
    }

    @Bean
    MatchBuilder matchBuilder() {
        return new MatchBuilder();
    }

    @Bean
    PartidaBuilderFactory partidaBuilderFactory() {
        return new DefaultPartidaBuilderFactory();
    }

    @Bean
    RunCycleUseCase runCycleUseCase(MatchmakingStore store, ServerAllocator servers, EventPublisher events,
                                    RatingPolicies policies, MatchBuilder matchBuilder,
                                    PartidaBuilderFactory builderFactory,
                                    @Value("${matchmaking.maps.ranked:Bunker,Refinery,Terminal}") List<String> rankedMaps,
                                    @Value("${matchmaking.maps.arcade:Arena,Docks}") List<String> arcadeMaps) {
        return new RunCycleService(store, servers, events, policies, matchBuilder, builderFactory, rankedMaps, arcadeMaps);
    }

    @Bean
    RatingPolicies ratingPolicies(
            @Value("${matchmaking.policy.ranked.base-gap:100}") int baseGap,
            @Value("${matchmaking.policy.ranked.growth-per-sec:2.0}") double growthPerSec,
            @Value("${matchmaking.policy.arcade.max-latency-ms:120}") int maxLatencyMs) {
        PolicySettings settings = new PolicySettings(
                new PolicySettings.RankedSettings(baseGap, growthPerSec),
                new PolicySettings.ArcadeSettings(maxLatencyMs));
        RatingPolicyCreator factory = new ConfiguredRatingPolicyFactory(settings);
        Map<GameMode, RatingPolicy> policies = new EnumMap<>(GameMode.class);
        for (GameMode mode : GameMode.values()) {
            policies.put(mode, factory.create(mode));
        }
        return new RatingPolicies(policies);
    }
}
