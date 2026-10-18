package com.killerprotocol.matchmaking.adapter.in.web;

import com.killerprotocol.matchmaking.application.port.in.CancelUseCase;
import com.killerprotocol.matchmaking.application.port.in.FindMatchUseCase;
import com.killerprotocol.matchmaking.application.port.in.GetTicketUseCase;
import com.killerprotocol.matchmaking.application.port.in.RunCycleUseCase;
import com.killerprotocol.matchmaking.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {
    private final FindMatchUseCase findMatch;
    private final RunCycleUseCase runCycle;
    private final GetTicketUseCase getTicket;
    private final CancelUseCase cancel;

    public MatchmakingController(FindMatchUseCase findMatch, RunCycleUseCase runCycle,
            GetTicketUseCase getTicket, CancelUseCase cancel) {
        this.findMatch = findMatch;
        this.runCycle = runCycle;
        this.getTicket = getTicket;
        this.cancel = cancel;
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketView enqueue(@Valid @RequestBody FindMatchRequest request) {
        Party party = new Party(new PartyId("party-" + java.util.UUID.randomUUID()),
                request.playerIds().stream().map(PlayerId::new).toList());
        return TicketView.from(findMatch.findMatch(party, request.mode(), request.region(), request.latencyMs()));
    }

    @GetMapping("/tickets/{id}")
    public TicketView get(@PathVariable String id) {
        return TicketView.from(getTicket.get(new TicketId(id)));
    }

    @DeleteMapping("/tickets/{id}")
    public TicketView cancel(@PathVariable String id) {
        return TicketView.from(cancel.cancel(new TicketId(id)));
    }

    @PostMapping("/cycle")
    public List<Partida> cycle() { return runCycle.runCycle(); }

    public record FindMatchRequest(@NotEmpty @Size(max = 5) List<@NotBlank String> playerIds,
                                   @NotNull GameMode mode, @NotBlank String region,
                                   @Min(0) int latencyMs) { }

    public record TicketView(String id, String status, Integer rating, String partidaId) {
        static TicketView from(Ticket ticket) {
            return new TicketView(ticket.id().value(), ticket.estado().name(), ticket.rating(),
                    ticket.partidaId() == null ? null : ticket.partidaId().value());
        }
    }
}
