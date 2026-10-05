package com.eykcorp.clientes.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credenciales del administrador: usuario y hash BCrypt (nunca la contraseña en claro). */
@ConfigurationProperties("app.security.admin")
public record AdminProperties(String user, String passwordHash) {

    @Override
    public String toString() {
        return "AdminProperties[user=" + user + "]";
    }
}
