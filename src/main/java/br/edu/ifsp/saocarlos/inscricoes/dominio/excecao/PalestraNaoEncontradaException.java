package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

/** Lançada quando a palestra solicitada não existe. */
public class PalestraNaoEncontradaException extends RuntimeException {

    public PalestraNaoEncontradaException() {
        super("Palestra não encontrada.");
    }
}
