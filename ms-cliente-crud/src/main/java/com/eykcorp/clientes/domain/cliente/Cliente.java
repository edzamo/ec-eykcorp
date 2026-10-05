package com.eykcorp.clientes.domain.cliente;

import java.time.Instant;
import java.util.Objects;

/** Agregado Cliente. Inmutable; toda modificación devuelve una copia. */
public record Cliente(
        Long id,
        String nombres,
        String apellidos,
        Correo correo,
        Telefono telefono,
        Instant fechaCreacion) {

    static final int LONGITUD_MAXIMA_NOMBRE = 100;

    public Cliente {
        nombres = textoObligatorio(nombres, "Los nombres");
        apellidos = textoObligatorio(apellidos, "Los apellidos");
        if (correo == null) {
            throw new ValorInvalidoException("El correo es obligatorio");
        }
        Objects.requireNonNull(fechaCreacion, "fechaCreacion");
    }

    public static Cliente crear(
            String nombres, String apellidos, Correo correo, Telefono telefono, Instant ahora) {
        return crear(null, nombres, apellidos, correo, telefono, ahora);
    }

    public static Cliente crear(
            Long id, String nombres, String apellidos, Correo correo, Telefono telefono, Instant ahora) {
        return new Cliente(id, nombres, apellidos, correo, telefono, ahora);
    }

    public Cliente conDatos(String nombres, String apellidos, Correo correo, Telefono telefono) {
        return new Cliente(id, nombres, apellidos, correo, telefono, fechaCreacion);
    }

    public Cliente conId(Long nuevoId) {
        return new Cliente(nuevoId, nombres, apellidos, correo, telefono, fechaCreacion);
    }

    private static String textoObligatorio(String texto, String campo) {
        String recortado = texto == null ? "" : texto.trim();
        if (recortado.isEmpty()) {
            throw new ValorInvalidoException(campo + " no pueden estar vacíos");
        }
        if (recortado.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValorInvalidoException(
                    campo + " no pueden superar " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }
        return recortado;
    }

    /** INV-17: sin datos personales; solo el identificador. */
    @Override
    public String toString() {
        return "Cliente[id=" + id + "]";
    }
}
