package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DadosInvalidosExceptionMapper implements ExceptionMapper<DadosInvalidosException> {

    @Override
    public Response toResponse(DadosInvalidosException excecao) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResposta(excecao.getMessage(), excecao.getErros()))
                .build();
    }
}
