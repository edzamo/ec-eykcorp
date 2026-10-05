package com.eykcorp.clientes.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.infrastructure.adapter.out.audit.MongoTestContainer;
import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

/** Documentación pública (sin token); el resto de la API mantiene el deny por defecto. */
@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerUiTest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        MongoTestContainer.registrarPropiedades(registry);
    }

    @LocalServerPort
    private int puerto;
    private WebTestClient web;
    private WebTestClient webSinRedireccion;

    @BeforeEach
    void clientes() {
        web = WebTestClient.bindToServer(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .baseUrl("http://localhost:" + puerto).build();
        webSinRedireccion = WebTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    void swagger_ui_debe_responder_200_sin_token_siguiendo_la_redireccion() {
        web.get().uri("/swagger-ui.html").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(html -> assertThat(html).containsIgnoringCase("swagger"));
    }

    @Test
    void swagger_ui_debe_apuntar_al_contrato_yaml() {
        web.get().uri("/swagger-ui/swagger-initializer.js").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(js -> assertThat(js).contains(ContratoOpenApi.RUTA_HTTP));
    }

    @Test
    void la_configuracion_de_la_ui_debe_ser_publica_y_apuntar_al_contrato() {
        web.get().uri("/v3/api-docs/swagger-config").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.url").isEqualTo(ContratoOpenApi.RUTA_HTTP);
    }

    @Test
    void detras_de_nginx_la_ui_debe_respetar_el_prefijo_x_forwarded_prefix() {
        web.get().uri("/v3/api-docs/swagger-config").header("X-Forwarded-Prefix", "/api").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.url").isEqualTo("/api" + ContratoOpenApi.RUTA_HTTP)
                .jsonPath("$.configUrl").isEqualTo("/api/v3/api-docs/swagger-config");
    }

    @Test
    void debe_servir_el_yaml_del_contrato_sin_token() {
        web.get().uri(ContratoOpenApi.RUTA_HTTP).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(ContratoOpenApi.contenido());
    }

    @Test
    void la_documentacion_generada_desde_el_codigo_no_debe_ser_publica() {
        webSinRedireccion.get().uri("/v3/api-docs").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void clientes_sin_token_debe_seguir_dando_401() {
        webSinRedireccion.get().uri("/clientes").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void una_ruta_no_documentada_debe_seguir_denegada() {
        webSinRedireccion.get().uri("/cualquier-cosa").exchange().expectStatus().isUnauthorized();
        webSinRedireccion.post().uri("/swagger-ui.html").exchange().expectStatus().isUnauthorized();
        webSinRedireccion.put().uri(ContratoOpenApi.RUTA_HTTP).exchange().expectStatus().isUnauthorized();
    }
}
