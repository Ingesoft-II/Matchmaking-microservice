package com.killerprotocol.matchmaking.domain.state;

public class Expired implements TicketState {

    @Override
    public String name() {
        return "EXPIRED";
    }
}
