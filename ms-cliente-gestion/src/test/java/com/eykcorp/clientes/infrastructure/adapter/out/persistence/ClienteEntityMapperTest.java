package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class ClienteEntityMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private final ClienteEntityMapper mapper = new ClienteEntityMapper();

    @Test
    void ida_y_vuelta_debe_conservar_todos_los_campos() {
        Cliente original = Cliente.crear(5L, "Ana", "Pérez", Correo.de("ana@example.com"),
                Telefono.de("0991234567"), AHORA);

        assertThat(mapper.aDominio(mapper.aEntidad(original))).isEqualTo(original);
    }

    @Test
    void ida_y_vuelta_debe_conservar_telefono_nulo_e_id_nulo() {
        Cliente original = Cliente.crear("Ana", "Pérez", Correo.de("ana@example.com"), null, AHORA);
        ClienteEntity entidad = mapper.aEntidad(original);

        assertThat(entidad.getTelefono()).isNull();
        assertThat(entidad.getId()).isNull();
        assertThat(mapper.aDominio(entidad)).isEqualTo(original);
    }

    @Test
    void debe_mapear_a_las_columnas_en_texto_plano() {
        ClienteEntity entidad = mapper.aEntidad(Cliente.crear(2L, "Ana", "Pérez",
                Correo.de("Ana@Example.com"), Telefono.de("+593991234567"), AHORA));

        assertThat(entidad.getCorreo()).isEqualTo("ana@example.com");
        assertThat(entidad.getTelefono()).isEqualTo("+593991234567");
        assertThat(entidad.getFechaCreacion()).isEqualTo(AHORA);
    }

    @Test
    void debe_truncar_la_fecha_a_microsegundos_porque_postgresql_no_guarda_nanosegundos() {
        Instant conNanos = Instant.parse("2026-10-05T17:17:02.370486297Z");
        Cliente cliente = Cliente.crear(1L, "Ana", "Pérez", Correo.de("ana@example.com"), null, conNanos);

        ClienteEntity entidad = mapper.aEntidad(cliente);

        assertThat(entidad.getFechaCreacion())
                .isEqualTo(conNanos.truncatedTo(ChronoUnit.MICROS))
                .isEqualTo(Instant.parse("2026-10-05T17:17:02.370486Z"));
    }
}
