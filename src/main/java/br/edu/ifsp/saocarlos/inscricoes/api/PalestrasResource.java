package br.edu.ifsp.saocarlos.inscricoes.api;

import br.edu.ifsp.saocarlos.inscricoes.api.dto.NovaPalestraRequisicao;
import br.edu.ifsp.saocarlos.inscricoes.api.dto.PalestraResposta;
import br.edu.ifsp.saocarlos.inscricoes.dominio.Palestra;
import br.edu.ifsp.saocarlos.inscricoes.dominio.RepositorioInscricoes;
import br.edu.ifsp.saocarlos.inscricoes.dominio.RepositorioPalestras;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraNaoEncontradaException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Path("/palestras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class PalestrasResource {

    @Inject
    RepositorioPalestras palestras;

    @Inject
    RepositorioInscricoes inscricoes;

    @POST
    public Response criar(@Valid NovaPalestraRequisicao requisicao, @Context UriInfo uriInfo) {
        Palestra palestra = palestras.salvar(
                Palestra.criar(requisicao.titulo(), requisicao.descricao(), requisicao.totalVagas()));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(palestra.getId())).build())
                .entity(PalestraResposta.de(palestra, 0))
                .build();
    }

    @GET
    public List<PalestraResposta> listar() {
        Map<Long, Long> ativasPorPalestra = inscricoes.contarAtivasPorPalestra();
        return palestras.listarTodas().stream()
                .map(palestra -> PalestraResposta.de(palestra, ativasPorPalestra.getOrDefault(palestra.getId(), 0L)))
                .toList();
    }

    @GET
    @Path("/{id}")
    public PalestraResposta detalhar(@PathParam("id") Long id) {
        Palestra palestra = palestras.buscarPorId(id).orElseThrow(PalestraNaoEncontradaException::new);
        return PalestraResposta.de(palestra, inscricoes.contarAtivas(id));
    }
}
