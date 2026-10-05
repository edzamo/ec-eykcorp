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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Casos de uso del cliente. EXC-2: sin @Transactional; la auditoría (otro almacén) es de mejor
 * esfuerzo: se ejecuta tras persistir, con timeout acotado y sus fallos se registran sin PII.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService implements CrearClienteUseCase, ListarClientesUseCase,
        ObtenerClienteUseCase, ActualizarClienteUseCase, EliminarClienteUseCase {

    static final Duration TIMEOUT_AUDITORIA = Duration.ofSeconds(2);

    private final ClienteRepositoryPort repositorio;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    @Override
    public Mono<Cliente> crear(DatosCliente datos) {
        return Mono.fromSupplier(() -> Cliente.crear(
                        datos.nombres(), datos.apellidos(), Correo.de(datos.correo()),
                        Telefono.deOpcional(datos.telefono()), reloj.instant()))
                .flatMap(nuevo -> repositorio.existePorCorreo(nuevo.correo())
                        .flatMap(existe -> existe
                                ? rechazarCorreoDuplicado()
                                : repositorio.guardar(nuevo)))
                .flatMap(guardado -> auditar(AccionAuditoria.CREADO, guardado))
                .doFirst(() -> log.debug("Creando cliente"))
                .doOnNext(c -> log.info("Cliente creado id={}", c.id()));
    }

    @Override
    public Flux<Cliente> listar() {
        return repositorio.buscarTodos().doFirst(() -> log.debug("Listando clientes"));
    }

    @Override
    public Mono<Cliente> obtener(Long id) {
        return repositorio.buscarPorId(id)
                .switchIfEmpty(Mono.error(() -> new ClienteNoEncontradoException(id)))
                .doFirst(() -> log.debug("Buscando cliente id={}", id));
    }

    @Override
    public Mono<Cliente> actualizar(Long id, DatosCliente datos) {
        return obtener(id)
                .map(actual -> actual.conDatos(
                        datos.nombres(), datos.apellidos(), Correo.de(datos.correo()),
                        Telefono.deOpcional(datos.telefono())))
                .flatMap(modificado -> repositorio.existePorCorreoDeOtro(modificado.correo(), id)
                        .flatMap(deOtro -> deOtro
                                ? rechazarCorreoDuplicado()
                                : repositorio.guardar(modificado)))
                .flatMap(guardado -> auditar(AccionAuditoria.ACTUALIZADO, guardado))
                .doFirst(() -> log.debug("Actualizando cliente id={}", id))
                .doOnNext(c -> log.info("Cliente actualizado id={}", c.id()));
    }

    @Override
    public Mono<Void> eliminar(Long id) {
        return obtener(id)
                .flatMap(existente -> repositorio.eliminarPorId(id).thenReturn(existente))
                .flatMap(eliminado -> auditar(AccionAuditoria.ELIMINADO, eliminado))
                .doFirst(() -> log.debug("Eliminando cliente id={}", id))
                .doOnNext(c -> log.info("Cliente eliminado id={}", c.id()))
                .then();
    }

    private static Mono<Cliente> rechazarCorreoDuplicado() {
        log.debug("Cliente rechazado: correo duplicado");
        return Mono.error(new CorreoDuplicadoException());
    }

    private Mono<Cliente> auditar(AccionAuditoria accion, Cliente cliente) {
        return auditoria.registrar(accion, cliente)
                .timeout(TIMEOUT_AUDITORIA)
                .onErrorResume(error -> {
                    log.warn("Auditoría no registrada: accion={} id={} causa={}",
                            accion, cliente.id(), error.getClass().getSimpleName());
                    return Mono.empty();
                })
                .thenReturn(cliente);
    }
}
