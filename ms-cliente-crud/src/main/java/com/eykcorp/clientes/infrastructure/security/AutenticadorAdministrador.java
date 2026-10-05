package com.eykcorp.clientes.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Valida las credenciales del único administrador. Siempre ejecuta {@code matches} (contra un hash
 * ficticio si el usuario no coincide) para no revelar la existencia del usuario por tiempo de respuesta.
 */
public class AutenticadorAdministrador {

    private final AdminProperties administrador;
    private final PasswordEncoder codificador;
    private final String hashFicticio;

    public AutenticadorAdministrador(AdminProperties administrador, PasswordEncoder codificador) {
        this.administrador = administrador;
        this.codificador = codificador;
        this.hashFicticio = codificador.encode(UUID.randomUUID().toString());
    }

    /** BCrypt es costoso y bloqueante: se ejecuta fuera del event loop. */
    public Mono<Boolean> autenticar(String usuario, String password) {
        return Mono.fromCallable(() -> verificar(usuario, password))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private boolean verificar(String usuario, String password) {
        String candidato = usuario == null ? "" : usuario;
        String clave = password == null ? "" : password;
        boolean usuarioCoincide = MessageDigest.isEqual(
                candidato.getBytes(StandardCharsets.UTF_8),
                administrador.user().getBytes(StandardCharsets.UTF_8));
        boolean passwordCoincide = codificador.matches(
                clave, usuarioCoincide ? administrador.passwordHash() : hashFicticio);
        return usuarioCoincide && passwordCoincide;
    }
}
