package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraNaoEncontradaException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PalestraNaoEncontradaExceptionMapper implements ExceptionMapper<PalestraNaoEncontradaException> {

    @Override
    public Response toResponse(PalestraNaoEncontradaException excecao) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResposta.de(excecao.getMessage()))
                .build();
    }
}
