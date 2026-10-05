package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import com.eykcorp.clientes.application.port.out.ClienteRepositoryPort;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Adaptador de salida a PostgreSQL (R2DBC). Traduce los errores de infraestructura (INV-09). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private static final String RESTRICCION_CORREO = "uk_clientes_correo";

    private final ClienteR2dbcRepository repositorio;
    private final ClienteEntityMapper mapper;

    @Override
    public Mono<Cliente> guardar(Cliente cliente) {
        log.debug("Guardando cliente");
        return repositorio.save(mapper.aEntidad(cliente))
                .map(mapper::aDominio)
                .onErrorMap(ClientePersistenceAdapter::esCorreoDuplicado, error -> new CorreoDuplicadoException());
    }

    @Override
    public Mono<Cliente> buscarPorId(Long id) {
        log.debug("Buscando cliente id={}", id);
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public Flux<Cliente> buscarTodos() {
        log.debug("Listando clientes");
        return repositorio.findAll().map(mapper::aDominio);
    }

    @Override
    public Mono<Void> eliminarPorId(Long id) {
        log.debug("Eliminando cliente id={}", id);
        return repositorio.deleteById(id);
    }

    @Override
    public Mono<Boolean> existePorCorreo(Correo correo) {
        log.debug("Verificando existencia de correo");
        return repositorio.existsByCorreo(correo.valor());
    }

    @Override
    public Mono<Boolean> existePorCorreoDeOtro(Correo correo, Long id) {
        log.debug("Verificando existencia de correo excluyendo id={}", id);
        return repositorio.existsByCorreoAndIdNot(correo.valor(), id);
    }

    private static boolean esCorreoDuplicado(Throwable error) {
        return error instanceof DuplicateKeyException
                || (error instanceof DataIntegrityViolationException
                        && String.valueOf(error.getMessage()).contains(RESTRICCION_CORREO));
    }
}
