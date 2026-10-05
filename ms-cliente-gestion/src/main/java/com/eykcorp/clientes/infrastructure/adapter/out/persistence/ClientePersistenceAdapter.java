package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import com.eykcorp.clientes.application.port.out.ClienteRepositoryPort;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Adaptador de salida a PostgreSQL (R2DBC). Traduce los errores de infraestructura (INV-09). */
@Component
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private static final String RESTRICCION_CORREO = "uk_clientes_correo";

    private final ClienteR2dbcRepository repositorio;
    private final ClienteEntityMapper mapper;

    public ClientePersistenceAdapter(ClienteR2dbcRepository repositorio, ClienteEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Mono<Cliente> guardar(Cliente cliente) {
        return repositorio.save(mapper.aEntidad(cliente))
                .map(mapper::aDominio)
                .onErrorMap(ClientePersistenceAdapter::esCorreoDuplicado, error -> new CorreoDuplicadoException());
    }

    @Override
    public Mono<Cliente> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public Flux<Cliente> buscarTodos() {
        return repositorio.findAll().map(mapper::aDominio);
    }

    @Override
    public Mono<Void> eliminarPorId(Long id) {
        return repositorio.deleteById(id);
    }

    @Override
    public Mono<Boolean> existePorCorreo(Correo correo) {
        return repositorio.existsByCorreo(correo.valor());
    }

    @Override
    public Mono<Boolean> existePorCorreoDeOtro(Correo correo, Long id) {
        return repositorio.existsByCorreoAndIdNot(correo.valor(), id);
    }

    private static boolean esCorreoDuplicado(Throwable error) {
        return error instanceof DuplicateKeyException
                || (error instanceof DataIntegrityViolationException
                        && String.valueOf(error.getMessage()).contains(RESTRICCION_CORREO));
    }
}
