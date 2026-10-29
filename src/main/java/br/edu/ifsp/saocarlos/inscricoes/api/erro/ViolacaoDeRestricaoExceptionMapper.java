package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Comparator;
import java.util.List;

/** Converte violações de Bean Validation nos DTOs para o mesmo formato de {@link DadosInvalidosException}. */
@Provider
public class ViolacaoDeRestricaoExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException excecao) {
        List<ErroDeCampo> erros = excecao.getConstraintViolations().stream()
                .map(violacao -> new ErroDeCampo(nomeDoCampo(violacao.getPropertyPath()), violacao.getMessage()))
                .sorted(Comparator.comparing(ErroDeCampo::campo).thenComparing(ErroDeCampo::mensagem))
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResposta(DadosInvalidosException.MENSAGEM, erros))
                .build();
    }

    private static String nomeDoCampo(Path caminho) {
        String nome = "";
        for (Path.Node no : caminho) {
            nome = no.getName();
        }
        return nome;
    }
}
