package com.eykcorp.clientes.domain.cliente;

import java.util.Locale;
import java.util.regex.Pattern;

/** Correo electrónico normalizado (recortado y en minúsculas). */
public record Correo(String valor) {

    static final int LONGITUD_MAXIMA = 254;
    private static final Pattern FORMATO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)+$");

    public Correo {
        String normalizado = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (normalizado.length() > LONGITUD_MAXIMA || !FORMATO.matcher(normalizado).matches()) {
            throw new ValorInvalidoException("El correo no tiene un formato válido");
        }
        valor = normalizado;
    }

    public static Correo de(String valor) {
        return new Correo(valor);
    }

    @Override
    public String toString() {
        return "Correo[***" + valor.substring(valor.indexOf('@')) + "]";
    }
}
