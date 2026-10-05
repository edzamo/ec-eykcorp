package com.eykcorp.clientes.application.port.out;

import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import reactor.core.publisher.Mono;

/** Fake de auditoría: registra invocaciones y puede fallar o ser lenta. */
public class FakeAuditoriaPort implements AuditoriaPort {

    public record Registro(AccionAuditoria accion, Long clienteId) {
    }

    private final List<Registro> registros = new CopyOnWriteArrayList<>();
    private volatile RuntimeException fallo;
    private volatile Duration latencia = Duration.ZERO;

    public void fallarCon(RuntimeException error) {
        this.fallo = error;
    }

    public void responderTras(Duration retardo) {
        this.latencia = retardo;
    }

    public List<Registro> registros() {
        return List.copyOf(registros);
    }

    @Override
    public Mono<Void> registrar(AccionAuditoria accion, Cliente cliente) {
        return Mono.defer(() -> {
            registros.add(new Registro(accion, cliente.id()));
            if (fallo != null) {
                return Mono.error(fallo);
            }
            return latencia.isZero() ? Mono.empty() : Mono.delay(latencia).then();
        });
    }
}
