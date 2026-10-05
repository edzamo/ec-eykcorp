package com.eykcorp.clientes.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.infrastructure.adapter.out.audit.MongoTestContainer;
import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/** Paridad en ambos sentidos entre las operaciones del contrato y los endpoints implementados. */
@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParidadContratoCodigoTest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        MongoTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapeos;

    private Set<String> operacionesDelCodigo() {
        Set<String> operaciones = new TreeSet<>();
        mapeos.getHandlerMethods().keySet().forEach(info -> info.getPatternsCondition().getPatterns().forEach(patron -> {
            String ruta = patron.getPatternString();
            if (ruta.startsWith("/clientes") || ruta.startsWith("/auth/login")) {
                info.getMethodsCondition().getMethods().forEach(m -> operaciones.add(m.name() + " " + ruta));
            }
        }));
        return operaciones;
    }

    private Set<String> operacionesDelContrato() {
        OpenAPI api = new OpenAPIV3Parser().readContents(ContratoOpenApi.contenido()).getOpenAPI();
        Set<String> operaciones = new TreeSet<>();
        api.getPaths().forEach((ruta, item) -> {
            if (!ruta.startsWith("/actuator")) {
                item.readOperationsMap().forEach((metodo, op) -> operaciones.add(metodo + " " + ruta));
            }
        });
        return operaciones;
    }

    @Test
    void cada_endpoint_implementado_debe_existir_en_el_contrato_y_viceversa() {
        Set<String> codigo = operacionesDelCodigo();
        Set<String> contrato = operacionesDelContrato();
        assertThat(codigo).as("endpoints implementados").hasSize(6);
        assertThat(codigo).as("código vs contrato").isEqualTo(contrato);
    }
}
