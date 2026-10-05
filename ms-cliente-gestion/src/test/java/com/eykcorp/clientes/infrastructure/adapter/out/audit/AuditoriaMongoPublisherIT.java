package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.application.port.out.AuditoriaPort;
import com.eykcorp.clientes.application.port.out.AuditoriaPortContract;
import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort.Registro;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

/** Contrato del puerto contra MongoDB real (Testcontainers) + marca de tiempo del Clock + minimización. */
@DataMongoTest
@Import({AuditoriaMongoPublisher.class, AuditoriaDocumentMapper.class,
        AuditoriaMongoPublisherIT.RelojFijo.class})
class AuditoriaMongoPublisherIT extends AuditoriaPortContract {

    static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojDePrueba() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        MongoTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    private AuditoriaPort publicador;

    @Autowired
    private ReactiveMongoTemplate mongo;

    @Override
    protected AuditoriaPort puerto() {
        return publicador;
    }

    @Override
    protected void vaciar() {
        mongo.remove(new org.springframework.data.mongodb.core.query.Query(), "auditoria").block();
    }

    @Override
    protected List<Registro> registrosDe(Long clienteId) {
        return mongo.findAll(AuditoriaDocument.class)
                .filter(d -> d.clienteId().equals(clienteId))
                .map(d -> new Registro(AccionAuditoria.valueOf(d.accion()), d.clienteId()))
                .collectList().block();
    }

    @Test
    void debe_guardar_solo_accion_cliente_id_y_marca_de_tiempo_del_reloj() {
        Cliente cliente = Cliente.crear(7L, "Ana", "Pérez", Correo.de("ana.perez@example.com"),
                Telefono.de("0991234567"), AHORA);

        StepVerifier.create(publicador.registrar(AccionAuditoria.CREADO, cliente)).verifyComplete();

        List<org.bson.Document> crudos = mongo.findAll(org.bson.Document.class, "auditoria")
                .collectList().block();
        assertThat(crudos).hasSize(1);
        org.bson.Document doc = crudos.get(0);
        assertThat(doc.keySet()).containsExactlyInAnyOrder("_id", "accion", "clienteId", "timestamp", "_class");
        assertThat(doc.getString("accion")).isEqualTo("CREADO");
        assertThat(doc.getDate("timestamp").toInstant()).isEqualTo(AHORA);
        assertThat(doc.toJson()).doesNotContain("ana.perez").doesNotContain("Pérez").doesNotContain("0991234567");
    }
}
