package com.eykcorp.clientes.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/** Contrato compartido: lo ejecutan el fake en memoria y el adaptador real. */
public abstract class ClienteRepositoryPortContract {

    protected static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z").truncatedTo(ChronoUnit.MICROS);

    protected abstract ClienteRepositoryPort repositorio();

    /** Único {@code @BeforeEach} (final): un override sin anotación de lifecycle no se ejecutaría. */
    @BeforeEach
    final void aislar() {
        vaciar();
    }

    /** Deja el almacén vacío antes de cada prueba (por defecto nada: el fake es nuevo por test). */
    protected void vaciar() {
    }

    protected Cliente nuevo(String correo) {
        return Cliente.crear("Ana", "Pérez", Correo.de(correo), Telefono.de("0991234567"), AHORA);
    }

    @Test
    void guardar_debe_asignar_id_y_conservar_los_datos() {
        StepVerifier.create(repositorio().guardar(nuevo("ana@example.com")))
                .assertNext(c -> {
                    assertThat(c.id()).isNotNull();
                    assertThat(c.correo()).isEqualTo(Correo.de("ana@example.com"));
                    assertThat(c.telefono()).isEqualTo(Telefono.de("0991234567"));
                    assertThat(c.fechaCreacion()).isEqualTo(AHORA);
                })
                .verifyComplete();
    }

    @Test
    void guardar_debe_persistir_telefono_nulo() {
        Cliente sinTelefono = Cliente.crear("Ana", "Pérez", Correo.de("n@example.com"), null, AHORA);
        StepVerifier.create(repositorio().guardar(sinTelefono)
                        .flatMap(g -> repositorio().buscarPorId(g.id())))
                .assertNext(c -> assertThat(c.telefono()).isNull())
                .verifyComplete();
    }

    @Test
    void guardar_con_id_existente_debe_actualizar_sin_cambiar_fecha_de_creacion() {
        ClienteRepositoryPort repo = repositorio();
        StepVerifier.create(repo.guardar(nuevo("a@example.com"))
                        .flatMap(g -> repo.guardar(g.conDatos("Luisa", "Gómez", g.correo(), null)))
                        .flatMap(g -> repo.buscarPorId(g.id())))
                .assertNext(c -> {
                    assertThat(c.nombres()).isEqualTo("Luisa");
                    assertThat(c.telefono()).isNull();
                    assertThat(c.fechaCreacion()).isEqualTo(AHORA);
                })
                .verifyComplete();
    }

    @Test
    void buscarPorId_debe_devolver_vacio_si_no_existe() {
        StepVerifier.create(repositorio().buscarPorId(999_999L)).verifyComplete();
    }

    @Test
    void buscarTodos_debe_listar_los_clientes_guardados() {
        ClienteRepositoryPort repo = repositorio();
        StepVerifier.create(repo.guardar(nuevo("uno@example.com"))
                        .then(repo.guardar(nuevo("dos@example.com")))
                        .thenMany(repo.buscarTodos().map(c -> c.correo().valor()).collectList()))
                .assertNext(correos -> assertThat(correos).contains("uno@example.com", "dos@example.com"))
                .verifyComplete();
    }

    @Test
    void eliminarPorId_debe_borrar_el_cliente() {
        ClienteRepositoryPort repo = repositorio();
        StepVerifier.create(repo.guardar(nuevo("del@example.com"))
                        .flatMap(g -> repo.eliminarPorId(g.id()).thenReturn(g))
                        .flatMap(g -> repo.buscarPorId(g.id())))
                .verifyComplete();
    }

    @Test
    void existePorCorreo_debe_reflejar_la_existencia() {
        ClienteRepositoryPort repo = repositorio();
        Correo correo = Correo.de("x@example.com");
        StepVerifier.create(repo.existePorCorreo(correo)
                        .flatMap(antes -> repo.guardar(nuevo("x@example.com"))
                                .then(repo.existePorCorreo(correo))
                                .map(despues -> List.of(antes, despues))))
                .assertNext(r -> assertThat(r).containsExactly(false, true))
                .verifyComplete();
    }

    @Test
    void existePorCorreoDeOtro_debe_ignorar_al_propio_cliente() {
        ClienteRepositoryPort repo = repositorio();
        StepVerifier.create(repo.guardar(nuevo("propio@example.com"))
                        .flatMap(g -> repo.existePorCorreoDeOtro(g.correo(), g.id())
                                .flatMap(propio -> repo.existePorCorreoDeOtro(g.correo(), g.id() + 1000)
                                        .map(ajeno -> List.of(propio, ajeno)))))
                .assertNext(r -> assertThat(r).containsExactly(false, true))
                .verifyComplete();
    }
}
