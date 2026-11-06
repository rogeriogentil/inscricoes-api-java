package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Toda recusa deve usar o formato { "mensagem", "erros" } em português (FR-014, SC-004). */
@QuarkusTest
class ErrosTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void limparBanco() {
        ApoioApi.limparBanco(entityManager);
    }

    private static void verificarFormato(ValidatableResponse resposta, int status) {
        resposta.statusCode(status)
                .contentType(ContentType.JSON)
                .body("mensagem", not(blankOrNullString()))
                .body("erros", notNullValue());
    }

    @Test
    void jsonMalformadoNaCriacaoDePalestra() {
        verificarFormato(given().contentType(ContentType.JSON).body("{titulo:")
                .when().post("/palestras").then(), 400);
    }

    @Test
    void jsonMalformadoNaInscricao() {
        int id = ApoioApi.criarPalestra(5);

        verificarFormato(given().contentType(ContentType.JSON).body("não é json")
                .when().post("/palestras/{id}/inscricoes", id).then(), 400);
    }

    @Test
    void tipoDeValorIncompativelNoCorpo() {
        verificarFormato(given().contentType(ContentType.JSON)
                .body("{\"titulo\":\"a\",\"descricao\":\"b\",\"totalVagas\":\"muitas\"}")
                .when().post("/palestras").then(), 400);
    }

    @Test
    void contentTypeInvalidoNaCriacaoDePalestra() {
        verificarFormato(given().contentType(ContentType.TEXT).body("titulo=a")
                .when().post("/palestras").then(), 415);
    }

    @Test
    void contentTypeInvalidoNaInscricao() {
        int id = ApoioApi.criarPalestra(5);

        verificarFormato(given().contentType(ContentType.TEXT).body("nome=a")
                .when().post("/palestras/{id}/inscricoes", id).then(), 415);
    }

    @Test
    void corpoAusenteNaCriacaoDePalestra() {
        verificarFormato(given().contentType(ContentType.JSON)
                .when().post("/palestras").then(), 400);
    }

    @Test
    void corpoAusenteNaInscricao() {
        int id = ApoioApi.criarPalestra(5);

        verificarFormato(given().contentType(ContentType.JSON)
                .when().post("/palestras/{id}/inscricoes", id).then(), 400);
    }

    @Test
    void cancelamentoSemEmailTrazMotivoPorCampo() {
        int id = ApoioApi.criarPalestra(5);

        given().when().delete("/palestras/{id}/inscricoes", id)
                .then().statusCode(400)
                .body("mensagem", equalTo("Dados inválidos."))
                .body("erros.campo", hasItem("email"));
    }

    @Test
    void idDePalestraNaoNumerico() {
        verificarFormato(given().when().get("/palestras/abc").then(), 404);
        verificarFormato(given().queryParam("email", "maria@exemplo.com")
                .when().delete("/palestras/abc/inscricoes").then(), 404);
        verificarFormato(given().contentType(ContentType.JSON).body("{\"nome\":\"a\",\"email\":\"a@b.com\"}")
                .when().post("/palestras/abc/inscricoes").then(), 404);
    }
}
