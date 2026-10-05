package com.eykcorp.clientes;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.infrastructure.adapter.out.audit.MongoTestContainer;
import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** E2E del backend: servidor real + PostgreSQL y MongoDB (Testcontainers), con seguridad JWT real (login + Bearer). */
@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteE2ETest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        MongoTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    private WebTestClient webSinToken;
    private WebTestClient web;

    @BeforeEach
    void iniciarSesion() {
        String token = (String) webSinToken.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", TokensDePrueba.USUARIO, "password", TokensDePrueba.PASSWORD))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody().get("token");
        web = webSinToken.mutate().defaultHeaders(h -> h.setBearerAuth(token)).build();
    }

    @Autowired
    private ReactiveMongoTemplate mongo;

    private List<String> accionesAuditadas(Integer clienteId) {
        return mongo.find(Query.query(Criteria.where("clienteId").is(clienteId.longValue())),
                        org.bson.Document.class, "auditoria")
                .map(d -> d.getString("accion"))
                .collectList().block();
    }

    private static Map<String, Object> cuerpo(String correo, String nombres) {
        return Map.of("nombres", nombres, "apellidos", "Pérez Loor", "correo", correo, "telefono", "0991234567");
    }

    private static String correoUnico() {
        return "e2e-" + UUID.randomUUID() + "@example.com";
    }

    private Map<?, ?> crear(String correo) {
        return web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Ana"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    void flujo_completo_crear_listar_obtener_actualizar_eliminar_y_404() {
        String correo = correoUnico();
        Map<?, ?> creado = crear(correo);
        Integer id = (Integer) creado.get("id");
        assertThat(id).isNotNull();

        web.get().uri("/clientes").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[?(@.id==" + id + ")].correo").isEqualTo(correo);

        web.get().uri("/clientes/{id}", id).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombres").isEqualTo("Ana");

        web.put().uri("/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Ana María"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombres").isEqualTo("Ana María")
                .jsonPath("$.fechaCreacion").isEqualTo(creado.get("fechaCreacion"));

        web.get().uri("/clientes/{id}", id).exchange()
                .expectBody().jsonPath("$.fechaCreacion").isEqualTo(creado.get("fechaCreacion"));

        web.delete().uri("/clientes/{id}", id).exchange().expectStatus().isNoContent();
        assertThat(accionesAuditadas(id)).containsExactly("CREADO", "ACTUALIZADO", "ELIMINADO");
        web.get().uri("/clientes/{id}", id).exchange().expectStatus().isNotFound();
        web.delete().uri("/clientes/{id}", id).exchange().expectStatus().isNotFound();
    }

    @Test
    void sin_token_o_con_token_invalido_debe_responder_401_en_todas_las_operaciones() {
        webSinToken.get().uri("/clientes").exchange().expectStatus().isUnauthorized();
        webSinToken.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correoUnico(), "Ana")).exchange().expectStatus().isUnauthorized();
        webSinToken.delete().uri("/clientes/1").headers(h -> h.setBearerAuth("falso"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void login_con_password_incorrecta_debe_responder_401() {
        webSinToken.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("usuario", TokensDePrueba.USUARIO, "password", "mala"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void debe_responder_400_por_validacion() {
        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nombres", "", "apellidos", "Pérez", "correo", "malo"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.errores.nombres").exists().jsonPath("$.errores.correo").exists();
    }

    @Test
    void debe_responder_409_por_correo_duplicado_al_crear_y_al_actualizar() {
        String correo = correoUnico();
        crear(correo);
        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(cuerpo(correo, "Otra"))
                .exchange().expectStatus().isEqualTo(409);

        Integer otroId = (Integer) crear(correoUnico()).get("id");
        web.put().uri("/clientes/{id}", otroId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Otra"))
                .exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void debe_responder_404_al_actualizar_un_cliente_inexistente() {
        web.put().uri("/clientes/{id}", 987_654_321L).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correoUnico(), "Ana"))
                .exchange().expectStatus().isNotFound();
    }
}
