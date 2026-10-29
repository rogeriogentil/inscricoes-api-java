package br.edu.ifsp.saocarlos.inscricoes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import java.util.List;
import org.junit.jupiter.api.Test;

class PalestraTest {

    private static List<String> camposComErro(DadosInvalidosException excecao) {
        return excecao.getErros().stream().map(ErroDeCampo::campo).toList();
    }

    @Test
    void criaPalestraValidaSemIdEComTextoSemEspacosNasPontas() {
        Palestra palestra = Palestra.criar("  Quarkus na prática ", " Introdução ao Quarkus  ", 50);

        assertNull(palestra.getId());
        assertEquals("Quarkus na prática", palestra.getTitulo());
        assertEquals("Introdução ao Quarkus", palestra.getDescricao());
        assertEquals(50, palestra.getTotalVagas());
    }

    @Test
    void rejeitaTituloNuloOuVazio() {
        for (String titulo : new String[] {null, "", "   "}) {
            DadosInvalidosException excecao =
                    assertThrows(DadosInvalidosException.class, () -> Palestra.criar(titulo, "Descrição", 10));
            assertEquals(List.of("titulo"), camposComErro(excecao));
        }
    }

    @Test
    void rejeitaDescricaoNulaOuVazia() {
        for (String descricao : new String[] {null, "", "  "}) {
            DadosInvalidosException excecao =
                    assertThrows(DadosInvalidosException.class, () -> Palestra.criar("Título", descricao, 10));
            assertEquals(List.of("descricao"), camposComErro(excecao));
        }
    }

    @Test
    void rejeitaTotalDeVagasZeroOuNegativo() {
        for (int totalVagas : new int[] {0, -1}) {
            DadosInvalidosException excecao =
                    assertThrows(DadosInvalidosException.class, () -> Palestra.criar("Título", "Descrição", totalVagas));
            assertEquals(List.of("totalVagas"), camposComErro(excecao));
        }
    }

    @Test
    void listaTodosOsCamposComErro() {
        DadosInvalidosException excecao =
                assertThrows(DadosInvalidosException.class, () -> Palestra.criar("", null, 0));

        assertEquals(List.of("titulo", "descricao", "totalVagas"), camposComErro(excecao));
    }

    @Test
    void calculaVagasDisponiveis() {
        Palestra palestra = Palestra.criar("Título", "Descrição", 50);

        assertEquals(30, palestra.vagasDisponiveis(20));
        assertEquals(50, palestra.vagasDisponiveis(0));
    }

    @Test
    void temVagaEnquantoAsAtivasForemMenoresQueOTotal() {
        Palestra palestra = Palestra.criar("Título", "Descrição", 2);

        assertTrue(palestra.temVaga(0));
        assertTrue(palestra.temVaga(1));
        assertFalse(palestra.temVaga(2));
    }
}
