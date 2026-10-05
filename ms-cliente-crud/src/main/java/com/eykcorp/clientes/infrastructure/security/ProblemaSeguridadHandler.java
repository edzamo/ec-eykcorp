package com.eykcorp.clientes.infrastructure.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Traduce 401/403 de Spring Security a {@link ProblemDetail}. Es un segundo punto de traducción de
 * errores de transporte (INV-15): estos rechazos ocurren en la cadena de filtros, antes de llegar a
 * los controladores, por lo que {@code GlobalExceptionHandler} no los ve. Mantiene el mismo formato.
 */
class ProblemaSeguridadHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    ProblemaSeguridadHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException error) {
        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return escribir(exchange, HttpStatus.UNAUTHORIZED, "No autenticado", "Autenticación requerida");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException error) {
        return escribir(exchange, HttpStatus.FORBIDDEN, "Acceso denegado", "No tiene permisos para este recurso");
    }

    private Mono<Void> escribir(ServerWebExchange exchange, HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        var respuesta = exchange.getResponse();
        respuesta.setStatusCode(estado);
        respuesta.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        try {
            byte[] cuerpo = objectMapper.writeValueAsBytes(problema);
            return respuesta.writeWith(Mono.just(respuesta.bufferFactory().wrap(cuerpo)));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
