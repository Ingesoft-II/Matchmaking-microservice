package com.killerprotocol.matchmaking.domain.state;

import com.killerprotocol.matchmaking.domain.model.Ticket;

public class Waiting implements TicketState {

    @Override
    public String name() {
        return "WAITING";
    }

    @Override
    public void cancelar(Ticket t) {
        t.cambiarEstado(new Cancelled());
    }

    @Override
    public void emparejar(Ticket t) {
        t.cambiarEstado(new Matched());
    }

    @Override
    public void expirar(Ticket t) {
        t.cambiarEstado(new Expired());
    }
}
