package br.edu.ifsp.saocarlos.inscricoes.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InscricaoConcorrenciaTest {

    private static final int REQUISICOES = 100;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void limparBanco() {
        ApoioApi.limparBanco(entityManager);
    }

    /** Dispara as requisições juntas e devolve o status HTTP de cada uma. */
    private static List<Integer> dispararJuntas(int quantidade, IntFunction<Integer> requisicao) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(quantidade);
        try {
            CountDownLatch largada = new CountDownLatch(1);
            List<Future<Integer>> futuros = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                int indice = i;
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return requisicao.apply(indice);
                }));
            }
            largada.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> futuro : futuros) {
                statuses.add(futuro.get());
            }
            return statuses;
        } finally {
            executor.shutdownNow();
        }
    }

    private static int inscrever(int palestraId, String email) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Participante", "email", email))
                .when().post("/palestras/{id}/inscricoes", palestraId)
                .statusCode();
    }

    @Test
    void cemRequisicoesPelaUltimaVagaResultamEmUmaInscricao() throws Exception {
        int id = ApoioApi.criarPalestra(1);

        List<Integer> statuses = dispararJuntas(REQUISICOES, i -> inscrever(id, "participante" + i + "@exemplo.com"));

        assertEquals(1, statuses.stream().filter(s -> s == 201).count());
        assertEquals(REQUISICOES - 1, statuses.stream().filter(s -> s == 409).count());
        given().when().get("/palestras/{id}", id).then().statusCode(200)
                .body("vagasDisponiveis", org.hamcrest.Matchers.equalTo(0));
    }

    @Test
    void mesmoEmailEmRequisicoesSimultaneasGeraUmaSoInscricaoAtiva() throws Exception {
        int id = ApoioApi.criarPalestra(10);

        List<Integer> statuses = dispararJuntas(2, i -> inscrever(id, "maria@exemplo.com"));

        assertEquals(1, statuses.stream().filter(s -> s == 201).count());
        assertEquals(1, statuses.stream().filter(s -> s == 409).count());
        given().when().get("/palestras/{id}", id).then().statusCode(200)
                .body("vagasDisponiveis", org.hamcrest.Matchers.equalTo(9));
    }
}
