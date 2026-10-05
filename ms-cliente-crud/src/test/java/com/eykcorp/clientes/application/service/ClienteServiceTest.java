package com.eykcorp.clientes.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.application.port.in.DatosCliente;
import com.eykcorp.clientes.application.port.out.AccionAuditoria;
import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort;
import com.eykcorp.clientes.application.port.out.InMemoryClienteRepository;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.ClienteNoEncontradoException;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import com.eykcorp.clientes.domain.cliente.ValorInvalidoException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ClienteServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private static final DatosCliente ANA = new DatosCliente("Ana", "Pérez", "ana@example.com", "0991234567");

    private final InMemoryClienteRepository repo = new InMemoryClienteRepository();
    private final FakeAuditoriaPort auditoria = new FakeAuditoriaPort();
    private final ClienteService service =
            new ClienteService(repo, auditoria, Clock.fixed(AHORA, ZoneOffset.UTC));

    @Test
    void crear_debe_guardar_con_id_y_fecha_del_reloj_y_auditar_despues() {
        StepVerifier.create(service.crear(ANA))
                .assertNext(c -> {
                    assertThat(c.id()).isNotNull();
                    assertThat(c.fechaCreacion()).isEqualTo(AHORA);
                    assertThat(c.correo().valor()).isEqualTo("ana@example.com");
                })
                .verifyComplete();
        assertThat(auditoria.registros()).hasSize(1);
        assertThat(auditoria.registros().get(0).accion()).isEqualTo(AccionAuditoria.CREADO);
        assertThat(auditoria.registros().get(0).clienteId()).isNotNull();
    }

    @Test
    void crear_debe_aceptar_telefono_nulo() {
        StepVerifier.create(service.crear(new DatosCliente("Ana", "Pérez", "a@example.com", null)))
                .assertNext(c -> assertThat(c.telefono()).isNull())
                .verifyComplete();
    }

    @Test
    void crear_debe_fallar_con_correo_duplicado_y_no_auditar() {
        service.crear(ANA).block(Duration.ofSeconds(2));

        StepVerifier.create(service.crear(new DatosCliente("Otra", "Persona", "ANA@example.com", null)))
                .expectError(CorreoDuplicadoException.class)
                .verify();
        assertThat(repo.total()).isEqualTo(1);
        assertThat(auditoria.registros()).hasSize(1);
    }

    @Test
    void crear_debe_fallar_con_datos_invalidos_sin_tocar_el_repositorio() {
        StepVerifier.create(service.crear(new DatosCliente("", "Pérez", "a@example.com", null)))
                .expectError(ValorInvalidoException.class)
                .verify();
        assertThat(repo.total()).isZero();
    }

    @Test
    void listar_debe_devolver_todos_los_clientes() {
        service.crear(ANA).block(Duration.ofSeconds(2));
        service.crear(new DatosCliente("Luis", "Gómez", "luis@example.com", null)).block(Duration.ofSeconds(2));

        StepVerifier.create(service.listar()).expectNextCount(2).verifyComplete();
    }

    @Test
    void obtener_debe_devolver_el_cliente_o_fallar_con_no_encontrado() {
        Cliente creado = service.crear(ANA).block(Duration.ofSeconds(2));

        StepVerifier.create(service.obtener(creado.id())).expectNext(creado).verifyComplete();
        StepVerifier.create(service.obtener(999L)).expectError(ClienteNoEncontradoException.class).verify();
    }

    @Test
    void actualizar_debe_conservar_fecha_de_creacion_y_auditar() {
        Cliente creado = service.crear(ANA).block(Duration.ofSeconds(2));
        ClienteService posterior = new ClienteService(
                repo, auditoria, Clock.fixed(AHORA.plusSeconds(3600), ZoneOffset.UTC));

        StepVerifier.create(posterior.actualizar(
                        creado.id(), new DatosCliente("Ana María", "Pérez", "ana@example.com", null)))
                .assertNext(c -> {
                    assertThat(c.id()).isEqualTo(creado.id());
                    assertThat(c.nombres()).isEqualTo("Ana María");
                    assertThat(c.telefono()).isNull();
                    assertThat(c.fechaCreacion()).isEqualTo(AHORA);
                })
                .verifyComplete();
        assertThat(auditoria.registros()).extracting(FakeAuditoriaPort.Registro::accion)
                .containsExactly(AccionAuditoria.CREADO, AccionAuditoria.ACTUALIZADO);
    }

    @Test
    void actualizar_debe_fallar_con_no_encontrado_si_no_existe() {
        StepVerifier.create(service.actualizar(42L, ANA))
                .expectError(ClienteNoEncontradoException.class)
                .verify();
    }

    @Test
    void actualizar_debe_fallar_con_409_si_el_correo_es_de_otro_cliente() {
        service.crear(ANA).block(Duration.ofSeconds(2));
        Cliente otro = service.crear(new DatosCliente("Luis", "Gómez", "luis@example.com", null))
                .block(Duration.ofSeconds(2));

        StepVerifier.create(service.actualizar(
                        otro.id(), new DatosCliente("Luis", "Gómez", "ana@example.com", null)))
                .expectError(CorreoDuplicadoException.class)
                .verify();
    }

    @Test
    void actualizar_debe_permitir_conservar_el_propio_correo() {
        Cliente creado = service.crear(ANA).block(Duration.ofSeconds(2));

        StepVerifier.create(service.actualizar(creado.id(), ANA)).expectNextCount(1).verifyComplete();
    }

    @Test
    void eliminar_debe_borrar_y_auditar() {
        Cliente creado = service.crear(ANA).block(Duration.ofSeconds(2));

        StepVerifier.create(service.eliminar(creado.id())).verifyComplete();
        assertThat(repo.total()).isZero();
        assertThat(auditoria.registros()).extracting(FakeAuditoriaPort.Registro::accion)
                .containsExactly(AccionAuditoria.CREADO, AccionAuditoria.ELIMINADO);
    }

    @Test
    void eliminar_debe_fallar_con_no_encontrado_si_no_existe() {
        StepVerifier.create(service.eliminar(7L)).expectError(ClienteNoEncontradoException.class).verify();
        assertThat(auditoria.registros()).isEmpty();
    }

    // --- EXC-2: auditoría de mejor esfuerzo ---

    @Test
    void con_auditoria_fallando_el_caso_de_uso_termina_ok_y_el_cliente_queda_persistido() {
        auditoria.fallarCon(new IllegalStateException("mongo caído"));

        StepVerifier.create(service.crear(ANA)).expectNextCount(1).verifyComplete();
        assertThat(repo.total()).isEqualTo(1);
    }

    @Test
    void con_repositorio_fallando_no_se_invoca_la_auditoria() {
        repo.fallarCon(new IllegalStateException("postgres caído"));

        StepVerifier.create(service.crear(ANA)).expectError(IllegalStateException.class).verify();
        assertThat(auditoria.registros()).isEmpty();
    }

    @Test
    void con_auditoria_lenta_la_operacion_no_se_bloquea_mas_del_timeout() {
        auditoria.responderTras(Duration.ofMinutes(10));

        StepVerifier.withVirtualTime(() -> service.crear(ANA))
                .expectSubscription()
                .thenAwait(ClienteService.TIMEOUT_AUDITORIA)
                .expectNextCount(1)
                .verifyComplete();
        assertThat(repo.total()).isEqualTo(1);
    }

}
