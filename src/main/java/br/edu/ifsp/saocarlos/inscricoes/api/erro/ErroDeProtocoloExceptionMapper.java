package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Erros de protocolo lançados pelo JAX-RS (tipo de conteúdo, id mal formado, método): mesmo formato de erro. */
@Provider
public class ErroDeProtocoloExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException excecao) {
        int status = excecao.getResponse().getStatus();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResposta.de(mensagem(status)))
                .build();
    }

    private static String mensagem(int status) {
        return switch (status) {
            case 400 -> DadosInvalidosException.MENSAGEM;
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método não permitido.";
            case 406 -> "Formato de resposta não aceito. Use application/json.";
            case 415 -> "Tipo de conteúdo não suportado. Use application/json.";
            default -> status >= 500 ? "Erro interno." : "Requisição não pôde ser atendida.";
        };
    }
}
