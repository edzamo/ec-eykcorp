package com.eykcorp.clientes.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

/** Constantes y fábricas de tokens para pruebas. */
public final class TokensDePrueba {

    public static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres!";
    public static final String OTRO_SECRETO = "otro-secreto-de-prueba-distinto-de-32+";
    public static final String USUARIO = "admin";
    public static final String PASSWORD = "clave-de-prueba";
    /** BCrypt coste 4 de {@link #PASSWORD}. */
    public static final String HASH = "$2a$04$SMY3hS5UEFGA1poLZL4tWeF.7G.BGLcKdOj1kZ1tg6h665CDQt0Bq";
    public static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");

    public static final String AUDIENCIA = "ms-cliente-presentacion";

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

    /** Token firmado con la clave correcta y claims válidos, con la audiencia indicada (nula = sin {@code aud}). */
    public static String tokenConAudiencia(String audiencia, Instant instante) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject(USUARIO).issuer("ms-cliente-gestion")
                    .issueTime(Date.from(instante)).expirationTime(Date.from(instante.plusSeconds(900)));
            if (audiencia != null) {
                claims.audience(audiencia);
            }
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
            jwt.sign(new MACSigner(SECRETO.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
