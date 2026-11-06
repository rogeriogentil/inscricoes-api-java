package br.edu.ifsp.saocarlos.inscricoes.api.erro;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Valor de tipo incompatível no JSON. Existe para sobrepor o mapeador embutido do Quarkus, que é mais
 * específico que {@link ProcessamentoDeCorpoExceptionMapper} e usa outro formato de erro.
 */
@Provider
public class TipoIncompativelExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException excecao) {
        return ProcessamentoDeCorpoExceptionMapper.resposta();
    }
}
