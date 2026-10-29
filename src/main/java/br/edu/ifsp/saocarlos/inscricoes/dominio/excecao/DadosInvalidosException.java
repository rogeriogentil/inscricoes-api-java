package br.edu.ifsp.saocarlos.inscricoes.dominio.excecao;

import java.util.List;

/** Lançada quando os dados informados violam as regras de validação do domínio. */
public class DadosInvalidosException extends RuntimeException {

    public static final String MENSAGEM = "Dados inválidos.";

    private final List<ErroDeCampo> erros;

    public DadosInvalidosException(List<ErroDeCampo> erros) {
        super(MENSAGEM);
        this.erros = List.copyOf(erros);
    }

    public List<ErroDeCampo> getErros() {
        return erros;
    }
}
