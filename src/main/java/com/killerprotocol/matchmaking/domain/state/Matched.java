package com.killerprotocol.matchmaking.domain.state;

public class Matched implements TicketState {

    @Override
    public String name() {
        return "MATCHED";
    }
}
