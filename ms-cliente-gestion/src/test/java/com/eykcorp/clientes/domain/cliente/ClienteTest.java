package com.eykcorp.clientes.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ClienteTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private static final Correo CORREO = Correo.de("ana.perez@example.com");
    private static final Telefono TELEFONO = Telefono.de("0991234567");

    @Test
    void debe_crear_cliente_sin_id_con_fecha_de_creacion_asignada() {
        Cliente cliente = Cliente.crear("Ana María", "Pérez Loor", CORREO, TELEFONO, AHORA);

        assertThat(cliente.id()).isNull();
        assertThat(cliente.nombres()).isEqualTo("Ana María");
        assertThat(cliente.apellidos()).isEqualTo("Pérez Loor");
        assertThat(cliente.correo()).isEqualTo(CORREO);
        assertThat(cliente.telefono()).isEqualTo(TELEFONO);
        assertThat(cliente.fechaCreacion()).isEqualTo(AHORA);
    }

    @Test
    void debe_crear_cliente_con_id_explicito() {
        assertThat(Cliente.crear(7L, "Ana", "Pérez", CORREO, null, AHORA).id()).isEqualTo(7L);
    }

    @Test
    void debe_permitir_telefono_nulo() {
        assertThat(Cliente.crear("Ana", "Pérez", CORREO, null, AHORA).telefono()).isNull();
    }

    @Test
    void debe_recortar_nombres_y_apellidos() {
        Cliente cliente = Cliente.crear("  Ana ", " Pérez  ", CORREO, null, AHORA);

        assertThat(cliente.nombres()).isEqualTo("Ana");
        assertThat(cliente.apellidos()).isEqualTo("Pérez");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void debe_rechazar_nombres_vacios(String nombres) {
        assertThatThrownBy(() -> Cliente.crear(nombres, "Pérez", CORREO, null, AHORA))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void debe_rechazar_apellidos_vacios(String apellidos) {
        assertThatThrownBy(() -> Cliente.crear("Ana", apellidos, CORREO, null, AHORA))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void debe_rechazar_nombres_o_apellidos_de_mas_de_100_caracteres() {
        String largo = "a".repeat(101);
        assertThatThrownBy(() -> Cliente.crear(largo, "Pérez", CORREO, null, AHORA))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> Cliente.crear("Ana", largo, CORREO, null, AHORA))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void debe_rechazar_correo_nulo() {
        assertThatThrownBy(() -> Cliente.crear("Ana", "Pérez", null, null, AHORA))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void conDatos_debe_devolver_copia_conservando_id_y_fecha_de_creacion() {
        Cliente original = Cliente.crear(5L, "Ana", "Pérez", CORREO, TELEFONO, AHORA);
        Correo otroCorreo = Correo.de("otra@example.com");

        Cliente modificado = original.conDatos(" Luisa ", "Gómez", otroCorreo, null);

        assertThat(modificado).isNotSameAs(original);
        assertThat(modificado.id()).isEqualTo(5L);
        assertThat(modificado.fechaCreacion()).isEqualTo(AHORA);
        assertThat(modificado.nombres()).isEqualTo("Luisa");
        assertThat(modificado.apellidos()).isEqualTo("Gómez");
        assertThat(modificado.correo()).isEqualTo(otroCorreo);
        assertThat(modificado.telefono()).isNull();
        assertThat(original.nombres()).isEqualTo("Ana");
    }

    @Test
    void conDatos_debe_validar_los_nuevos_datos() {
        Cliente original = Cliente.crear("Ana", "Pérez", CORREO, null, AHORA);
        assertThatThrownBy(() -> original.conDatos("", "Pérez", CORREO, null))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void conId_debe_devolver_copia_con_el_identificador_asignado() {
        Cliente guardado = Cliente.crear("Ana", "Pérez", CORREO, null, AHORA).conId(9L);

        assertThat(guardado.id()).isEqualTo(9L);
        assertThat(guardado.nombres()).isEqualTo("Ana");
    }

    @Test
    void debe_enmascarar_datos_personales_en_toString() {
        String texto = Cliente.crear(3L, "Ana María", "Pérez Loor", CORREO, TELEFONO, AHORA).toString();

        assertThat(texto)
                .contains("3")
                .doesNotContain("Ana")
                .doesNotContain("Pérez")
                .doesNotContain("ana.perez")
                .doesNotContain("0991234567");
    }

    @Test
    void excepciones_de_dominio_deben_ser_DomainException() {
        assertThat(new ValorInvalidoException("x")).isInstanceOf(DomainException.class);
        assertThat(new ClienteNoEncontradoException(4L)).isInstanceOf(DomainException.class);
        assertThat(new CorreoDuplicadoException()).isInstanceOf(DomainException.class);
        assertThat(new ClienteNoEncontradoException(4L).getMessage()).contains("4");
    }
}
