package br.edu.ifsp.saocarlos.inscricoes.dominio;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Inscrição de um participante em uma palestra. */
public final class Inscricao {

    private final Long id;
    private final Long palestraId;
    private final String nome;
    private final Email email;
    private final Instant dataHoraInscricao;
    private final SituacaoInscricao situacao;
    private final Instant dataHoraCancelamento;

    private Inscricao(Long id, Long palestraId, String nome, Email email, Instant dataHoraInscricao,
            SituacaoInscricao situacao, Instant dataHoraCancelamento) {
        this.id = id;
        this.palestraId = palestraId;
        this.nome = nome;
        this.email = email;
        this.dataHoraInscricao = dataHoraInscricao;
        this.situacao = situacao;
        this.dataHoraCancelamento = dataHoraCancelamento;
    }

    /**
     * Cria uma inscrição ativa, acumulando os erros de nome e e-mail.
     *
     * @throws DadosInvalidosException se o nome for vazio ou o e-mail for inválido
     */
    public static Inscricao criar(Long palestraId, String nome, String email, Instant agora) {
        List<ErroDeCampo> erros = new ArrayList<>();
        if (nome == null || nome.isBlank()) {
            erros.add(new ErroDeCampo("nome", "Nome é obrigatório."));
        }
        Email emailNormalizado = null;
        try {
            emailNormalizado = Email.de(email);
        } catch (DadosInvalidosException excecao) {
            erros.addAll(excecao.getErros());
        }
        if (!erros.isEmpty()) {
            throw new DadosInvalidosException(erros);
        }
        return new Inscricao(null, palestraId, nome.trim(), emailNormalizado, agora, SituacaoInscricao.ATIVA, null);
    }

    /** Reconstitui uma inscrição já persistida, sem revalidar. */
    public static Inscricao restaurar(Long id, Long palestraId, String nome, Email email, Instant dataHoraInscricao,
            SituacaoInscricao situacao, Instant dataHoraCancelamento) {
        return new Inscricao(id, palestraId, nome, email, dataHoraInscricao, situacao, dataHoraCancelamento);
    }

    /** Devolve a mesma inscrição com o identificador atribuído pela persistência. */
    public Inscricao comId(Long novoId) {
        return new Inscricao(novoId, palestraId, nome, email, dataHoraInscricao, situacao, dataHoraCancelamento);
    }

    /**
     * Devolve a inscrição cancelada (estado final, mantida como histórico).
     *
     * @throws IllegalStateException se a inscrição já estiver cancelada
     */
    public Inscricao cancelar(Instant agora) {
        if (situacao != SituacaoInscricao.ATIVA) {
            throw new IllegalStateException("Inscrição já cancelada.");
        }
        return new Inscricao(id, palestraId, nome, email, dataHoraInscricao, SituacaoInscricao.CANCELADA, agora);
    }

    public Long getId() {
        return id;
    }

    public Long getPalestraId() {
        return palestraId;
    }

    public String getNome() {
        return nome;
    }

    public Email getEmail() {
        return email;
    }

    public Instant getDataHoraInscricao() {
        return dataHoraInscricao;
    }

    public SituacaoInscricao getSituacao() {
        return situacao;
    }

    public Instant getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }
}
