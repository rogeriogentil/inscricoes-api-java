package br.edu.ifsp.saocarlos.inscricoes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void normalizaComTrimEMinusculas() {
        assertEquals("maria@exemplo.com", Email.de("  Maria@Exemplo.COM ").getValor());
    }

    @Test
    void rejeitaFormatoInvalido() {
        for (String invalido : new String[] {null, "", "   ", "semarroba.com", "sem@dominio", "@exemplo.com",
                "dois@@exemplo.com", "com espaco@exemplo.com"}) {
            DadosInvalidosException excecao = assertThrows(DadosInvalidosException.class, () -> Email.de(invalido));
            assertEquals("email", excecao.getErros().get(0).campo());
            assertEquals(List.of("E-mail em formato inválido."),
                    excecao.getErros().stream().map(e -> e.mensagem()).toList());
        }
    }

    @Test
    void igualdadePeloValorNormalizado() {
        assertEquals(Email.de(" Maria@Exemplo.com "), Email.de("maria@exemplo.com"));
        assertEquals(Email.de(" Maria@Exemplo.com ").hashCode(), Email.de("maria@exemplo.com").hashCode());
        assertNotEquals(Email.de("maria@exemplo.com"), Email.de("outra@exemplo.com"));
    }
}
