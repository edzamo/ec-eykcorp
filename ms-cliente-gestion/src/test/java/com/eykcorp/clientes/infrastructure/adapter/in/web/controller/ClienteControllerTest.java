package com.eykcorp.clientes.infrastructure.adapter.in.web.controller;

import com.eykcorp.clientes.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.eykcorp.clientes.infrastructure.adapter.in.web.mapper.ClienteWebMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.eykcorp.clientes.application.port.in.ActualizarClienteUseCase;
import com.eykcorp.clientes.application.port.in.CrearClienteUseCase;
import com.eykcorp.clientes.application.command.DatosCliente;
import com.eykcorp.clientes.application.port.in.EliminarClienteUseCase;
import com.eykcorp.clientes.application.port.in.ListarClientesUseCase;
import com.eykcorp.clientes.application.port.in.ObtenerClienteUseCase;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.ClienteNoEncontradoException;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import com.eykcorp.clientes.domain.cliente.Telefono;
import com.eykcorp.clientes.domain.cliente.ValorInvalidoException;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.SecurityConfig;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(ClienteController.class)
@Import({ClienteWebMapper.class, GlobalExceptionHandler.class, SecurityConfig.class})
@PropiedadesDeSeguridadDePrueba
class ClienteControllerTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private static final Cliente ANA = Cliente.crear(1L, "Ana", "Pérez", Correo.de("ana@example.com"),
            Telefono.de("0991234567"), AHORA);
    private static final Map<String, Object> VALIDO = Map.of(
            "nombres", "Ana", "apellidos", "Pérez", "correo", "ana@example.com", "telefono", "0991234567");

    @Autowired
    private WebTestClient webSinToken;
    private WebTestClient web;
    @MockitoBean
    private CrearClienteUseCase crear;
    @MockitoBean
    private ListarClientesUseCase listar;
    @MockitoBean
    private ObtenerClienteUseCase obtener;
    @MockitoBean
    private ActualizarClienteUseCase actualizar;
    @MockitoBean
    private EliminarClienteUseCase eliminar;

    @BeforeEach
    void autenticar() {
        web = webSinToken.mutate().defaultHeaders(h -> h.setBearerAuth(TokensDePrueba.tokenVigente())).build();
    }

    @Test
    void post_valido_debe_responder_201_con_location() {
        when(crear.crear(any(DatosCliente.class))).thenReturn(Mono.just(ANA));

        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/clientes/1")
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.correo").isEqualTo("ana@example.com")
                .jsonPath("$.fechaCreacion").isEqualTo("2026-10-05T15:30:00Z");
    }

    @Test
    void post_con_campos_invalidos_debe_responder_400_con_errores_por_campo() {
        Map<String, Object> invalido = Map.of(
                "nombres", " ", "apellidos", "x".repeat(101), "correo", "no-es-correo", "telefono", "abc");

        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(invalido)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errores.nombres").exists()
                .jsonPath("$.errores.apellidos").exists()
                .jsonPath("$.errores.correo").exists()
                .jsonPath("$.errores.telefono").exists();
    }

    @Test
    void post_sin_telefono_debe_ser_valido() {
        when(crear.crear(any(DatosCliente.class))).thenReturn(Mono.just(ANA));

        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nombres", "Ana", "apellidos", "Pérez", "correo", "ana@example.com"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void post_con_json_malformado_debe_responder_400() {
        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue("{no-json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void post_con_valor_invalido_del_dominio_debe_responder_400() {
        when(crear.crear(any(DatosCliente.class))).thenReturn(Mono.error(new ValorInvalidoException("inválido")));

        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void post_con_correo_duplicado_debe_responder_409_con_problem_detail() {
        when(crear.crear(any(DatosCliente.class))).thenReturn(Mono.error(new CorreoDuplicadoException()));

        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Correo duplicado")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.instance").isEqualTo("/clientes");
    }

    @Test
    void get_lista_debe_responder_200() {
        when(listar.listar()).thenReturn(Flux.just(ANA));

        web.get().uri("/clientes").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].nombres").isEqualTo("Ana");
    }

    @Test
    void get_por_id_debe_responder_200() {
        when(obtener.obtener(1L)).thenReturn(Mono.just(ANA));

        web.get().uri("/clientes/1").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(1);
    }

    @Test
    void get_por_id_inexistente_debe_responder_404() {
        when(obtener.obtener(9L)).thenReturn(Mono.error(new ClienteNoEncontradoException(9L)));

        web.get().uri("/clientes/9").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void get_con_id_no_numerico_debe_responder_400() {
        web.get().uri("/clientes/abc").exchange().expectStatus().isBadRequest();
    }

    @Test
    void put_valido_debe_responder_200() {
        when(actualizar.actualizar(eq(1L), any(DatosCliente.class))).thenReturn(Mono.just(ANA));

        web.put().uri("/clientes/1").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(1);
    }

    @Test
    void put_invalido_debe_responder_400() {
        web.put().uri("/clientes/1").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nombres", "", "apellidos", "Pérez", "correo", "ana@example.com"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void put_inexistente_debe_responder_404() {
        when(actualizar.actualizar(eq(9L), any(DatosCliente.class)))
                .thenReturn(Mono.error(new ClienteNoEncontradoException(9L)));

        web.put().uri("/clientes/9").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void put_con_correo_de_otro_debe_responder_409() {
        when(actualizar.actualizar(eq(1L), any(DatosCliente.class)))
                .thenReturn(Mono.error(new CorreoDuplicadoException()));

        web.put().uri("/clientes/1").contentType(MediaType.APPLICATION_JSON).bodyValue(VALIDO)
                .exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void delete_debe_responder_204() {
        when(eliminar.eliminar(1L)).thenReturn(Mono.empty());

        web.delete().uri("/clientes/1").exchange().expectStatus().isNoContent();
    }

    @Test
    void delete_inexistente_debe_responder_404() {
        when(eliminar.eliminar(9L)).thenReturn(Mono.error(new ClienteNoEncontradoException(9L)));

        web.delete().uri("/clientes/9").exchange().expectStatus().isNotFound();
    }

    @Test
    void error_inesperado_debe_responder_500_sin_filtrar_detalle() {
        when(obtener.obtener(1L)).thenReturn(Mono.error(new IllegalStateException("conexión interna 10.0.0.5")));

        web.get().uri("/clientes/1").exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.detail").isEqualTo("Error interno del servidor");
    }
}
