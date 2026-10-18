package com.killerprotocol.matchmaking.domain.state;

import com.killerprotocol.matchmaking.domain.exception.TransicionInvalidaException;
import com.killerprotocol.matchmaking.domain.model.Ticket;

public interface TicketState {

    String name();

    default void cancelar(Ticket t) {
        throw new TransicionInvalidaException(name(), "cancelar");
    }

    default void emparejar(Ticket t) {
        throw new TransicionInvalidaException(name(), "emparejar");
    }

    default void expirar(Ticket t) {
        throw new TransicionInvalidaException(name(), "expirar");
    }

    static TicketState fromName(String n) {
        if (n == null) {
            throw new IllegalArgumentException("El nombre del estado no puede ser nulo.");
        }
        return switch (n) {
            case "WAITING" -> new Waiting();
            case "MATCHED" -> new Matched();
            case "CANCELLED" -> new Cancelled();
            case "EXPIRED" -> new Expired();
            default -> throw new IllegalArgumentException("Estado de ticket desconocido: " + n);
        };
    }
}
