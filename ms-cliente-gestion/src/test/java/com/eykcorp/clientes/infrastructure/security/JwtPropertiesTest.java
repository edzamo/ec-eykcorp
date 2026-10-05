package com.eykcorp.clientes.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class JwtPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(Config.class);

    @Test
    void debe_rechazar_un_secreto_de_menos_de_32_caracteres() {
        assertThatThrownBy(() -> new JwtProperties("x".repeat(31), 30, "ms-cliente-gestion"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_SECRET").hasMessageContaining("32");
    }

    @Test
    void debe_rechazar_secreto_nulo_o_en_blanco() {
        assertThatThrownBy(() -> new JwtProperties(null, 30, "ms-cliente-gestion"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtProperties(" ".repeat(40), 30, "ms-cliente-gestion"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debe_rechazar_expiracion_no_positiva() {
        assertThatThrownBy(() -> new JwtProperties(TokensDePrueba.SECRETO, 0, "ms-cliente-gestion"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void el_arranque_debe_fallar_con_mensaje_claro_si_el_secreto_es_corto() {
        runner.withPropertyValues("app.security.jwt.secret=corto", "app.security.jwt.expiration-minutes=30",
                        "app.security.jwt.issuer=ms-cliente-gestion")
                .run(contexto -> assertThat(contexto).hasFailed().getFailure()
                        .rootCause().hasMessageContaining("JWT_SECRET"));
    }

    @Test
    void el_arranque_debe_fallar_si_falta_el_secreto() {
        runner.withPropertyValues("app.security.jwt.expiration-minutes=30", "app.security.jwt.issuer=ms-cliente-gestion")
                .run(contexto -> assertThat(contexto).hasFailed());
    }

    @Test
    void debe_enlazar_propiedades_validas() {
        runner.withPropertyValues("app.security.jwt.secret=" + TokensDePrueba.SECRETO,
                        "app.security.jwt.expiration-minutes=15", "app.security.jwt.issuer=ms-cliente-gestion")
                .run(contexto -> assertThat(contexto.getBean(JwtProperties.class).expirationMinutes())
                        .isEqualTo(15));
    }

    @Test
    void la_expiracion_por_defecto_de_application_yml_debe_ser_15_minutos() {
        new ApplicationContextRunner().withUserConfiguration(Config.class)
                .withInitializer(new org.springframework.boot.test.context.ConfigDataApplicationContextInitializer())
                .withPropertyValues("JWT_SECRET=" + TokensDePrueba.SECRETO)
                .run(contexto -> assertThat(contexto.getBean(JwtProperties.class).expirationMinutes())
                        .isEqualTo(15));
    }
}
