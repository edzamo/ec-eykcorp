package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.application.port.out.ClienteRepositoryPort;
import com.eykcorp.clientes.application.port.out.ClienteRepositoryPortContract;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** Contrato del puerto contra PostgreSQL real (Testcontainers) + migración Flyway + unicidad de correo. */
@DataR2dbcTest
@Import({ClientePersistenceAdapter.class, ClienteEntityMapper.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientePersistenceAdapterIT extends ClienteRepositoryPortContract {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    private ClientePersistenceAdapter adaptador;

    @Autowired
    private DatabaseClient db;

    @Override
    protected ClienteRepositoryPort repositorio() {
        return adaptador;
    }

    @Override
    protected void vaciar() {
        db.sql("DELETE FROM clientes").then().block();
    }

    /** Par ordenado (1 y 2): demuestra el aislamiento; la BD debe vaciarse antes de cada prueba. */
    @Test
    @Order(1)
    void aislamiento_paso_1_deja_un_registro_en_la_tabla() {
        StepVerifier.create(adaptador.guardar(nuevo("aislamiento@example.com"))).expectNextCount(1).verifyComplete();
    }

    @Test
    @Order(2)
    void aislamiento_paso_2_debe_encontrar_la_tabla_vacia() {
        StepVerifier.create(adaptador.buscarTodos().count())
                .assertNext(n -> assertThat(n).isZero())
                .verifyComplete();
    }

    @Test
    void la_migracion_debe_crear_la_tabla_clientes() {
        StepVerifier.create(db.sql("SELECT count(*) AS n FROM information_schema.tables "
                                + "WHERE table_name = 'clientes'")
                        .map(fila -> fila.get("n", Long.class)).one())
                .assertNext(n -> assertThat(n).isEqualTo(1L))
                .verifyComplete();
    }

    @Test
    void guardar_con_correo_repetido_debe_traducirse_a_CorreoDuplicadoException() {
        StepVerifier.create(adaptador.guardar(nuevo("dup@example.com"))
                        .then(adaptador.guardar(nuevo("dup@example.com"))))
                .expectError(CorreoDuplicadoException.class)
                .verify();
    }

    /** Caracterización (SEC-007): el INSERT que choca con UNIQUE en una carrera real se traduce a dominio. */
    @Test
    void guardados_concurrentes_con_el_mismo_correo_deben_dar_un_exito_y_un_CorreoDuplicadoException() {
        StepVerifier.create(Flux.merge(
                                adaptador.guardar(nuevo("carrera@example.com")).materialize(),
                                adaptador.guardar(nuevo("carrera@example.com")).materialize())
                        .collectList())
                .assertNext(senales -> {
                    assertThat(senales).filteredOn(s -> s.isOnNext()).hasSize(1);
                    assertThat(senales).filteredOn(s -> s.isOnError())
                            .singleElement()
                            .satisfies(s -> assertThat(s.getThrowable()).isInstanceOf(CorreoDuplicadoException.class));
                })
                .verifyComplete();
    }

    @Test
    void actualizar_con_correo_de_otro_cliente_debe_traducirse_a_CorreoDuplicadoException() {
        StepVerifier.create(adaptador.guardar(nuevo("uno@example.com"))
                        .then(adaptador.guardar(nuevo("dos@example.com")))
                        .flatMap(dos -> adaptador.guardar(
                                dos.conDatos("Ana", "Pérez", nuevo("uno@example.com").correo(), null))))
                .expectError(CorreoDuplicadoException.class)
                .verify();
    }
}
