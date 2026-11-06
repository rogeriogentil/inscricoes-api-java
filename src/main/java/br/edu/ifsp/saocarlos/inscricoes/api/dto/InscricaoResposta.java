package br.edu.ifsp.saocarlos.inscricoes.api.dto;

import br.edu.ifsp.saocarlos.inscricoes.dominio.Inscricao;
import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import java.time.Instant;

/** Representação JSON de uma inscrição. */
public record InscricaoResposta(Long id, Long palestraId, String nome, String email, SituacaoInscricao situacao,
        Instant dataHoraInscricao) {

    public static InscricaoResposta de(Inscricao inscricao) {
        return new InscricaoResposta(inscricao.getId(), inscricao.getPalestraId(), inscricao.getNome(),
                inscricao.getEmail().getValor(), inscricao.getSituacao(), inscricao.getDataHoraInscricao());
    }
}
