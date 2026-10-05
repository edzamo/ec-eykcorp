package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import com.eykcorp.clientes.domain.cliente.Telefono;
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
        entidad.setFechaCreacion(cliente.fechaCreacion());
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
