package com.eykcorp.clientes.domain.cliente;

import java.util.regex.Pattern;

/** Teléfono de 7 a 15 dígitos con un "+" inicial opcional. */
public record Telefono(String valor) {

    /** Fuente de verdad del formato (la reutiliza la validación del DTO web). */
    public static final String PATRON = "\\+?\\d{7,15}";

    private static final Pattern FORMATO = Pattern.compile(PATRON);

    public Telefono {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new ValorInvalidoException("El teléfono no tiene un formato válido");
        }
    }

    public static Telefono de(String valor) {
        return new Telefono(valor);
    }

    /** El teléfono es opcional: {@code null} significa "sin teléfono". */
    public static Telefono deOpcional(String valor) {
        return valor == null ? null : new Telefono(valor);
    }

    @Override
    public String toString() {
        return "Telefono[***]";
    }
}
