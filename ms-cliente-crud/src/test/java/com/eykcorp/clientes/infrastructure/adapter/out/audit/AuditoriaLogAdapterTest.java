package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.eykcorp.clientes.application.port.out.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;

class AuditoriaLogAdapterTest {

    @Test
    void debe_registrar_accion_e_id_sin_datos_personales() {
        Logger logger = (Logger) LoggerFactory.getLogger(AuditoriaLogAdapter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        Cliente cliente = Cliente.crear(7L, "Ana", "Pérez", Correo.de("ana.perez@example.com"),
                Telefono.de("0991234567"), Instant.parse("2026-10-05T15:30:00Z"));

        StepVerifier.create(new AuditoriaLogAdapter().registrar(AccionAuditoria.CREADO, cliente))
                .verifyComplete();
        logger.detachAppender(appender);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("CREADO").contains("7")
                .doesNotContain("ana.perez").doesNotContain("Pérez").doesNotContain("0991234567");
    }
}
