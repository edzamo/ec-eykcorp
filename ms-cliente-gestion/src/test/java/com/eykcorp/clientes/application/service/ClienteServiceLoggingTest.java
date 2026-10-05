package com.eykcorp.clientes.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.CapturaLogs;
import com.eykcorp.clientes.application.command.DatosCliente;
import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort;
import com.eykcorp.clientes.application.port.out.InMemoryClienteRepository;
import java.time.Clock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/** Logs de ClienteService (Lombok @Slf4j) verificados con un ListAppender de Logback y sin PII (INV-17). */
class ClienteServiceLoggingTest {

    private static final String CORREO = "ana.perez@example.com";
    private static final DatosCliente ANA = new DatosCliente("Ana", "Pérez", CORREO, "0991234567");

    private CapturaLogs logs;
    private FakeAuditoriaPort auditoria;
    private ClienteService service;

    @BeforeEach
    void preparar() {
        logs = CapturaLogs.de(ClienteService.class);
        auditoria = new FakeAuditoriaPort();
        service = new ClienteService(new InMemoryClienteRepository(), auditoria, Clock.systemUTC());
    }

    @AfterEach
    void cerrar() {
        logs.close();
    }

    private void sinPii() {
        assertThat(String.join("\n", logs.mensajes()))
                .doesNotContain("ana.perez").doesNotContain("0991234567").doesNotContain("Pérez")
                .doesNotContain("Ana ");
    }

    @Test
    void el_fallo_de_auditoria_llega_a_logback_sin_datos_personales() {
        auditoria.fallarCon(new IllegalStateException("mongo caído"));

        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();

        assertThat(logs.mensajes()).anySatisfy(m -> assertThat(m)
                .startsWith("WARN").contains("CREADO").contains("IllegalStateException"));
        sinPii();
    }

    @Test
    void crear_debe_registrar_debug_de_entrada_e_info_con_id() {
        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();

        assertThat(logs.mensajes()).contains("DEBUG Creando cliente", "INFO Cliente creado id=1");
        sinPii();
    }

    @Test
    void crear_con_correo_duplicado_debe_registrar_debug_del_rechazo_sin_el_correo() {
        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.crear(ANA)).expectError().verify();

        assertThat(logs.mensajes()).contains("DEBUG Cliente rechazado: correo duplicado");
        sinPii();
    }

    @Test
    void listar_y_obtener_deben_registrar_debug() {
        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.listar()).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.obtener(1L)).expectNextCount(1).verifyComplete();

        assertThat(logs.mensajes()).contains("DEBUG Listando clientes", "DEBUG Buscando cliente id=1");
        sinPii();
    }

    @Test
    void actualizar_y_eliminar_deben_registrar_info_con_id() {
        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.actualizar(1L, ANA)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.eliminar(1L)).verifyComplete();

        assertThat(logs.mensajes()).contains(
                "DEBUG Actualizando cliente id=1", "INFO Cliente actualizado id=1",
                "DEBUG Eliminando cliente id=1", "INFO Cliente eliminado id=1");
        sinPii();
    }
}
