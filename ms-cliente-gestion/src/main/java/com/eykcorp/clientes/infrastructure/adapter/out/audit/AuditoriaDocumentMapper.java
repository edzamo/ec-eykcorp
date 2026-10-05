package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import com.eykcorp.clientes.application.port.out.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
class AuditoriaDocumentMapper {

    AuditoriaDocument aDocumento(AccionAuditoria accion, Cliente cliente, Instant momento) {
        return new AuditoriaDocument(null, accion.name(), cliente.id(), momento);
    }
}
