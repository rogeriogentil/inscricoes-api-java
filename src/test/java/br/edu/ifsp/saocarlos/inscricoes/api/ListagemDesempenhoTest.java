package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import br.edu.ifsp.saocarlos.inscricoes.infra.InscricaoEntidade;
import br.edu.ifsp.saocarlos.inscricoes.infra.PalestraEntidade;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ListagemDesempenhoTest {

    private static final int PALESTRAS = 200;
    private static final long LIMITE_MILISSEGUNDOS = 2000;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void limparBanco() {
        ApoioApi.limparBanco(entityManager);
    }

    /** Cria as palestras com inscrições variadas e devolve as vagas esperadas por título. */
    private Map<String, Long> popularBanco() {
        Map<String, Long> vagasEsperadas = new HashMap<>();
        QuarkusTransaction.requiringNew().run(() -> {
            for (int i = 0; i < PALESTRAS; i++) {
                PalestraEntidade palestra = new PalestraEntidade();
                palestra.titulo = "Palestra " + i;
                palestra.descricao = "Descrição " + i;
                palestra.totalVagas = 10 + (i % 5) * 10;
                entityManager.persist(palestra);

                int ativas = i % 7;
                int canceladas = i % 3;
                for (int j = 0; j < ativas + canceladas; j++) {
                    InscricaoEntidade inscricao = new InscricaoEntidade();
                    inscricao.palestra = palestra;
                    inscricao.nome = "Participante " + j;
                    inscricao.email = "p" + j + "@palestra" + i + ".com";
                    inscricao.dataHoraInscricao = Instant.now();
                    inscricao.situacao = j < ativas ? SituacaoInscricao.ATIVA : SituacaoInscricao.CANCELADA;
                    if (inscricao.situacao == SituacaoInscricao.CANCELADA) {
                        inscricao.dataHoraCancelamento = Instant.now();
                    }
                    entityManager.persist(inscricao);
                }
                vagasEsperadas.put(palestra.titulo, (long) palestra.totalVagas - ativas);
            }
        });
        return vagasEsperadas;
    }

    @Test
    void listaDuzentasPalestrasEmAteDoisSegundosComVagasCorretas() {
        Map<String, Long> vagasEsperadas = popularBanco();

        Response resposta = given().when().get("/palestras");

        assertTrue(resposta.time() <= LIMITE_MILISSEGUNDOS,
                "A listagem levou " + resposta.time() + " ms");
        assertEquals(200, resposta.statusCode());
        JsonPath json = resposta.jsonPath();
        List<String> titulos = json.getList("titulo");
        List<Integer> vagas = json.getList("vagasDisponiveis");
        assertEquals(PALESTRAS, titulos.size());
        for (int i = 0; i < PALESTRAS; i++) {
            assertEquals(vagasEsperadas.get(titulos.get(i)), vagas.get(i).longValue(), titulos.get(i));
        }
    }
}
