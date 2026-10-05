package com.eykcorp.clientes.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.TestPropertySource;

/**
 * Propiedades de seguridad solo para pruebas (valores ficticios, hash BCrypt de coste 4 de
 * {@link TokensDePrueba#PASSWORD}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestPropertySource(properties = {
        "app.security.jwt.secret=" + TokensDePrueba.SECRETO,
        "app.security.jwt.expiration-minutes=30",
        "app.security.jwt.issuer=ms-cliente-crud",
        "app.security.admin.user=" + TokensDePrueba.USUARIO,
        "app.security.admin.password-hash=" + TokensDePrueba.HASH
})
public @interface PropiedadesDeSeguridadDePrueba {
}
