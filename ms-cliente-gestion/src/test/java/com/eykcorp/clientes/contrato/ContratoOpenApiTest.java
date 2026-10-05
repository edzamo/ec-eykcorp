package com.eykcorp.clientes.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.Telefono;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** El YAML es válido OpenAPI 3.0 y declara las operaciones, seguridad, esquemas y errores acordados. */
class ContratoOpenApiTest {

    private static SwaggerParseResult resultado;
    private static OpenAPI api;

    @BeforeAll
    static void parsear() {
        ParseOptions opciones = new ParseOptions();
        opciones.setResolve(true);
        resultado = new OpenAPIV3Parser().readContents(ContratoOpenApi.contenido(), null, opciones);
        api = resultado.getOpenAPI();
    }

    @Test
    void debe_parsear_sin_errores_ni_advertencias() {
        assertThat(resultado.getMessages()).isEmpty();
        assertThat(api).isNotNull();
        assertThat(api.getOpenapi()).isEqualTo("3.0.3");
        assertThat(api.getInfo().getTitle()).isEqualTo("ms-cliente-gestion");
        assertThat(api.getInfo().getVersion()).isEqualTo("0.1.0");
    }

    @Test
    void debe_declarar_exactamente_las_siete_operaciones() {
        Set<String> operaciones = new LinkedHashSet<>();
        api.getPaths().forEach((ruta, item) -> item.readOperationsMap()
                .forEach((metodo, op) -> operaciones.add(metodo + " " + ruta)));
        assertThat(operaciones).containsExactlyInAnyOrder(
                "POST /auth/login", "POST /clientes", "GET /clientes", "GET /clientes/{id}",
                "PUT /clientes/{id}", "DELETE /clientes/{id}", "GET /actuator/health");
    }

    @Test
    void debe_declarar_servidores_y_etiquetas() {
        assertThat(api.getServers()).extracting("url").containsExactly("/api", "/");
        assertThat(api.getTags()).extracting("name").containsExactly("Autenticación", "Clientes");
    }

    @Test
    void debe_exigir_bearer_jwt_globalmente_salvo_login_y_health() {
        var esquema = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(esquema.getType().toString()).isEqualTo("http");
        assertThat(esquema.getScheme()).isEqualTo("bearer");
        assertThat(esquema.getBearerFormat()).isEqualTo("JWT");
        assertThat(api.getSecurity()).hasSize(1);
        assertThat(api.getSecurity().get(0)).containsKey("bearerAuth");
        assertThat(api.getPaths().get("/auth/login").getPost().getSecurity()).isEmpty();
        assertThat(api.getPaths().get("/actuator/health").getGet().getSecurity()).isEmpty();
    }

    @Test
    void debe_declarar_los_esquemas() {
        assertThat(api.getComponents().getSchemas().keySet())
                .contains("ClienteRequest", "ClienteResponse", "LoginRequest", "LoginResponse", "Problem");
    }

    @Test
    void debe_declarar_las_respuestas_de_error_por_operacion() {
        assertThat(codigos("/auth/login", PathItem::getPost)).contains("200", "400", "401");
        assertThat(codigos("/clientes", PathItem::getPost)).contains("201", "400", "401", "409");
        assertThat(codigos("/clientes", PathItem::getGet)).contains("200", "401");
        assertThat(codigos("/clientes/{id}", PathItem::getGet)).contains("200", "401", "404");
        assertThat(codigos("/clientes/{id}", PathItem::getPut)).contains("200", "400", "401", "404", "409");
        assertThat(codigos("/clientes/{id}", PathItem::getDelete)).contains("204", "401", "404");
        assertThat(api.getPaths().get("/clientes").getPost().getResponses().get("201").getHeaders())
                .containsKey("Location");
    }

    @Test
    void debe_usar_el_patron_de_telefono_del_dominio() {
        Schema<?> telefono = (Schema<?>) api.getComponents().getSchemas().get("ClienteRequest").getProperties().get("telefono");
        assertThat(telefono.getPattern()).isEqualTo("^" + Telefono.PATRON + "$");
        assertThat(telefono.getNullable()).isTrue();
    }

    @Test
    void debe_limitar_las_longitudes_de_los_campos() {
        var cliente = api.getComponents().getSchemas().get("ClienteRequest");
        assertThat(cliente.getRequired()).containsExactlyInAnyOrder("nombres", "apellidos", "correo");
        assertThat(((Schema<?>) cliente.getProperties().get("nombres"))
                .getMaxLength()).isEqualTo(100);
        assertThat(((Schema<?>) cliente.getProperties().get("correo"))
                .getMaxLength()).isEqualTo(254);
        var login = api.getComponents().getSchemas().get("LoginRequest");
        assertThat(((Schema<?>) login.getProperties().get("password"))
                .getMaxLength()).isEqualTo(128);
    }

    private static Set<String> codigos(String ruta, java.util.function.Function<PathItem, Operation> operacion) {
        return operacion.apply(api.getPaths().get(ruta)).getResponses().keySet();
    }
}
