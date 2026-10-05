package com.eykcorp.clientes.infrastructure.adapter.in.web.mapper;

import com.eykcorp.clientes.infrastructure.adapter.in.web.dto.request.ClienteRequest;
import com.eykcorp.clientes.infrastructure.adapter.in.web.dto.response.ClienteResponse;

import com.eykcorp.clientes.application.command.DatosCliente;
import com.eykcorp.clientes.domain.cliente.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteWebMapper {

    public DatosCliente aDatos(ClienteRequest peticion) {
        return new DatosCliente(
                peticion.nombres(), peticion.apellidos(), peticion.correo(), peticion.telefono());
    }

    public ClienteResponse aRespuesta(Cliente cliente) {
        return new ClienteResponse(
                cliente.id(),
                cliente.nombres(),
                cliente.apellidos(),
                cliente.correo().valor(),
                cliente.telefono() == null ? null : cliente.telefono().valor(),
                cliente.fechaCreacion());
    }
}
