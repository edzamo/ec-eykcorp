package com.eykcorp.clientes.infrastructure.security;

/** JWT firmado y su vigencia en segundos. */
public record TokenEmitido(String token, long expiraEnSegundos) {

    @Override
    public String toString() {
        return "TokenEmitido[expiraEnSegundos=" + expiraEnSegundos + "]";
    }
}
