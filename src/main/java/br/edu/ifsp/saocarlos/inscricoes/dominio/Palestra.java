package br.edu.ifsp.saocarlos.inscricoes.dominio;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import java.util.ArrayList;
import java.util.List;

/** Palestra do evento. Imutável após a criação. */
public final class Palestra {

    private final Long id;
    private final String titulo;
    private final String descricao;
    private final int totalVagas;

    private Palestra(Long id, String titulo, String descricao, int totalVagas) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.totalVagas = totalVagas;
    }

    /**
     * Cria uma palestra nova (ainda sem identificador), validando os dados.
     *
     * @throws DadosInvalidosException se título ou descrição forem vazios ou o total de vagas não for positivo
     */
    public static Palestra criar(String titulo, String descricao, int totalVagas) {
        List<ErroDeCampo> erros = new ArrayList<>();
        if (titulo == null || titulo.isBlank()) {
            erros.add(new ErroDeCampo("titulo", "Título é obrigatório."));
        }
        if (descricao == null || descricao.isBlank()) {
            erros.add(new ErroDeCampo("descricao", "Descrição é obrigatória."));
        }
        if (totalVagas <= 0) {
            erros.add(new ErroDeCampo("totalVagas", "Total de vagas deve ser maior que zero."));
        }
        if (!erros.isEmpty()) {
            throw new DadosInvalidosException(erros);
        }
        return new Palestra(null, titulo.trim(), descricao.trim(), totalVagas);
    }

    /** Reconstitui uma palestra já persistida, sem revalidar. */
    public static Palestra restaurar(Long id, String titulo, String descricao, int totalVagas) {
        return new Palestra(id, titulo, descricao, totalVagas);
    }

    /** Devolve a mesma palestra com o identificador atribuído pela persistência. */
    public Palestra comId(Long novoId) {
        return new Palestra(novoId, titulo, descricao, totalVagas);
    }

    public long vagasDisponiveis(long inscricoesAtivas) {
        return totalVagas - inscricoesAtivas;
    }

    public boolean temVaga(long inscricoesAtivas) {
        return vagasDisponiveis(inscricoesAtivas) > 0;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getTotalVagas() {
        return totalVagas;
    }
}
