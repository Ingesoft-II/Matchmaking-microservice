package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.Ticket;

public interface RatingPolicy {

    boolean canMatch(Ticket a, Ticket b, long waitedSec);
}
