package br.edu.ifsp.saocarlos.inscricoes.infra;

import br.edu.ifsp.saocarlos.inscricoes.dominio.RepositorioInscricoes;
import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import br.edu.ifsp.saocarlos.inscricoes.dominio.Email;
import br.edu.ifsp.saocarlos.inscricoes.dominio.Inscricao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Implementação JPA/Panache da porta {@link RepositorioInscricoes}. */
@ApplicationScoped
public class RepositorioInscricoesJpa implements RepositorioInscricoes, PanacheRepository<InscricaoEntidade> {

    @Override
    public Inscricao salvar(Inscricao inscricao) {
        boolean nova = inscricao.getId() == null;
        InscricaoEntidade entidade = nova ? new InscricaoEntidade() : findById(inscricao.getId());
        entidade.palestra = getEntityManager().getReference(PalestraEntidade.class, inscricao.getPalestraId());
        entidade.nome = inscricao.getNome();
        entidade.email = inscricao.getEmail().getValor();
        entidade.dataHoraInscricao = inscricao.getDataHoraInscricao();
        entidade.situacao = inscricao.getSituacao();
        entidade.dataHoraCancelamento = inscricao.getDataHoraCancelamento();
        if (nova) {
            persist(entidade);
        }
        return paraDominio(entidade);
    }

    @Override
    public Optional<Inscricao> buscarAtiva(Long palestraId, Email email) {
        return find("palestra.id = ?1 and email = ?2 and situacao = ?3",
                palestraId, email.getValor(), SituacaoInscricao.ATIVA)
                .firstResultOptional()
                .map(RepositorioInscricoesJpa::paraDominio);
    }

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

    private static Inscricao paraDominio(InscricaoEntidade entidade) {
        return Inscricao.restaurar(entidade.id, entidade.palestra.id, entidade.nome, Email.de(entidade.email),
                entidade.dataHoraInscricao, entidade.situacao, entidade.dataHoraCancelamento);
    }
}
