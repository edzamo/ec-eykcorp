package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.domain.cliente.AccionAuditoria;
import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AuditoriaDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-05T15:30:00Z");
    private final AuditoriaDocumentMapper mapper = new AuditoriaDocumentMapper();

    @ParameterizedTest
    @EnumSource(AccionAuditoria.class)
    void debe_mapear_ida_y_vuelta_cada_accion(AccionAuditoria accion) {
        Cliente cliente = Cliente.crear(7L, "Ana", "Pérez", Correo.de("ana@example.com"),
                Telefono.de("0991234567"), AHORA);

        AuditoriaDocument documento = mapper.aDocumento(accion, cliente, AHORA);

        assertThat(documento.id()).isNull();
        assertThat(documento.clienteId()).isEqualTo(7L);
        assertThat(documento.timestamp()).isEqualTo(AHORA);
        assertThat(documento.accion()).isEqualTo(accion.name());
    }

    @Test
    void el_documento_no_contiene_datos_personales() {
        assertThat(Arrays.stream(AuditoriaDocument.class.getRecordComponents()).map(c -> c.getName()))
                .containsExactly("id", "accion", "clienteId", "timestamp");
    }
}
