package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.application.port.out.ClienteRepositoryPort;
import com.eykcorp.clientes.application.port.out.ClienteRepositoryPortContract;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

/** Contrato del puerto contra PostgreSQL real (Testcontainers) + migración Flyway + unicidad de correo. */
@DataR2dbcTest
@Import({ClientePersistenceAdapter.class, ClienteEntityMapper.class})
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
    protected void limpiar() {
        db.sql("DELETE FROM clientes").then().block();
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
