package com.killerprotocol.matchmaking.domain.state;

public class Cancelled implements TicketState {

    @Override
    public String name() {
        return "CANCELLED";
    }
}
