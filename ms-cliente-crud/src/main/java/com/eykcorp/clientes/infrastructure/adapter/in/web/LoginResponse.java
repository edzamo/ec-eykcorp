package com.eykcorp.clientes.infrastructure.adapter.in.web;

/** Token JWT y su vigencia en segundos. */
public record LoginResponse(String token, long expiraEn) {

    @Override
    public String toString() {
        return "LoginResponse[expiraEn=" + expiraEn + "]";
    }
}
