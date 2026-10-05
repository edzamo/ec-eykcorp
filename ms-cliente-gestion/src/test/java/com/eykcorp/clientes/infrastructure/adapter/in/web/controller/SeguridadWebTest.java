package com.eykcorp.clientes.infrastructure.adapter.in.web.controller;

import com.eykcorp.clientes.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.eykcorp.clientes.infrastructure.adapter.in.web.mapper.ClienteWebMapper;

import static org.mockito.Mockito.when;

import com.eykcorp.clientes.application.port.in.ActualizarClienteUseCase;
import com.eykcorp.clientes.application.port.in.CrearClienteUseCase;
import com.eykcorp.clientes.application.port.in.EliminarClienteUseCase;
import com.eykcorp.clientes.application.port.in.ListarClientesUseCase;
import com.eykcorp.clientes.application.port.in.ObtenerClienteUseCase;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.SecurityConfig;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(controllers = {ClienteController.class, AuthController.class})
@Import({ClienteWebMapper.class, GlobalExceptionHandler.class, SecurityConfig.class})
@PropiedadesDeSeguridadDePrueba
class SeguridadWebTest {

    @Autowired
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

    private void listarVacio() {
        when(listar.listar()).thenReturn(Flux.empty());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private WebTestClient.ResponseSpec getClientes(String authorization) {
        WebTestClient.RequestHeadersSpec<?> peticion = web.get().uri("/clientes");
        return (authorization == null ? peticion : peticion.header(HttpHeaders.AUTHORIZATION, authorization))
                .exchange();
    }

    private void esProblema401(WebTestClient.ResponseSpec respuesta) {
        respuesta.expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.title").isEqualTo("No autenticado");
    }

    @Test
    void sin_token_debe_responder_401_con_problem_detail() {
        esProblema401(getClientes(null));
    }

    @Test
    void con_token_basura_debe_responder_401() {
        esProblema401(getClientes(bearer("no-es-un-jwt")));
    }

    @Test
    void con_token_expirado_debe_responder_401() {
        String expirado = TokensDePrueba.servicio(TokensDePrueba.SECRETO, "ms-cliente-gestion",
                Instant.now().minusSeconds(3600)).emitir("admin").token();

        esProblema401(getClientes(bearer(expirado)));
    }

    @Test
    void con_token_firmado_con_otra_clave_debe_responder_401() {
        String falso = TokensDePrueba.servicio(TokensDePrueba.OTRO_SECRETO, "ms-cliente-gestion",
                Instant.now()).emitir("admin").token();

        esProblema401(getClientes(bearer(falso)));
    }

    @Test
    void con_token_de_otro_issuer_debe_responder_401() {
        String ajeno = TokensDePrueba.servicio(TokensDePrueba.SECRETO, "otro", Instant.now()).emitir("admin").token();

        esProblema401(getClientes(bearer(ajeno)));
    }

    @Test
    void con_token_sin_audiencia_o_con_audiencia_distinta_debe_responder_401() {
        esProblema401(getClientes(bearer(TokensDePrueba.tokenConAudiencia(null, Instant.now()))));
        esProblema401(getClientes(bearer(TokensDePrueba.tokenConAudiencia("otro-servicio", Instant.now()))));
    }

    @Test
    void con_token_valido_debe_responder_200() {
        listarVacio();

        getClientes(bearer(TokensDePrueba.tokenVigente())).expectStatus().isOk();
    }

    @Test
    void una_ruta_no_declarada_sin_token_debe_responder_401() {
        esProblema401(web.get().uri("/otra-ruta").exchange());
    }

    @Test
    void una_ruta_no_declarada_con_token_valido_debe_responder_403_con_problem_detail() {
        web.get().uri("/otra-ruta").header(HttpHeaders.AUTHORIZATION, bearer(TokensDePrueba.tokenVigente()))
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.title").isEqualTo("Acceso denegado");
    }

    @Test
    void login_correcto_debe_devolver_un_token_usable_y_la_expiracion_en_segundos() {
        listarVacio();

        Map<?, ?> cuerpo = web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", TokensDePrueba.USUARIO, "password", TokensDePrueba.PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(cuerpo.get("expiraEn")).isEqualTo(1800);
        getClientes(bearer((String) cuerpo.get("token"))).expectStatus().isOk();
    }

    private byte[] loginFallido(String usuario, String password) {
        return web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", usuario, "password", password))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.detail").isEqualTo("Credenciales inválidas")
                .returnResult().getResponseBody();
    }

    @Test
    void login_incorrecto_debe_dar_el_mismo_cuerpo_para_usuario_o_password_erroneos() {
        byte[] usuarioMalo = loginFallido("intruso", TokensDePrueba.PASSWORD);
        byte[] passwordMala = loginFallido(TokensDePrueba.USUARIO, "incorrecta");
        byte[] ambosMalos = loginFallido("intruso", "incorrecta");

        org.assertj.core.api.Assertions.assertThat(usuarioMalo).isEqualTo(passwordMala).isEqualTo(ambosMalos);
    }

    @Test
    void login_con_campos_en_blanco_debe_responder_400() {
        web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", " ", "password", ""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.errores.usuario").exists().jsonPath("$.errores.password").exists();
    }

    @Test
    void login_con_usuario_o_password_de_mas_de_128_caracteres_debe_responder_400() {
        web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", TokensDePrueba.USUARIO, "password", "x".repeat(129)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.errores.password").exists();
        web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", "u".repeat(129), "password", TokensDePrueba.PASSWORD))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.errores.usuario").exists();
    }

    @Test
    void login_con_password_de_exactamente_128_caracteres_no_debe_ser_400() {
        web.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", TokensDePrueba.USUARIO, "password", "x".repeat(128)))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
