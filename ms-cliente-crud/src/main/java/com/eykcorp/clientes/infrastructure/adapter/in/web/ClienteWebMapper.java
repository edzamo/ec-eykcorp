package com.eykcorp.clientes.infrastructure.adapter.in.web;

import com.eykcorp.clientes.application.port.in.DatosCliente;
import com.eykcorp.clientes.domain.cliente.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteWebMapper {

    DatosCliente aDatos(ClienteRequest peticion) {
        return new DatosCliente(
                peticion.nombres(), peticion.apellidos(), peticion.correo(), peticion.telefono());
    }

    ClienteResponse aRespuesta(Cliente cliente) {
        return new ClienteResponse(
                cliente.id(),
                cliente.nombres(),
                cliente.apellidos(),
                cliente.correo().valor(),
                cliente.telefono() == null ? null : cliente.telefono().valor(),
                cliente.fechaCreacion());
    }
}
