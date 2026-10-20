package com.killerprotocol.matchmaking.application.port.in;

import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;

public interface GetTicketUseCase {
    Ticket get(TicketId id);
}
