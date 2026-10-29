package br.edu.ifsp.saocarlos.inscricoes.infra;

import br.edu.ifsp.saocarlos.inscricoes.dominio.Palestra;
import br.edu.ifsp.saocarlos.inscricoes.dominio.RepositorioPalestras;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/** Implementação JPA/Panache da porta {@link RepositorioPalestras}. */
@ApplicationScoped
public class RepositorioPalestrasJpa implements RepositorioPalestras, PanacheRepository<PalestraEntidade> {

    @Override
    public Palestra salvar(Palestra palestra) {
        PalestraEntidade entidade = new PalestraEntidade();
        entidade.titulo = palestra.getTitulo();
        entidade.descricao = palestra.getDescricao();
        entidade.totalVagas = palestra.getTotalVagas();
        persist(entidade);
        return paraDominio(entidade);
    }

    @Override
    public Optional<Palestra> buscarPorId(Long id) {
        return findByIdOptional(id).map(RepositorioPalestrasJpa::paraDominio);
    }

    @Override
    public List<Palestra> listarTodas() {
        return listAll(Sort.by("id")).stream().map(RepositorioPalestrasJpa::paraDominio).toList();
    }

    private static Palestra paraDominio(PalestraEntidade entidade) {
        return Palestra.restaurar(entidade.id, entidade.titulo, entidade.descricao, entidade.totalVagas);
    }
}
