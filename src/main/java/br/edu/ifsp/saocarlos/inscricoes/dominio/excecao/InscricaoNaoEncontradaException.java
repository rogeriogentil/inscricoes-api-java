package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

/** Lançada quando não há inscrição ativa para o e-mail na palestra. */
public class InscricaoNaoEncontradaException extends RuntimeException {

    public InscricaoNaoEncontradaException() {
        super("Não há inscrição ativa para este e-mail nesta palestra.");
    }
}
