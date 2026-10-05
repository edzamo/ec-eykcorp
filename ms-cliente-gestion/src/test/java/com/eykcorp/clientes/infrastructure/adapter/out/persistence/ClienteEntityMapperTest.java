package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
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
}
