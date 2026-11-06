package br.edu.ifsp.saocarlos.inscricoes.dominio;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.InscricaoNaoEncontradaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraLotadaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraNaoEncontradaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ParticipanteJaInscritoException;
import java.time.Instant;

/**
 * Regras de inscrição. Deve ser chamado dentro de uma transação: o bloqueio da palestra garante o limite de
 * vagas e a unicidade da inscrição ativa por e-mail até o commit.
 */
public class ServicoInscricoes {

    private final RepositorioPalestras palestras;
    private final RepositorioInscricoes inscricoes;

    public ServicoInscricoes(RepositorioPalestras palestras, RepositorioInscricoes inscricoes) {
        this.palestras = palestras;
        this.inscricoes = inscricoes;
    }

    /**
     * Inscreve o participante na palestra.
     *
     * @throws br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException nome vazio ou e-mail inválido
     * @throws PalestraNaoEncontradaException palestra inexistente
     * @throws PalestraLotadaException não há vagas
     * @throws ParticipanteJaInscritoException o e-mail já tem inscrição ativa na palestra
     */
    public Inscricao inscrever(Long palestraId, String nome, String email) {
        Inscricao nova = Inscricao.criar(palestraId, nome, email, Instant.now());
        Palestra palestra = palestras.buscarParaAtualizacao(palestraId)
                .orElseThrow(PalestraNaoEncontradaException::new);
        if (!palestra.temVaga(inscricoes.contarAtivas(palestraId))) {
            throw new PalestraLotadaException();
        }
        if (inscricoes.buscarAtiva(palestraId, nova.getEmail()).isPresent()) {
            throw new ParticipanteJaInscritoException();
        }
        return inscricoes.salvar(nova);
    }

    /**
     * Cancela a inscrição ativa do e-mail na palestra, liberando a vaga.
     *
     * @throws br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException e-mail inválido
     * @throws PalestraNaoEncontradaException palestra inexistente
     * @throws InscricaoNaoEncontradaException não há inscrição ativa para o e-mail
     */
    public Inscricao cancelar(Long palestraId, String email) {
        Email emailNormalizado = Email.de(email);
        palestras.buscarParaAtualizacao(palestraId).orElseThrow(PalestraNaoEncontradaException::new);
        Inscricao ativa = inscricoes.buscarAtiva(palestraId, emailNormalizado)
                .orElseThrow(InscricaoNaoEncontradaException::new);
        return inscricoes.salvar(ativa.cancelar(Instant.now()));
    }
}
