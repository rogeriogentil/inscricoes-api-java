package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

/**
 * Erro de validação associado a um campo.
 *
 * @param campo nome do campo com erro
 * @param mensagem motivo da recusa, em português
 */
public record ErroDeCampo(String campo, String mensagem) {
}
