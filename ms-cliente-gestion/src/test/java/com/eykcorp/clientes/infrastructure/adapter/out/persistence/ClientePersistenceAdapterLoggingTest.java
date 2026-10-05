package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eykcorp.clientes.CapturaLogs;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ClientePersistenceAdapterLoggingTest {

    private final ClienteR2dbcRepository repo = mock(ClienteR2dbcRepository.class);
    private final ClientePersistenceAdapter adaptador =
            new ClientePersistenceAdapter(repo, new ClienteEntityMapper());

    @Test
    void las_operaciones_deben_loguear_debug_con_id_y_sin_el_correo() {
        when(repo.save(any(ClienteEntity.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repo.findById(anyLong())).thenReturn(Mono.empty());
        when(repo.findAll()).thenReturn(Flux.empty());
        when(repo.deleteById(anyLong())).thenReturn(Mono.empty());
        when(repo.existsByCorreo(anyString())).thenReturn(Mono.just(false));
        when(repo.existsByCorreoAndIdNot(anyString(), anyLong())).thenReturn(Mono.just(false));

        try (CapturaLogs logs = CapturaLogs.de(ClientePersistenceAdapter.class)) {
            StepVerifier.create(adaptador.guardar(Cliente.crear("Ana", "Pérez", Correo.de("ana@example.com"),
                    Telefono.de("0991234567"), Instant.parse("2026-10-05T15:30:00Z"))))
                    .expectNextCount(1).verifyComplete();
            StepVerifier.create(adaptador.buscarPorId(5L)).verifyComplete();
            StepVerifier.create(adaptador.buscarTodos()).verifyComplete();
            StepVerifier.create(adaptador.eliminarPorId(5L)).verifyComplete();
            StepVerifier.create(adaptador.existePorCorreo(Correo.de("ana@example.com"))).expectNext(false).verifyComplete();
            StepVerifier.create(adaptador.existePorCorreoDeOtro(Correo.de("ana@example.com"), 5L))
                    .expectNext(false).verifyComplete();

            assertThat(logs.mensajes()).containsExactly(
                    "DEBUG Guardando cliente",
                    "DEBUG Buscando cliente id=5",
                    "DEBUG Listando clientes",
                    "DEBUG Eliminando cliente id=5",
                    "DEBUG Verificando existencia de correo",
                    "DEBUG Verificando existencia de correo excluyendo id=5");
        }
    }
}
