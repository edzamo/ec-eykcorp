package com.eykcorp.clientes.infrastructure.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Constantes y fábricas de tokens para pruebas. */
public final class TokensDePrueba {

    public static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres!";
    public static final String OTRO_SECRETO = "otro-secreto-de-prueba-distinto-de-32+";
    public static final String USUARIO = "admin";
    public static final String PASSWORD = "clave-de-prueba";
    /** BCrypt coste 4 de {@link #PASSWORD}. */
    public static final String HASH = "$2a$04$SMY3hS5UEFGA1poLZL4tWeF.7G.BGLcKdOj1kZ1tg6h665CDQt0Bq";
    public static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");

    private TokensDePrueba() {
    }

    public static JwtProperties propiedades(String secreto, String emisor) {
        return new JwtProperties(secreto, 30, emisor);
    }

    public static JwtService servicio(String secreto, String emisor, Instant instante) {
        return new JwtService(propiedades(secreto, emisor), Clock.fixed(instante, ZoneOffset.UTC));
    }

    /** Token vigente (emitido con el reloj real) para llamar a la API desde las pruebas. */
    public static String tokenVigente() {
        return new JwtService(propiedades(SECRETO, "ms-cliente-gestion"), Clock.systemUTC())
                .emitir(USUARIO).token();
    }
}
