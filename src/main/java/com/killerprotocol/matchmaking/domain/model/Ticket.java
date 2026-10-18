package com.killerprotocol.matchmaking.domain.model;

import com.killerprotocol.matchmaking.domain.state.TicketState;
import com.killerprotocol.matchmaking.domain.state.Waiting;

import java.time.Duration;
import java.time.Instant;

public class Ticket {

    private final TicketId id;
    private final Party party;
    private final GameMode mode;
    private final String region;
    private final int latencyMs;
    private final Instant createdAt;
    private Integer rating;
    private TicketState estado = new Waiting();
    private PartidaId partidaId;
    private long version;

    public Ticket(TicketId id, Party party, GameMode mode, String region, int latencyMs, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("El ticket requiere un identificador.");
        }
        if (party == null) {
            throw new IllegalArgumentException("El ticket requiere un party.");
        }
        if (mode == null) {
            throw new IllegalArgumentException("El ticket requiere un modo de juego.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("El ticket requiere una región.");
        }
        if (latencyMs < 0) {
            throw new IllegalArgumentException("La latencia no puede ser negativa.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("El ticket requiere el instante de creación.");
        }
        this.id = id;
        this.party = party;
        this.mode = mode;
        this.region = region;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
    }

    public void cancelar() {
        estado.cancelar(this);
    }

    public void emparejar(PartidaId pid) {
        if (pid == null) {
            throw new IllegalArgumentException("Falta el identificador de la partida para emparejar.");
        }
        estado.emparejar(this);
        this.partidaId = pid;
    }

    public void expirar() {
        estado.expirar(this);
    }

    public void cambiarEstado(TicketState nuevo) {
        this.estado = nuevo;
    }

    public void asignarRating(int nuevoRating) {
        this.rating = nuevoRating;
    }

    public long waitedSec(Instant now) {
        return Duration.between(createdAt, now).toSeconds();
    }

    public TicketId id() {
        return id;
    }

    public Party party() {
        return party;
    }

    public GameMode mode() {
        return mode;
    }

    public String region() {
        return region;
    }

    public int latencyMs() {
        return latencyMs;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Integer rating() {
        return rating;
    }

    public TicketState estado() {
        return estado;
    }

    public PartidaId partidaId() {
        return partidaId;
    }

    public long version() {
        return version;
    }
}
