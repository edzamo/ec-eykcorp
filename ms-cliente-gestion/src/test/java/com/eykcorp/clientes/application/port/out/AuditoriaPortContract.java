package com.eykcorp.clientes.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort.Registro;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

/** Contrato compartido de {@link AuditoriaPort}: lo ejecutan el fake y el adaptador real. */
public abstract class AuditoriaPortContract {

    protected abstract AuditoriaPort puerto();

    /** Registros observables del cliente indicado, en orden de inserción. */
    protected abstract List<Registro> registrosDe(Long clienteId);

    @BeforeEach
    protected void limpiar() {
    }

    private static Cliente cliente(long id) {
        return Cliente.crear(id, "Ana", "Pérez", Correo.de("ana" + id + "@example.com"),
                Telefono.de("0991234567"), Instant.parse("2026-10-05T15:30:00Z"));
    }

    @ParameterizedTest
    @EnumSource(AccionAuditoria.class)
    void registrar_debe_completar_y_dejar_constancia_de_cada_accion(AccionAuditoria accion) {
        StepVerifier.create(puerto().registrar(accion, cliente(101L))).verifyComplete();

        assertThat(registrosDe(101L)).containsExactly(new Registro(accion, 101L));
    }

    @Test
    void registrar_debe_conservar_el_orden_de_las_acciones_de_un_cliente_sin_mezclar_otros() {
        StepVerifier.create(puerto().registrar(AccionAuditoria.CREADO, cliente(201L))
                        .then(puerto().registrar(AccionAuditoria.CREADO, cliente(202L)))
                        .then(puerto().registrar(AccionAuditoria.ELIMINADO, cliente(201L))))
                .verifyComplete();

        assertThat(registrosDe(201L)).containsExactly(
                new Registro(AccionAuditoria.CREADO, 201L), new Registro(AccionAuditoria.ELIMINADO, 201L));
        assertThat(registrosDe(202L)).containsExactly(new Registro(AccionAuditoria.CREADO, 202L));
    }
}
