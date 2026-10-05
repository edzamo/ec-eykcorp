package com.eykcorp.clientes.contrato;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Acceso al contrato OpenAPI (fuente de verdad) empaquetado en el classpath. */
final class ContratoOpenApi {

    static final String RUTA_CLASSPATH = "/static/openapi/ms-cliente-gestion.yaml";
    static final String RUTA_HTTP = "/openapi/ms-cliente-gestion.yaml";

    private ContratoOpenApi() {
    }

    static String contenido() {
        try (InputStream in = ContratoOpenApi.class.getResourceAsStream(RUTA_CLASSPATH)) {
            if (in == null) {
                throw new IllegalStateException("No existe " + RUTA_CLASSPATH);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
