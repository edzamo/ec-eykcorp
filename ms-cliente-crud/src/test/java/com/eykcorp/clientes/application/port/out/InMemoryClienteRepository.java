package com.eykcorp.clientes.application.port.out;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Fake en memoria del puerto de persistencia; permite simular un fallo del almacén. */
public class InMemoryClienteRepository implements ClienteRepositoryPort {

    private final Map<Long, Cliente> almacen = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();
    private volatile RuntimeException fallo;

    public void fallarCon(RuntimeException error) {
        this.fallo = error;
    }

    public int total() {
        return almacen.size();
    }

    private <T> Mono<T> conFallo(Mono<T> normal) {
        return Mono.defer(() -> fallo == null ? normal : Mono.error(fallo));
    }

    @Override
    public Mono<Cliente> guardar(Cliente cliente) {
        return conFallo(Mono.fromSupplier(() -> {
            Cliente guardado = cliente.id() == null ? cliente.conId(secuencia.incrementAndGet()) : cliente;
            almacen.put(guardado.id(), guardado);
            return guardado;
        }));
    }

    @Override
    public Mono<Cliente> buscarPorId(Long id) {
        return conFallo(Mono.justOrEmpty(almacen.get(id)));
    }

    @Override
    public Flux<Cliente> buscarTodos() {
        return Flux.defer(() -> fallo == null
                ? Flux.fromIterable(almacen.values()).sort((a, b) -> a.id().compareTo(b.id()))
                : Flux.error(fallo));
    }

    @Override
    public Mono<Void> eliminarPorId(Long id) {
        return conFallo(Mono.fromRunnable(() -> almacen.remove(id)));
    }

    @Override
    public Mono<Boolean> existePorCorreo(Correo correo) {
        return conFallo(Mono.fromSupplier(
                () -> almacen.values().stream().anyMatch(c -> c.correo().equals(correo))));
    }

    @Override
    public Mono<Boolean> existePorCorreoDeOtro(Correo correo, Long id) {
        return conFallo(Mono.fromSupplier(() -> almacen.values().stream()
                .anyMatch(c -> c.correo().equals(correo) && !c.id().equals(id))));
    }
}
