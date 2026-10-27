# Data Model: Inscrição em Palestras

Modelo de domínio (pacote `br.edu.ifsp.saocarlos.inscricoes.dominio`, POJOs sem framework) e seu mapeamento de persistência (pacote `infra`). Decisões em [research.md](./research.md).

> Fase 0 não cria nenhuma destas classes; elas chegam nas Fases 1–3 do [plan.md](./plan.md).

## Entidades de domínio

### Palestra

| Campo | Tipo | Regras |
|---|---|---|
| `id` | `Long` | Gerado pela persistência; nulo antes de salvar |
| `titulo` | `String` | Obrigatório, não vazio após `trim` (FR-002) |
| `descricao` | `String` | Obrigatória, não vazia após `trim` (FR-002) |
| `totalVagas` | `int` | Inteiro > 0 (FR-002; edge case: zero/negativo rejeitado) |

- Imutável após a criação (FR-001: sem edição/exclusão).
- Comportamento derivado: `vagasDisponiveis(long inscricoesAtivas) = totalVagas − inscricoesAtivas` (FR-005) e `temVaga(long inscricoesAtivas)`.
- Criação inválida lança `DadosInvalidosException` com a lista de campos com erro.

### Participante (value object, sem tabela própria)

Representado nos campos `nome` + `email` da `Inscricao`. Não há cadastro de participantes (spec: identificação apenas por e-mail).

### Email (value object)

| Campo | Tipo | Regras |
|---|---|---|
| `valor` | `String` | `trim()` + `toLowerCase(Locale.ROOT)`; formato `^[^@\s]+@[^@\s]+\.[^@\s]+$` (FR-007) |

- A igualdade usa o valor normalizado (FR-009, edge case de maiúsculas/espaços).

### Inscricao

| Campo | Tipo | Regras |
|---|---|---|
| `id` | `Long` | Gerado pela persistência |
| `palestraId` | `Long` | Palestra existente |
| `nome` | `String` | Obrigatório, não vazio após `trim` (FR-007) |
| `email` | `Email` | Válido e normalizado |
| `dataHoraInscricao` | `Instant` | Momento da inscrição |
| `situacao` | `SituacaoInscricao` | `ATIVA` na criação |
| `dataHoraCancelamento` | `Instant` | Nulo enquanto ativa |

### SituacaoInscricao (enum)

`ATIVA`, `CANCELADA`. Só `ATIVA` ocupa vaga.

## Transições de estado (Inscricao)

```text
(nova) ──inscrever──▶ ATIVA ──cancelar──▶ CANCELADA   (estado final; mantida como histórico)
```

- `cancelar()` em uma inscrição `CANCELADA` não acontece pelo fluxo, porque o serviço só procura inscrições ativas. Um segundo cancelamento resulta em `InscricaoNaoEncontradaException` sem alterar vagas (edge case).
- A reinscrição cria uma **nova** `Inscricao` `ATIVA` e mantém a antiga `CANCELADA` (FR-013).

## Invariantes (garantidas por `ServicoInscricoes` sob lock pessimista da palestra)

1. `count(Inscricao ATIVA da palestra) ≤ palestra.totalVagas` (FR-010, SC-002).
2. No máximo uma `Inscricao ATIVA` por `(palestraId, email)` (FR-009).

## Portas (interfaces de domínio)

| Interface | Operações |
|---|---|
| `RepositorioPalestras` | `salvar(Palestra)`, `buscarPorId(Long)`, `buscarParaAtualizacao(Long)` (com lock), `listarTodas()` |
| `RepositorioInscricoes` | `salvar(Inscricao)`, `contarAtivas(Long palestraId)`, `contarAtivasPorPalestra(): Map<Long, Long>`, `buscarAtiva(Long palestraId, Email)` |

## Persistência (pacote `infra`, JPA/Panache)

### Tabela `palestra` (`PalestraEntidade`)

| Coluna | Tipo | Restrição |
|---|---|---|
| `id` | BIGINT | PK, identidade |
| `titulo` | VARCHAR(200) | NOT NULL |
| `descricao` | VARCHAR(2000) | NOT NULL |
| `total_vagas` | INT | NOT NULL |

### Tabela `inscricao` (`InscricaoEntidade`)

| Coluna | Tipo | Restrição |
|---|---|---|
| `id` | BIGINT | PK, identidade |
| `palestra_id` | BIGINT | NOT NULL, FK → `palestra.id` |
| `nome` | VARCHAR(200) | NOT NULL |
| `email` | VARCHAR(320) | NOT NULL (normalizado) |
| `data_hora_inscricao` | TIMESTAMP WITH TIME ZONE | NOT NULL |
| `situacao` | VARCHAR(20) | NOT NULL (`ATIVA`/`CANCELADA`) |
| `data_hora_cancelamento` | TIMESTAMP WITH TIME ZONE | NULL |

Índice: `(palestra_id, situacao)` e `(palestra_id, email)`, para contagem e busca. A unicidade da inscrição ativa **não** é feita por índice (o H2 não suporta índice parcial) e sim pelo lock da palestra (research §9).

Os limites de tamanho dos campos (200/2000/320) são aplicados como validação na camada `api` (Bean Validation `@Size`), alinhados às colunas.
