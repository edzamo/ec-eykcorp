package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/** Mapeo manual dominio <-> persistencia (D-04), con prueba de ida y vuelta. */
@Component
public class ClienteEntityMapper {

    ClienteEntity aEntidad(Cliente cliente) {
        ClienteEntity entidad = new ClienteEntity();
        entidad.setId(cliente.id());
        entidad.setNombres(cliente.nombres());
        entidad.setApellidos(cliente.apellidos());
        entidad.setCorreo(cliente.correo().valor());
        entidad.setTelefono(cliente.telefono() == null ? null : cliente.telefono().valor());
        // PostgreSQL (TIMESTAMPTZ) guarda microsegundos; un reloj con nanosegundos (Linux) haría que
        // el POST y un GET posterior devolvieran valores distintos.
        entidad.setFechaCreacion(cliente.fechaCreacion().truncatedTo(ChronoUnit.MICROS));
        return entidad;
    }

    Cliente aDominio(ClienteEntity entidad) {
        return Cliente.crear(
                entidad.getId(),
                entidad.getNombres(),
                entidad.getApellidos(),
                Correo.de(entidad.getCorreo()),
                Telefono.deOpcional(entidad.getTelefono()),
                entidad.getFechaCreacion());
    }
}
