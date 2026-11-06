package br.edu.ifsp.saocarlos.inscricoes.dominio;

import java.util.Map;
import java.util.Optional;

/** Porta de persistência das inscrições. */
public interface RepositorioInscricoes {

    /** Persiste a inscrição e a devolve com o identificador atribuído. */
    Inscricao salvar(Inscricao inscricao);

    Optional<Inscricao> buscarAtiva(Long palestraId, Email email);

    long contarAtivas(Long palestraId);

    /** Inscrições ativas por palestra, em uma única consulta. Palestras sem inscrição ativa ficam fora do mapa. */
    Map<Long, Long> contarAtivasPorPalestra();
}
