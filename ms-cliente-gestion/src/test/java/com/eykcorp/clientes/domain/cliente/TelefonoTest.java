package com.eykcorp.clientes.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TelefonoTest {

    @ParameterizedTest
    @ValueSource(strings = {"0991234567", "+593991234567", "1234567", "123456789012345"})
    void debe_aceptar_entre_7_y_15_digitos_con_mas_opcional(String valor) {
        assertThat(Telefono.de(valor).valor()).isEqualTo(valor);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "123456", "1234567890123456", "09912abc67", "099-123-4567", "++593991234567", "593+991234567"})
    void debe_rechazar_formato_invalido(String valor) {
        assertThatThrownBy(() -> Telefono.de(valor)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void debe_exponer_el_patron_como_fuente_de_verdad() {
        assertThat("0991234567").matches(Telefono.PATRON);
        assertThat("abc").doesNotMatch(Telefono.PATRON);
    }

    @Test
    void debe_devolver_null_cuando_el_telefono_opcional_no_se_informa() {
        assertThat(Telefono.deOpcional(null)).isNull();
        assertThat(Telefono.deOpcional("0991234567")).isEqualTo(Telefono.de("0991234567"));
    }

    @Test
    void debe_ocultar_el_numero_en_toString() {
        assertThat(Telefono.de("0991234567").toString()).doesNotContain("0991234567").isEqualTo("Telefono[***]");
    }
}
