package com.eykcorp.clientes.application.service;

import com.eykcorp.clientes.application.port.in.ActualizarClienteUseCase;
import com.eykcorp.clientes.application.port.in.CrearClienteUseCase;
import com.eykcorp.clientes.application.command.DatosCliente;
import com.eykcorp.clientes.application.port.in.EliminarClienteUseCase;
import com.eykcorp.clientes.application.port.in.ListarClientesUseCase;
import com.eykcorp.clientes.application.port.in.ObtenerClienteUseCase;
import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.application.port.out.AuditoriaPort;
import com.eykcorp.clientes.application.port.out.ClienteRepositoryPort;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.ClienteNoEncontradoException;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.CorreoDuplicadoException;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Clock;
import java.time.Duration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Casos de uso del cliente. EXC-2: sin @Transactional; la auditoría (otro almacén) es de mejor
 * esfuerzo: se ejecuta tras persistir, con timeout acotado y sus fallos se registran sin PII.
 */
@Service
public class ClienteService implements CrearClienteUseCase, ListarClientesUseCase,
        ObtenerClienteUseCase, ActualizarClienteUseCase, EliminarClienteUseCase {

    static final Duration TIMEOUT_AUDITORIA = Duration.ofSeconds(2);

    private static final System.Logger LOG = System.getLogger(ClienteService.class.getName());

    private final ClienteRepositoryPort repositorio;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public ClienteService(ClienteRepositoryPort repositorio, AuditoriaPort auditoria, Clock reloj) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    public Mono<Cliente> crear(DatosCliente datos) {
        return Mono.fromSupplier(() -> Cliente.crear(
                        datos.nombres(), datos.apellidos(), Correo.de(datos.correo()),
                        Telefono.deOpcional(datos.telefono()), reloj.instant()))
                .flatMap(nuevo -> repositorio.existePorCorreo(nuevo.correo())
                        .flatMap(existe -> existe
                                ? Mono.<Cliente>error(new CorreoDuplicadoException())
                                : repositorio.guardar(nuevo)))
                .flatMap(guardado -> auditar(AccionAuditoria.CREADO, guardado));
    }

    @Override
    public Flux<Cliente> listar() {
        return repositorio.buscarTodos();
    }

    @Override
    public Mono<Cliente> obtener(Long id) {
        return repositorio.buscarPorId(id)
                .switchIfEmpty(Mono.error(() -> new ClienteNoEncontradoException(id)));
    }

    @Override
    public Mono<Cliente> actualizar(Long id, DatosCliente datos) {
        return obtener(id)
                .map(actual -> actual.conDatos(
                        datos.nombres(), datos.apellidos(), Correo.de(datos.correo()),
                        Telefono.deOpcional(datos.telefono())))
                .flatMap(modificado -> repositorio.existePorCorreoDeOtro(modificado.correo(), id)
                        .flatMap(deOtro -> deOtro
                                ? Mono.<Cliente>error(new CorreoDuplicadoException())
                                : repositorio.guardar(modificado)))
                .flatMap(guardado -> auditar(AccionAuditoria.ACTUALIZADO, guardado));
    }

    @Override
    public Mono<Void> eliminar(Long id) {
        return obtener(id)
                .flatMap(existente -> repositorio.eliminarPorId(id).thenReturn(existente))
                .flatMap(eliminado -> auditar(AccionAuditoria.ELIMINADO, eliminado))
                .then();
    }

    private Mono<Cliente> auditar(AccionAuditoria accion, Cliente cliente) {
        return auditoria.registrar(accion, cliente)
                .timeout(TIMEOUT_AUDITORIA)
                .onErrorResume(error -> {
                    LOG.log(System.Logger.Level.WARNING,
                            "Auditoría no registrada: accion={0} id={1} causa={2}",
                            accion, cliente.id(), error.getClass().getSimpleName());
                    return Mono.empty();
                })
                .thenReturn(cliente);
    }
}
