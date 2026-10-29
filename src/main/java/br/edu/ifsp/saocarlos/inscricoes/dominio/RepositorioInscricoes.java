package br.edu.ifsp.saocarlos.inscricoes.dominio;

import java.util.Map;

/** Porta de persistência das inscrições. */
public interface RepositorioInscricoes {

    long contarAtivas(Long palestraId);

    /** Inscrições ativas por palestra, em uma única consulta. Palestras sem inscrição ativa ficam fora do mapa. */
    Map<Long, Long> contarAtivasPorPalestra();
}
