package br.gov.saude.sinan.util;

import java.util.Locale;

public final class Texto {

    private Texto() {
    }

    /**
     * Normaliza um texto para comparação: remove espaços nas pontas, colapsa espaços
     * repetidos e ignora diferenças de maiúsculas/minúsculas.
     * Retorna null quando o texto é nulo ou em branco.
     */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return limpo.isEmpty() ? null : limpo;
    }

    public static String maiusculas(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim().toUpperCase(Locale.ROOT);
    }
}
