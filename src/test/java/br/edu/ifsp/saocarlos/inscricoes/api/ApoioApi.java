package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import java.util.Map;

/** Operações compartilhadas pelos testes de API. */
final class ApoioApi {

    private ApoioApi() {
    }

    static void limparBanco(EntityManager entityManager) {
        QuarkusTransaction.requiringNew().run(() -> {
            entityManager.createQuery("delete from InscricaoEntidade").executeUpdate();
            entityManager.createQuery("delete from PalestraEntidade").executeUpdate();
        });
    }

    static int criarPalestra(int totalVagas) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("titulo", "Quarkus na prática", "descricao", "Introdução ao Quarkus",
                        "totalVagas", totalVagas))
                .when().post("/palestras")
                .then().statusCode(201)
                .extract().path("id");
    }
}
