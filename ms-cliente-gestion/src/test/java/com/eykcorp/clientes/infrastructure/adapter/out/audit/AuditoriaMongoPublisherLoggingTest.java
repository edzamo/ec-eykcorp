package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eykcorp.clientes.CapturaLogs;
import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
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

class AuditoriaMongoPublisherLoggingTest {

    @Test
    void registrar_debe_loguear_debug_con_accion_e_id_sin_datos_personales() {
        ReactiveMongoTemplate mongo = mock(ReactiveMongoTemplate.class);
        when(mongo.insert(any(AuditoriaDocument.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        Instant ahora = Instant.parse("2026-10-05T15:30:00Z");
        var publicador = new AuditoriaMongoPublisher(mongo, new AuditoriaDocumentMapper(),
                Clock.fixed(ahora, ZoneOffset.UTC));
        Cliente cliente = Cliente.crear(7L, "Ana", "Pérez", Correo.de("ana@example.com"),
                Telefono.de("0991234567"), ahora);

        try (CapturaLogs logs = CapturaLogs.de(AuditoriaMongoPublisher.class)) {
            StepVerifier.create(publicador.registrar(AccionAuditoria.CREADO, cliente)).verifyComplete();

            assertThat(logs.mensajes()).containsExactly("DEBUG Registrando auditoría accion=CREADO id=7");
        }
    }
}
