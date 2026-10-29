package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import java.util.List;

/**
 * Corpo padrão de todas as recusas.
 *
 * @param mensagem motivo da recusa, em português
 * @param erros erros por campo (vazio quando não se aplica)
 */
public record ErroResposta(String mensagem, List<ErroDeCampo> erros) {

    public static ErroResposta de(String mensagem) {
        return new ErroResposta(mensagem, List.of());
    }
}
