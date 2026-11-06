package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

/** Lançada quando a palestra não tem mais vagas. */
public class PalestraLotadaException extends RuntimeException {

    public PalestraLotadaException() {
        super("Não há vagas disponíveis nesta palestra.");
    }
}
