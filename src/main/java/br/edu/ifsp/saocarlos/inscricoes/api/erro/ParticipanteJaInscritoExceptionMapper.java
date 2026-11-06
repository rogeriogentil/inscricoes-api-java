package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ParticipanteJaInscritoException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ParticipanteJaInscritoExceptionMapper implements ExceptionMapper<ParticipanteJaInscritoException> {

    @Override
    public Response toResponse(ParticipanteJaInscritoException excecao) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResposta.de(excecao.getMessage()))
                .build();
    }
}
