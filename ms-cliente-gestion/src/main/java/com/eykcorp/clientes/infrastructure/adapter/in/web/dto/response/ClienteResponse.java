package com.eykcorp.clientes.infrastructure.adapter.in.web.dto.response;

import java.time.Instant;

public record ClienteResponse(
        Long id, String nombres, String apellidos, String correo, String telefono, Instant fechaCreacion) {

    /** INV-17: sin datos personales. */
    @Override
    public String toString() {
        return "ClienteResponse[id=" + id + "]";
    }
}
