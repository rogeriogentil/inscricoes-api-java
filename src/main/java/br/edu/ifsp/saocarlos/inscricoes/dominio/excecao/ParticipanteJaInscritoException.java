package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

/** Lançada quando o e-mail já possui inscrição ativa na palestra. */
public class ParticipanteJaInscritoException extends RuntimeException {

    public ParticipanteJaInscritoException() {
        super("Participante já possui inscrição ativa nesta palestra.");
    }
}
