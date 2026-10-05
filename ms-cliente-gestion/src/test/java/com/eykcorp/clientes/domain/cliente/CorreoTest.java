package com.eykcorp.clientes.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CorreoTest {

    @Test
    void debe_normalizar_a_minusculas_y_recortar_espacios() {
        assertThat(Correo.de("  Ana.Perez@Example.COM ").valor()).isEqualTo("ana.perez@example.com");
    }

    @Test
    void debe_ser_igual_a_otro_correo_con_distinta_capitalizacion() {
        assertThat(Correo.de("ANA@example.com")).isEqualTo(Correo.de("ana@EXAMPLE.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "sin-arroba", "@example.com", "ana@", "ana@example", "ana perez@example.com", "a@@b.com"})
    void debe_rechazar_formato_invalido(String valor) {
        assertThatThrownBy(() -> Correo.de(valor)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void debe_rechazar_correo_de_mas_de_254_caracteres() {
        String largo = "a".repeat(250) + "@b.com";
        assertThatThrownBy(() -> Correo.de(largo)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void debe_ocultar_la_parte_local_en_toString() {
        assertThat(Correo.de("ana.perez@example.com").toString())
                .doesNotContain("ana.perez")
                .isEqualTo("Correo[***@example.com]");
    }
}
