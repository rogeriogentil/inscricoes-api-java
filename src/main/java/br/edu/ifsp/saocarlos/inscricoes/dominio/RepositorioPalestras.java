package br.edu.ifsp.saocarlos.inscricoes.dominio;

import java.util.List;
import java.util.Optional;

/** Porta de persistência das palestras. */
public interface RepositorioPalestras {

    /** Persiste a palestra e a devolve com o identificador atribuído. */
    Palestra salvar(Palestra palestra);

    Optional<Palestra> buscarPorId(Long id);

    /** Todas as palestras, ordenadas por identificador. */
    List<Palestra> listarTodas();
}
