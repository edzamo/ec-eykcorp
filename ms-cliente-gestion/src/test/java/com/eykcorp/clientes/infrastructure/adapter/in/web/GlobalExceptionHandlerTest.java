package com.eykcorp.clientes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.ClienteNoEncontradoException;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import com.eykcorp.clientes.domain.cliente.ValorInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void valor_invalido_debe_ser_400() {
        ProblemDetail pd = handler.valorInvalido(new ValorInvalidoException("El correo no tiene un formato válido"));

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getDetail()).isEqualTo("El correo no tiene un formato válido");
    }

    @Test
    void cliente_no_encontrado_debe_ser_404() {
        ProblemDetail pd = handler.noEncontrado(new ClienteNoEncontradoException(9L));

        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("Cliente no encontrado");
    }

    @Test
    void correo_duplicado_debe_ser_409() {
        ProblemDetail pd = handler.duplicado(new CorreoDuplicadoException());

        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Correo duplicado");
    }

    @Test
    void error_de_estado_http_debe_conservar_su_status() {
        ProblemDetail pd = handler.estadoHttp(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThat(pd.getStatus()).isEqualTo(404);
    }

    @Test
    void violacion_de_integridad_de_la_restriccion_de_correo_debe_ser_409_sin_filtrar_detalles() {
        ProblemDetail pd = handler.integridad(new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uk_clientes_correo\" "
                        + "Detail: Key (correo)=(ana@example.com) already exists"));

        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Correo duplicado");
        assertThat(pd.toString()).doesNotContain("ana@example.com").doesNotContain("uk_clientes_correo");
    }

    @Test
    void violacion_de_integridad_de_otra_restriccion_debe_ser_500_generico() {
        ProblemDetail pd = handler.integridad(new DataIntegrityViolationException("null value in column \"nombres\""));

        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.toString()).doesNotContain("nombres");
    }

    @Test
    void error_de_estado_http_con_codigo_no_estandar_no_debe_lanzar() {
        ProblemDetail pd = handler.estadoHttp(new ResponseStatusException(HttpStatusCode.valueOf(499)));

        assertThat(pd.getStatus()).isEqualTo(499);
    }

    @Test
    void error_inesperado_debe_ser_500_sin_filtrar_detalles_internos() {
        ProblemDetail pd = handler.inesperado(new IllegalStateException("password=secreto jdbc://interno"));

        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.getDetail()).isEqualTo("Error interno del servidor");
        assertThat(pd.toString()).doesNotContain("secreto");
    }
}
