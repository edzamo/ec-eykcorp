package com.eykcorp.clientes.infrastructure.adapter.in.web;

import com.eykcorp.clientes.domain.cliente.ClienteNoEncontradoException;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import com.eykcorp.clientes.domain.cliente.ValorInvalidoException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Traducción de errores de los controladores a ProblemDetail (RFC 7807). Los 401/403 de la cadena de
 * seguridad los traduce {@code ProblemaSeguridadHandler} (segundo punto de transporte, INV-15).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    ProblemDetail validacion(WebExchangeBindException error) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : error.getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), String.valueOf(campo.getDefaultMessage()));
        }
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", "La petición contiene datos inválidos");
        pd.setProperty("errores", errores);
        return pd;
    }

    @ExceptionHandler(ValorInvalidoException.class)
    ProblemDetail valorInvalido(ValorInvalidoException error) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", error.getMessage());
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    ProblemDetail noEncontrado(ClienteNoEncontradoException error) {
        return problema(HttpStatus.NOT_FOUND, "Cliente no encontrado", error.getMessage());
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    ProblemDetail duplicado(CorreoDuplicadoException error) {
        return problema(HttpStatus.CONFLICT, "Correo duplicado", error.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    ProblemDetail credencialesInvalidas(CredencialesInvalidasException error) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", error.getMessage());
    }

    /** Errores de protocolo (JSON malformado, id no numérico, ruta inexistente...): conservan su status. */
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail estadoHttp(ResponseStatusException error) {
        HttpStatusCode status = error.getStatusCode();
        String detalle = status.is4xxClientError() ? "Petición incorrecta" : "Error del servidor";
        return problema(status, HttpStatus.valueOf(status.value()).getReasonPhrase(), detalle);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception error) {
        LOG.error("Error no controlado", error);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Error interno del servidor");
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        return pd;
    }
}
