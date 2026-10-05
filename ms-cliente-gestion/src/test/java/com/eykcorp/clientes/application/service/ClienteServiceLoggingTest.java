package com.eykcorp.clientes.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.eykcorp.clientes.application.port.in.DatosCliente;
import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort;
import com.eykcorp.clientes.application.port.out.InMemoryClienteRepository;
import java.time.Clock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import reactor.test.StepVerifier;

/** Verifica que System.Logger (usado en application) llega a SLF4J/Logback vía jul-to-slf4j y sin PII. */
class ClienteServiceLoggingTest {

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Logger logbackLogger = (Logger) LoggerFactory.getLogger(ClienteService.class.getName());

    private final java.util.logging.Logger raizJul = java.util.logging.Logger.getLogger("");
    private java.util.logging.Handler[] manejadoresOriginales;

    @BeforeEach
    void instalarPuente() {
        manejadoresOriginales = raizJul.getHandlers();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        appender.start();
        logbackLogger.addAppender(appender);
    }

    @AfterEach
    void desinstalarPuente() {
        logbackLogger.detachAppender(appender);
        SLF4JBridgeHandler.uninstall();
        // Restaura el estado JUL global original (el puente se instala solo durante este test)
        for (java.util.logging.Handler manejador : raizJul.getHandlers()) {
            raizJul.removeHandler(manejador);
        }
        for (java.util.logging.Handler manejador : manejadoresOriginales) {
            raizJul.addHandler(manejador);
        }
    }

    @Test
    void el_fallo_de_auditoria_llega_a_logback_sin_datos_personales() {
        FakeAuditoriaPort auditoria = new FakeAuditoriaPort();
        auditoria.fallarCon(new IllegalStateException("mongo caído"));
        ClienteService service = new ClienteService(new InMemoryClienteRepository(), auditoria, Clock.systemUTC());

        StepVerifier.create(service.crear(
                        new DatosCliente("Ana", "Pérez", "ana.perez@example.com", "0991234567")))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(appender.list).hasSize(1);
        String mensaje = appender.list.get(0).getFormattedMessage();
        assertThat(mensaje).contains("CREADO").contains("IllegalStateException")
                .doesNotContain("ana.perez").doesNotContain("0991234567").doesNotContain("Pérez");
    }
}
