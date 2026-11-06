package br.edu.ifsp.saocarlos.inscricoes.infra;

import br.edu.ifsp.saocarlos.inscricoes.dominio.ServicoInscricoes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/** Instancia os serviços de domínio (sem anotações de framework) com as implementações JPA. */
@ApplicationScoped
public class ProdutorDominio {

    @Produces
    public ServicoInscricoes servicoInscricoes(RepositorioPalestrasJpa palestras, RepositorioInscricoesJpa inscricoes) {
        return new ServicoInscricoes(palestras, inscricoes);
    }
}
