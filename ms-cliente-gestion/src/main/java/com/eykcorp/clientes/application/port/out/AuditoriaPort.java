package com.eykcorp.clientes.application.port.out;

import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import reactor.core.publisher.Mono;

public interface AuditoriaPort {

    Mono<Void> registrar(AccionAuditoria accion, Cliente cliente);
}
