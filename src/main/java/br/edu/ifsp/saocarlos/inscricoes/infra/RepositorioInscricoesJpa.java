package br.edu.ifsp.saocarlos.inscricoes.infra;

import br.edu.ifsp.saocarlos.inscricoes.dominio.RepositorioInscricoes;
import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/** Implementação JPA/Panache da porta {@link RepositorioInscricoes}. */
@ApplicationScoped
public class RepositorioInscricoesJpa implements RepositorioInscricoes, PanacheRepository<InscricaoEntidade> {

    @Override
    public long contarAtivas(Long palestraId) {
        return count("palestra.id = ?1 and situacao = ?2", palestraId, SituacaoInscricao.ATIVA);
    }

    @Override
    public Map<Long, Long> contarAtivasPorPalestra() {
        Map<Long, Long> contagem = new HashMap<>();
        getEntityManager()
                .createQuery("select i.palestra.id, count(i) from InscricaoEntidade i "
                        + "where i.situacao = ?1 group by i.palestra.id", Object[].class)
                .setParameter(1, SituacaoInscricao.ATIVA)
                .getResultList()
                .forEach(linha -> contagem.put((Long) linha[0], (Long) linha[1]));
        return contagem;
    }
}
