package com.eykcorp.clientes.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.eykcorp.clientes.infrastructure.adapter.out.audit.MongoTestContainer;
import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Cada par petición/respuesta real del flujo e2e debe cumplir el contrato OpenAPI. */
@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConformidadContratoE2ETest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        MongoTestContainer.registrarPropiedades(registry);
    }

    // El contrato declara el servidor "/" (directo al microservicio): sin prefijo base.
    private static final OpenApiInteractionValidator VALIDADOR =
            OpenApiInteractionValidator.createFor(ContratoOpenApi.contenido()).withBasePathOverride("").build();
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private WebTestClient webSinToken;
    private WebTestClient web;

    @BeforeEach
    void iniciarSesion() {
        String token = login(TokensDePrueba.PASSWORD).token;
        web = webSinToken.mutate().defaultHeaders(h -> h.setBearerAuth(token)).build();
    }

    private record Intercambio(String token, Long id) {
    }

    private Intercambio login(String password) {
        String cuerpo = json(Map.of("usuario", TokensDePrueba.USUARIO, "password", password));
        EntityExchangeResult<byte[]> r = webSinToken.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(cuerpo)
                .exchange().expectBody().returnResult();
        conforme(r, cuerpo, true);
        if (r.getStatus().value() != 200) {
            return new Intercambio(null, null);
        }
        return new Intercambio((String) leer(r).get("token"), null);
    }

    private static String json(Object valor) {
        try {
            return JSON.writeValueAsString(valor);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> leer(EntityExchangeResult<byte[]> r) {
        try {
            return JSON.readValue(r.getResponseBody(), Map.class);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Convierte el intercambio de WebTestClient a SimpleRequest/SimpleResponse y lo valida contra el YAML. */
    private static void conforme(EntityExchangeResult<byte[]> r, String cuerpoPeticion, boolean validarPeticion) {
        SimpleRequest.Builder peticion = new SimpleRequest.Builder(r.getMethod().name(), r.getUrl().getPath());
        r.getRequestHeaders().forEach((nombre, valores) -> peticion.withHeader(nombre, valores));
        if (cuerpoPeticion != null) {
            peticion.withBody(cuerpoPeticion);
        }
        SimpleResponse.Builder respuesta = new SimpleResponse.Builder(r.getStatus().value());
        r.getResponseHeaders().forEach((nombre, valores) -> respuesta.withHeader(nombre, valores));
        if (r.getResponseBody() != null && r.getResponseBody().length > 0) {
            respuesta.withBody(new String(r.getResponseBody(), StandardCharsets.UTF_8));
        }
        ValidationReport informe = validarPeticion
                ? VALIDADOR.validate(peticion.build(), respuesta.build())
                : VALIDADOR.validateResponse(r.getUrl().getPath(), peticion.build().getMethod(), respuesta.build());
        assertThat(informe.hasErrors()).as("%s %s -> %s: %s", r.getMethod(), r.getUrl().getPath(),
                r.getStatus().value(), informe.getMessages()).isFalse();
    }

    private EntityExchangeResult<byte[]> enviar(WebTestClient cliente, org.springframework.http.HttpMethod metodo,
            String ruta, Object cuerpo, int estadoEsperado) {
        WebTestClient.RequestBodySpec spec = cliente.method(metodo).uri(ruta).contentType(MediaType.APPLICATION_JSON);
        String texto = cuerpo == null ? null : (cuerpo instanceof String s ? s : json(cuerpo));
        WebTestClient.RequestHeadersSpec<?> peticion = texto == null ? spec : spec.bodyValue(texto);
        EntityExchangeResult<byte[]> r = peticion.exchange().expectStatus().isEqualTo(estadoEsperado)
                .expectBody().returnResult();
        // Una petición deliberadamente inválida no cumple el contrato de entrada: solo se valida la respuesta
        conforme(r, texto, cliente == web && estadoEsperado != 400);
        return r;
    }

    private static Map<String, Object> cliente(String correo, String nombres) {
        return Map.of("nombres", nombres, "apellidos", "Pérez Loor", "correo", correo, "telefono", "0991234567");
    }

    private static String correoUnico() {
        return "conf-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void flujo_completo_debe_cumplir_el_contrato() {
        String correo = correoUnico();
        EntityExchangeResult<byte[]> creado = enviar(web, org.springframework.http.HttpMethod.POST, "/clientes",
                cliente(correo, "Ana"), 201);
        assertThat(creado.getResponseHeaders().getFirst(HttpHeaders.LOCATION)).startsWith("/clientes/");
        Object id = leer(creado).get("id");

        enviar(web, org.springframework.http.HttpMethod.GET, "/clientes", null, 200);
        enviar(web, org.springframework.http.HttpMethod.GET, "/clientes/" + id, null, 200);
        enviar(web, org.springframework.http.HttpMethod.PUT, "/clientes/" + id, cliente(correo, "Ana María"), 200);
        enviar(web, org.springframework.http.HttpMethod.DELETE, "/clientes/" + id, null, 204);
        enviar(web, org.springframework.http.HttpMethod.GET, "/clientes/" + id, null, 404);
    }

    @Test
    void errores_400_404_409_deben_cumplir_el_contrato() {
        enviar(web, org.springframework.http.HttpMethod.POST, "/clientes",
                Map.of("nombres", "", "apellidos", "Pérez", "correo", "malo"), 400);
        enviar(web, org.springframework.http.HttpMethod.POST, "/clientes", "{no es json", 400);
        enviar(web, org.springframework.http.HttpMethod.PUT, "/clientes/987654321", cliente(correoUnico(), "Ana"), 404);
        enviar(web, org.springframework.http.HttpMethod.DELETE, "/clientes/987654321", null, 404);

        String correo = correoUnico();
        enviar(web, org.springframework.http.HttpMethod.POST, "/clientes", cliente(correo, "Ana"), 201);
        enviar(web, org.springframework.http.HttpMethod.POST, "/clientes", cliente(correo, "Otra"), 409);
    }

    @Test
    void errores_401_deben_cumplir_el_contrato() {
        enviar(webSinToken, org.springframework.http.HttpMethod.GET, "/clientes", null, 401);
        enviar(webSinToken, org.springframework.http.HttpMethod.DELETE, "/clientes/1", null, 401);
        assertThat(login("mala").token).isNull();
    }

    @Test
    void login_con_cuerpo_invalido_debe_cumplir_el_contrato_400() {
        enviar(webSinToken, org.springframework.http.HttpMethod.POST, "/auth/login",
                Map.of("usuario", "", "password", ""), 400);
    }

    @Test
    void salud_debe_cumplir_el_contrato() {
        EntityExchangeResult<byte[]> r = webSinToken.get().uri("/actuator/health").exchange()
                .expectStatus().isOk().expectBody().returnResult();
        conforme(r, null, true);
    }
}
