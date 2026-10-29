package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import br.edu.ifsp.saocarlos.inscricoes.infra.InscricaoEntidade;
import br.edu.ifsp.saocarlos.inscricoes.infra.PalestraEntidade;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PalestrasResourceTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void limparBanco() {
        QuarkusTransaction.requiringNew().run(() -> {
            entityManager.createQuery("delete from InscricaoEntidade").executeUpdate();
            entityManager.createQuery("delete from PalestraEntidade").executeUpdate();
        });
    }

    private static Map<String, Object> palestraValida(int totalVagas) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("titulo", "Quarkus na prática");
        corpo.put("descricao", "Introdução ao Quarkus");
        corpo.put("totalVagas", totalVagas);
        return corpo;
    }

    private static int criarPalestra(int totalVagas) {
        return given().contentType(ContentType.JSON).body(palestraValida(totalVagas))
                .when().post("/palestras")
                .then().statusCode(201)
                .extract().path("id");
    }

    private void inserirInscricoes(int palestraId, SituacaoInscricao situacao, int quantidade) {
        QuarkusTransaction.requiringNew().run(() -> {
            PalestraEntidade palestra = entityManager.getReference(PalestraEntidade.class, (long) palestraId);
            for (int i = 0; i < quantidade; i++) {
                InscricaoEntidade inscricao = new InscricaoEntidade();
                inscricao.palestra = palestra;
                inscricao.nome = "Participante " + situacao + i;
                inscricao.email = situacao.name().toLowerCase() + i + "@exemplo.com";
                inscricao.dataHoraInscricao = Instant.now();
                inscricao.situacao = situacao;
                if (situacao == SituacaoInscricao.CANCELADA) {
                    inscricao.dataHoraCancelamento = Instant.now();
                }
                entityManager.persist(inscricao);
            }
        });
    }

    @Test
    void criaPalestraValida() {
        given().contentType(ContentType.JSON).body(palestraValida(50))
                .when().post("/palestras")
                .then()
                .statusCode(201)
                .header("Location", endsWith("/palestras/" + obterUltimoId()))
                .body("id", notNullValue())
                .body("titulo", equalTo("Quarkus na prática"))
                .body("descricao", equalTo("Introdução ao Quarkus"))
                .body("totalVagas", equalTo(50))
                .body("vagasDisponiveis", equalTo(50));
    }

    private long obterUltimoId() {
        return QuarkusTransaction.requiringNew().call(() ->
                entityManager.createQuery("select max(p.id) from PalestraEntidade p", Long.class).getSingleResult());
    }

    @Test
    void rejeitaTituloVazio() {
        Map<String, Object> corpo = palestraValida(10);
        corpo.put("titulo", "");
        verificarDadosInvalidos(corpo, "titulo");
    }

    @Test
    void rejeitaDescricaoVazia() {
        Map<String, Object> corpo = palestraValida(10);
        corpo.put("descricao", "  ");
        verificarDadosInvalidos(corpo, "descricao");
    }

    @Test
    void rejeitaTotalDeVagasZero() {
        verificarDadosInvalidos(palestraValida(0), "totalVagas");
    }

    @Test
    void rejeitaTotalDeVagasNegativo() {
        verificarDadosInvalidos(palestraValida(-5), "totalVagas");
    }

    @Test
    void rejeitaTotalDeVagasAusente() {
        Map<String, Object> corpo = palestraValida(10);
        corpo.remove("totalVagas");
        verificarDadosInvalidos(corpo, "totalVagas");
    }

    private void verificarDadosInvalidos(Map<String, Object> corpo, String campo) {
        given().contentType(ContentType.JSON).body(corpo)
                .when().post("/palestras")
                .then()
                .statusCode(400)
                .body("mensagem", equalTo("Dados inválidos."))
                .body("erros.campo", hasItem(campo));
    }

    @Test
    void listaPalestrasOrdenadasPorId() {
        int primeira = criarPalestra(10);
        int segunda = criarPalestra(20);
        int terceira = criarPalestra(30);

        given().when().get("/palestras")
                .then()
                .statusCode(200)
                .body("id", contains(primeira, segunda, terceira))
                .body("totalVagas", contains(10, 20, 30))
                .body("vagasDisponiveis", contains(10, 20, 30));
    }

    @Test
    void listaVaziaQuandoNaoHaPalestras() {
        given().when().get("/palestras")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    void detalhaPalestra() {
        int id = criarPalestra(15);

        given().when().get("/palestras/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("titulo", equalTo("Quarkus na prática"))
                .body("totalVagas", equalTo(15))
                .body("vagasDisponiveis", equalTo(15));
    }

    @Test
    void devolve404ParaPalestraInexistente() {
        given().when().get("/palestras/999")
                .then()
                .statusCode(404)
                .body("mensagem", equalTo("Palestra não encontrada."));
    }

    @Test
    void inscricoesAtivasReduzemAsVagasDisponiveis() {
        int id = criarPalestra(50);
        inserirInscricoes(id, SituacaoInscricao.ATIVA, 20);

        given().when().get("/palestras/{id}", id).then().statusCode(200)
                .body("vagasDisponiveis", equalTo(30));
        given().when().get("/palestras").then().statusCode(200)
                .body("", hasSize(1))
                .body("[0].vagasDisponiveis", equalTo(30));
    }

    @Test
    void inscricoesCanceladasNaoReduzemAsVagas() {
        int id = criarPalestra(50);
        inserirInscricoes(id, SituacaoInscricao.ATIVA, 5);
        inserirInscricoes(id, SituacaoInscricao.CANCELADA, 10);

        given().when().get("/palestras/{id}", id).then().statusCode(200)
                .body("vagasDisponiveis", equalTo(45));
    }
}
