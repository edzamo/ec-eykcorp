package com.eykcorp.clientes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.application.port.in.DatosCliente;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ClienteWebMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private final ClienteWebMapper mapper = new ClienteWebMapper();

    @Test
    void debe_convertir_la_peticion_en_comando() {
        DatosCliente datos = mapper.aDatos(new ClienteRequest("Ana", "Pérez", "ana@example.com", "0991234567"));

        assertThat(datos).isEqualTo(new DatosCliente("Ana", "Pérez", "ana@example.com", "0991234567"));
    }

    @Test
    void debe_convertir_el_cliente_en_respuesta() {
        Cliente cliente = Cliente.crear(1L, "Ana", "Pérez", Correo.de("ana@example.com"),
                Telefono.de("0991234567"), AHORA);

        assertThat(mapper.aRespuesta(cliente)).isEqualTo(
                new ClienteResponse(1L, "Ana", "Pérez", "ana@example.com", "0991234567", AHORA));
    }

    @Test
    void debe_responder_telefono_nulo_cuando_el_cliente_no_tiene() {
        Cliente cliente = Cliente.crear(1L, "Ana", "Pérez", Correo.de("ana@example.com"), null, AHORA);

        assertThat(mapper.aRespuesta(cliente).telefono()).isNull();
    }
}
