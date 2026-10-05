package com.eykcorp.clientes.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades del JWT. El secreto llega por {@code JWT_SECRET}; sin secreto válido (mínimo 32
 * caracteres) la aplicación falla al arrancar.
 */
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, long expirationMinutes, String issuer) {

    static final int LONGITUD_MINIMA_SECRETO = 32;

    public JwtProperties {
        if (secret == null || secret.isBlank() || secret.length() < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalArgumentException(
                    "JWT_SECRET es obligatorio y debe tener al menos " + LONGITUD_MINIMA_SECRETO + " caracteres");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MINUTES debe ser mayor que cero");
        }
    }

    /** Evita volcar el secreto en logs o trazas. */
    @Override
    public String toString() {
        return "JwtProperties[expirationMinutes=" + expirationMinutes + ", issuer=" + issuer + "]";
    }
}
