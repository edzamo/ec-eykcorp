package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eykcorp.clientes.application.port.out.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuditoriaMongoPublisherTest {

    @Test
    void debe_propagar_el_error_de_mongo_para_que_el_caso_de_uso_decida() {
        ReactiveMongoTemplate mongo = mock(ReactiveMongoTemplate.class);
        when(mongo.insert(any(AuditoriaDocument.class))).thenReturn(Mono.error(new IllegalStateException("caído")));
        Instant ahora = Instant.parse("2026-10-05T15:30:00Z");
        var publicador = new AuditoriaMongoPublisher(mongo, new AuditoriaDocumentMapper(),
                Clock.fixed(ahora, ZoneOffset.UTC));
        Cliente cliente = Cliente.crear(7L, "Ana", "Pérez", Correo.de("ana@example.com"),
                Telefono.de("0991234567"), ahora);

        StepVerifier.create(publicador.registrar(AccionAuditoria.CREADO, cliente))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(IllegalStateException.class))
                .verify();
    }
}
