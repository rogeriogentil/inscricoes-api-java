package br.edu.ifsp.saocarlos.inscricoes.api;

import br.edu.ifsp.saocarlos.inscricoes.api.dto.InscricaoResposta;
import br.edu.ifsp.saocarlos.inscricoes.api.dto.NovaInscricaoRequisicao;
import br.edu.ifsp.saocarlos.inscricoes.dominio.Inscricao;
import br.edu.ifsp.saocarlos.inscricoes.dominio.ServicoInscricoes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/palestras/{id}/inscricoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InscricoesResource {

    @Inject
    ServicoInscricoes servico;

    /** A transação cobre todo o método para que o bloqueio da palestra dure até o commit. */
    @POST
    @Transactional
    public Response inscrever(@PathParam("id") Long palestraId, @Valid NovaInscricaoRequisicao requisicao) {
        Inscricao inscricao = servico.inscrever(palestraId, requisicao.nome(), requisicao.email());
        return Response.status(Response.Status.CREATED).entity(InscricaoResposta.de(inscricao)).build();
    }
}
