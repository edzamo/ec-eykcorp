package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.application.port.out.AuditoriaPort;
import com.eykcorp.clientes.domain.cliente.Cliente;
import java.time.Clock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Publica la auditoría en MongoDB; el instante sale del {@link Clock} inyectado (INV-13). */
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AuditoriaMongoPublisher implements AuditoriaPort {

    private final ReactiveMongoTemplate mongo;
    private final AuditoriaDocumentMapper mapper;
    private final Clock reloj;

    @Override
    public Mono<Void> registrar(AccionAuditoria accion, Cliente cliente) {
        return Mono.defer(() -> mongo.insert(mapper.aDocumento(accion, cliente, reloj.instant())))
                .then();
    }
}
