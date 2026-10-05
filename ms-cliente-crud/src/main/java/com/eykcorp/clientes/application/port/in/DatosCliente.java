package com.eykcorp.clientes.application.port.in;

/** Comando con los datos editables de un cliente (valores sin validar: el dominio los valida). */
public record DatosCliente(String nombres, String apellidos, String correo, String telefono) {

    /** INV-17: sin datos personales. */
    @Override
    public String toString() {
        return "DatosCliente[***]";
    }
}
