package br.edu.ifsp.saocarlos.inscricoes.api.dto;

import br.edu.ifsp.saocarlos.inscricoes.dominio.Palestra;

/** Representação JSON de uma palestra. */
public record PalestraResposta(Long id, String titulo, String descricao, int totalVagas, long vagasDisponiveis) {

    public static PalestraResposta de(Palestra palestra, long inscricoesAtivas) {
        return new PalestraResposta(palestra.getId(), palestra.getTitulo(), palestra.getDescricao(),
                palestra.getTotalVagas(), palestra.vagasDisponiveis(inscricoesAtivas));
    }
}
