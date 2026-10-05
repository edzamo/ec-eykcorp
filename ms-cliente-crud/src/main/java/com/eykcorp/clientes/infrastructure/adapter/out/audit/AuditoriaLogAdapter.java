package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import com.eykcorp.clientes.application.port.out.AccionAuditoria;
import com.eykcorp.clientes.application.port.out.AuditoriaPort;
import com.eykcorp.clientes.domain.cliente.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implementación mínima de {@link AuditoriaPort}: solo registra acción e id (sin PII). Es el
 * adaptador de runtime hasta la tarea de MongoDB, que lo reemplazará (se elimina esta clase).
 */
@Component
public class AuditoriaLogAdapter implements AuditoriaPort {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaLogAdapter.class);

    @Override
    public Mono<Void> registrar(AccionAuditoria accion, Cliente cliente) {
        return Mono.fromRunnable(() -> LOG.info("Auditoría: accion={} id={}", accion, cliente.id()));
    }
}
