package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InscricoesResourceTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void limparBanco() {
        ApoioApi.limparBanco(entityManager);
    }

    private static ValidatableResponse inscrever(Object palestraId, String nome, String email) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("nome", nome, "email", email))
                .when().post("/palestras/{id}/inscricoes", palestraId)
                .then();
    }

    @Test
    void inscreveEReduzUmaVaga() {
        int id = ApoioApi.criarPalestra(5);

        inscrever(id, "Maria Silva", "Maria@Exemplo.com ")
                .statusCode(201)
                .body("id", notNullValue())
                .body("palestraId", equalTo(id))
                .body("nome", equalTo("Maria Silva"))
                .body("email", equalTo("maria@exemplo.com"))
                .body("situacao", equalTo("ATIVA"))
                .body("dataHoraInscricao", notNullValue());

        given().when().get("/palestras/{id}", id).then().statusCode(200)
                .body("vagasDisponiveis", equalTo(4));
    }

    @Test
    void recusaPalestraLotada() {
        int id = ApoioApi.criarPalestra(1);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);

        inscrever(id, "João", "joao@exemplo.com")
                .statusCode(409)
                .body("mensagem", equalTo("Não há vagas disponíveis nesta palestra."));
    }

    @Test
    void recusaParticipanteJaInscrito() {
        int id = ApoioApi.criarPalestra(5);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);

        inscrever(id, "Maria", " MARIA@exemplo.com")
                .statusCode(409)
                .body("mensagem", equalTo("Participante já possui inscrição ativa nesta palestra."));
        given().when().get("/palestras/{id}", id).then().body("vagasDisponiveis", equalTo(4));
    }

    @Test
    void recusaDadosInvalidosComErrosPorCampo() {
        int id = ApoioApi.criarPalestra(5);

        inscrever(id, "", "invalido")
                .statusCode(400)
                .body("mensagem", equalTo("Dados inválidos."))
                .body("erros.campo", hasItem("nome"))
                .body("erros.campo", hasItem("email"));
    }

    @Test
    void recusaCorpoSemCampos() {
        int id = ApoioApi.criarPalestra(5);

        given().contentType(ContentType.JSON).body("{}")
                .when().post("/palestras/{id}/inscricoes", id)
                .then().statusCode(400)
                .body("erros.campo", hasItem("nome"))
                .body("erros.campo", hasItem("email"));
    }

    @Test
    void devolve404ParaPalestraInexistente() {
        inscrever(999, "Maria", "maria@exemplo.com")
                .statusCode(404)
                .body("mensagem", equalTo("Palestra não encontrada."));
    }

    private static ValidatableResponse cancelar(Object palestraId, String email) {
        return given().queryParam("email", email)
                .when().delete("/palestras/{id}/inscricoes", palestraId)
                .then();
    }

    @Test
    void cancelaELiberaUmaVaga() {
        int id = ApoioApi.criarPalestra(5);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);

        cancelar(id, "maria@exemplo.com").statusCode(204);

        given().when().get("/palestras/{id}", id).then().body("vagasDisponiveis", equalTo(5));
    }

    @Test
    void palestraLotadaVoltaATerVagaAposCancelamento() {
        int id = ApoioApi.criarPalestra(1);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);
        inscrever(id, "João", "joao@exemplo.com").statusCode(409);

        cancelar(id, "maria@exemplo.com").statusCode(204);

        inscrever(id, "João", "joao@exemplo.com").statusCode(201);
    }

    @Test
    void cancelarSemInscricaoAtivaDevolve404() {
        int id = ApoioApi.criarPalestra(5);

        cancelar(id, "maria@exemplo.com")
                .statusCode(404)
                .body("mensagem", equalTo("Não há inscrição ativa para este e-mail nesta palestra."));
    }

    @Test
    void cancelamentoRepetidoDevolve404SemAlterarAsVagas() {
        int id = ApoioApi.criarPalestra(5);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);
        cancelar(id, "maria@exemplo.com").statusCode(204);

        cancelar(id, "maria@exemplo.com")
                .statusCode(404)
                .body("mensagem", equalTo("Não há inscrição ativa para este e-mail nesta palestra."));
        given().when().get("/palestras/{id}", id).then().body("vagasDisponiveis", equalTo(5));
    }

    @Test
    void cancelarEmPalestraInexistenteDevolve404() {
        cancelar(999, "maria@exemplo.com")
                .statusCode(404)
                .body("mensagem", equalTo("Palestra não encontrada."));
    }

    @Test
    void cancelarSemEmailOuComEmailInvalidoDevolve400() {
        int id = ApoioApi.criarPalestra(5);

        given().when().delete("/palestras/{id}/inscricoes", id)
                .then().statusCode(400)
                .body("mensagem", equalTo("Dados inválidos."))
                .body("erros.campo", hasItem("email"));
        cancelar(id, "invalido")
                .statusCode(400)
                .body("erros.campo", hasItem("email"));
    }

    @Test
    void cancelamentoAceitaEmailComCaixaEEspacosDiferentes() {
        int id = ApoioApi.criarPalestra(5);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);

        cancelar(id, "  MARIA@Exemplo.com ").statusCode(204);
    }

    @Test
    void reinscricaoAposCancelamentoDevolve201() {
        int id = ApoioApi.criarPalestra(1);
        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);
        cancelar(id, "maria@exemplo.com").statusCode(204);

        inscrever(id, "Maria", "maria@exemplo.com").statusCode(201);

        given().when().get("/palestras/{id}", id).then().body("vagasDisponiveis", equalTo(0));
    }
}
