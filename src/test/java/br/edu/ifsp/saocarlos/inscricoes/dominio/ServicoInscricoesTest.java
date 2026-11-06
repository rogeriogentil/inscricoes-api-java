package br.edu.ifsp.saocarlos.inscricoes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.DadosInvalidosException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ErroDeCampo;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.InscricaoNaoEncontradaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraLotadaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.PalestraNaoEncontradaException;
import br.edu.ifsp.saocarlos.inscricoes.dominio.excecao.ParticipanteJaInscritoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicoInscricoesTest {

    private RepositorioPalestrasFalso palestras;
    private RepositorioInscricoesFalso inscricoes;
    private ServicoInscricoes servico;

    @BeforeEach
    void preparar() {
        palestras = new RepositorioPalestrasFalso();
        inscricoes = new RepositorioInscricoesFalso();
        servico = new ServicoInscricoes(palestras, inscricoes);
    }

    private Long palestraComVagas(int totalVagas) {
        return palestras.salvar(Palestra.criar("Título", "Descrição", totalVagas)).getId();
    }

    @Test
    void inscricaoValidaNasceAtivaComDataHora() {
        Long palestraId = palestraComVagas(2);

        Inscricao inscricao = servico.inscrever(palestraId, "  Maria Silva ", " Maria@Exemplo.com ");

        assertNotNull(inscricao.getId());
        assertEquals(palestraId, inscricao.getPalestraId());
        assertEquals("Maria Silva", inscricao.getNome());
        assertEquals("maria@exemplo.com", inscricao.getEmail().getValor());
        assertEquals(SituacaoInscricao.ATIVA, inscricao.getSituacao());
        assertNotNull(inscricao.getDataHoraInscricao());
        assertNull(inscricao.getDataHoraCancelamento());
        assertEquals(1, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void rejeitaNomeVazioEEmailInvalidoJuntos() {
        Long palestraId = palestraComVagas(2);

        DadosInvalidosException excecao = assertThrows(DadosInvalidosException.class,
                () -> servico.inscrever(palestraId, " ", "invalido"));

        assertEquals(List.of("nome", "email"), excecao.getErros().stream().map(ErroDeCampo::campo).toList());
        assertEquals(0, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void rejeitaPalestraLotada() {
        Long palestraId = palestraComVagas(1);
        servico.inscrever(palestraId, "Maria", "maria@exemplo.com");

        assertThrows(PalestraLotadaException.class, () -> servico.inscrever(palestraId, "João", "joao@exemplo.com"));
        assertEquals(1, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void rejeitaMesmoEmailComVariacaoDeCaixaEEspacos() {
        Long palestraId = palestraComVagas(5);
        servico.inscrever(palestraId, "Maria", "maria@exemplo.com");

        assertThrows(ParticipanteJaInscritoException.class,
                () -> servico.inscrever(palestraId, "Maria de novo", "  MARIA@Exemplo.com "));
        assertEquals(1, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void mesmoEmailPodeSeInscreverEmOutraPalestra() {
        Long primeira = palestraComVagas(5);
        Long segunda = palestraComVagas(5);
        servico.inscrever(primeira, "Maria", "maria@exemplo.com");

        servico.inscrever(segunda, "Maria", "maria@exemplo.com");

        assertEquals(1, inscricoes.contarAtivas(segunda));
    }

    @Test
    void rejeitaPalestraInexistente() {
        assertThrows(PalestraNaoEncontradaException.class,
                () -> servico.inscrever(999L, "Maria", "maria@exemplo.com"));
    }

    @Test
    void cancelaInscricaoAtivaRegistrandoADataHora() {
        Long palestraId = palestraComVagas(2);
        servico.inscrever(palestraId, "Maria", "maria@exemplo.com");

        Inscricao cancelada = servico.cancelar(palestraId, "maria@exemplo.com");

        assertEquals(SituacaoInscricao.CANCELADA, cancelada.getSituacao());
        assertNotNull(cancelada.getDataHoraCancelamento());
        assertEquals(0, inscricoes.contarAtivas(palestraId));
        assertEquals(1, inscricoes.todas.size());
        assertEquals(SituacaoInscricao.CANCELADA, inscricoes.todas.get(0).getSituacao());
    }

    @Test
    void normalizaOEmailAntesDeBuscarAInscricaoNoCancelamento() {
        Long palestraId = palestraComVagas(2);
        servico.inscrever(palestraId, "Maria", "maria@exemplo.com");

        servico.cancelar(palestraId, "  MARIA@Exemplo.com ");

        assertEquals(0, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void cancelarSemInscricaoAtivaFalha() {
        Long palestraId = palestraComVagas(2);

        assertThrows(InscricaoNaoEncontradaException.class, () -> servico.cancelar(palestraId, "maria@exemplo.com"));
    }

    @Test
    void segundoCancelamentoFalhaSemAlterarAsVagas() {
        Long palestraId = palestraComVagas(2);
        servico.inscrever(palestraId, "Maria", "maria@exemplo.com");
        servico.inscrever(palestraId, "João", "joao@exemplo.com");
        servico.cancelar(palestraId, "maria@exemplo.com");

        assertThrows(InscricaoNaoEncontradaException.class, () -> servico.cancelar(palestraId, "maria@exemplo.com"));
        assertEquals(1, inscricoes.contarAtivas(palestraId));
    }

    @Test
    void cancelarEmPalestraInexistenteFalha() {
        assertThrows(PalestraNaoEncontradaException.class, () -> servico.cancelar(999L, "maria@exemplo.com"));
    }

    @Test
    void cancelarComEmailInvalidoFalha() {
        Long palestraId = palestraComVagas(2);

        DadosInvalidosException excecao =
                assertThrows(DadosInvalidosException.class, () -> servico.cancelar(palestraId, "invalido"));

        assertEquals(List.of("email"), excecao.getErros().stream().map(ErroDeCampo::campo).toList());
    }

    @Test
    void reinscricaoAposCancelarCriaNovaAtivaEMantemAAntigaCancelada() {
        Long palestraId = palestraComVagas(1);
        Inscricao primeira = servico.inscrever(palestraId, "Maria", "maria@exemplo.com");
        servico.cancelar(palestraId, "maria@exemplo.com");

        Inscricao segunda = servico.inscrever(palestraId, "Maria", "maria@exemplo.com");

        assertNotEquals(primeira.getId(), segunda.getId());
        assertEquals(SituacaoInscricao.ATIVA, segunda.getSituacao());
        assertEquals(1, inscricoes.contarAtivas(palestraId));
        assertEquals(List.of(SituacaoInscricao.CANCELADA, SituacaoInscricao.ATIVA),
                inscricoes.todas.stream().map(Inscricao::getSituacao).toList());
    }

    private static class RepositorioPalestrasFalso implements RepositorioPalestras {
        private final Map<Long, Palestra> porId = new HashMap<>();

        @Override
        public Palestra salvar(Palestra palestra) {
            Palestra salva = palestra.comId((long) porId.size() + 1);
            porId.put(salva.getId(), salva);
            return salva;
        }

        @Override
        public Optional<Palestra> buscarPorId(Long id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Palestra> buscarParaAtualizacao(Long id) {
            return buscarPorId(id);
        }

        @Override
        public List<Palestra> listarTodas() {
            return new ArrayList<>(porId.values());
        }
    }

    private static class RepositorioInscricoesFalso implements RepositorioInscricoes {
        private final List<Inscricao> todas = new ArrayList<>();

        @Override
        public Inscricao salvar(Inscricao inscricao) {
            Inscricao salva = inscricao.getId() == null ? inscricao.comId((long) todas.size() + 1) : inscricao;
            todas.removeIf(existente -> existente.getId().equals(salva.getId()));
            todas.add(salva);
            return salva;
        }

        @Override
        public Optional<Inscricao> buscarAtiva(Long palestraId, Email email) {
            return todas.stream()
                    .filter(i -> i.getPalestraId().equals(palestraId) && i.getEmail().equals(email)
                            && i.getSituacao() == SituacaoInscricao.ATIVA)
                    .findFirst();
        }

        @Override
        public long contarAtivas(Long palestraId) {
            return todas.stream()
                    .filter(i -> i.getPalestraId().equals(palestraId) && i.getSituacao() == SituacaoInscricao.ATIVA)
                    .count();
        }

        @Override
        public Map<Long, Long> contarAtivasPorPalestra() {
            Map<Long, Long> contagem = new HashMap<>();
            todas.stream().filter(i -> i.getSituacao() == SituacaoInscricao.ATIVA)
                    .forEach(i -> contagem.merge(i.getPalestraId(), 1L, Long::sum));
            return contagem;
        }
    }
}
