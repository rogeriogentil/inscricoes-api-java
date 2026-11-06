package br.edu.ifsp.saocarlos.inscricoes.dominio;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** E-mail normalizado (sem espaços nas pontas e em minúsculas). A igualdade usa o valor normalizado. */
public final class Email {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String valor;

    private Email(String valor) {
        this.valor = valor;
    }

    /**
     * Normaliza e valida o e-mail.
     *
     * @throws DadosInvalidosException se o formato for inválido
     */
    public static Email de(String texto) {
        String normalizado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(normalizado).matches()) {
            throw new DadosInvalidosException(List.of(new ErroDeCampo("email", "E-mail em formato inválido.")));
        }
        return new Email(normalizado);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Email email && valor.equals(email.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
