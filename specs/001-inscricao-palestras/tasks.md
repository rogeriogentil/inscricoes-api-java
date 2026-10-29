# Tasks: Inscrição em Palestras

**Input**: documentos em `/specs/001-inscricao-palestras/` (plan.md, spec.md, data-model.md, research.md, contracts/api-rest.md, quickstart.md)

**Prerequisites**: plan.md, spec.md, constituição (`.specify/memory/constitution.md`)

**Tests**: INCLUÍDOS. O Princípio I da constituição exige teste JUnit para toda regra de negócio. Regras de domínio: JUnit 5 puro (sem Quarkus). API: `@QuarkusTest` + REST Assured. Escreva cada teste antes da implementação e confirme que ele falha.

**Organização**: Fase 0 (fundação) primeiro, depois uma fase por história de usuário, mais uma fase final de acabamento. **Cada fase termina com `./mvnw verify` verde** (JDK 21: rode `sdk env` antes). Uma fase só começa quando a anterior fechou com `BUILD SUCCESS`.

## Formato: `[ID] [P?] [Story] Descrição com caminho`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência entre si)
- **[USn]**: história de usuário a que a tarefa pertence
- `B` = `src/main/java/br/edu/ifsp/saocarlos/inscricoes` e `T` = `src/test/java/br/edu/ifsp/saocarlos/inscricoes`
- Todo identificador, mensagem e Javadoc em PT-BR (Princípio III). O pacote `dominio` não pode importar `io.quarkus`, `jakarta`, `javax`, `org.hibernate`, `com.fasterxml` nem `io.smallrye` (Princípio IV).

---

## Fase 0: Fundação (sem regras de negócio)

**Objetivo**: projeto Quarkus 3 na raiz compilando, H2 em memória nos testes, `ArquiteturaTest` e `BancoDeDadosTest` ativos.

> O repositório já contém um esqueleto commitado (`pom.xml`, `mvnw`, `package-info.java` das três camadas, `application.properties`, `ArquiteturaTest`, `BancoDeDadosTest`). As tarefas abaixo confirmam cada item contra o plano e corrigem o que divergir. Não recrie o que já está conforme.

- [X] T001 Conferir `pom.xml`: `groupId` `br.edu.ifsp.saocarlos`, `artifactId` `inscricoes-api`, `maven.compiler.release` 21, Quarkus platform 3.40.1, extensões `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2` e, no escopo `test`, `quarkus-junit`, `rest-assured` e `com.tngtech.archunit:archunit-junit5:1.5.1`; corrigir divergências em `pom.xml`
- [X] T002 [P] Conferir o wrapper e o ambiente: `mvnw` executável, `.mvn/wrapper/maven-wrapper.properties` em Maven Wrapper 3.3.4 tipo `only-script` (sem `maven-wrapper.jar`), `.sdkmanrc` com `java=21.0.4-oracle`, `.gitignore` com `target/` e `data/`
- [X] T003 [P] Conferir `src/main/resources/application.properties`: `%test` com `jdbc:h2:mem:inscricoes;DB_CLOSE_DELAY=-1` e `drop-and-create`; `%dev`/`%prod` com `jdbc:h2:file:./data/inscricoes` e `update`
- [X] T004 [P] Conferir que `B/api/package-info.java`, `B/dominio/package-info.java` e `B/infra/package-info.java` existem, com Javadoc em PT-BR (garantem que o ArchUnit não rode sobre camadas vazias)
- [X] T005 [P] Conferir `T/ArquiteturaTest.java` (`@AnalyzeClasses` com `ImportOption.DoNotIncludeTests`): `layeredArchitecture` (`api` → `dominio`; `infra` → `dominio`; `dominio` sem acesso a `api` e `infra`; ninguém acessa `api`), regra de que `api` não depende de `infra`, e regra de que `dominio` não depende de `io.quarkus..`, `jakarta..`, `javax..`, `org.hibernate..`, `com.fasterxml..` nem `io.smallrye..`
- [X] T006 [P] Conferir `T/BancoDeDadosTest.java` (`@QuarkusTest`): injeta `AgroalDataSource`, executa `SELECT 1` e afirma que a URL começa com `jdbc:h2:mem:`
- [X] T007 Confirmar que não restou código de exemplo do gerador (`GreetingResource`, `GreetingResourceTest`, `GreetingResourceIT`) em `src/`; remover se existir
- [X] T008 **Fechamento da Fase 0**: com `sdk env` ativo e `java -version` em 21.x, rodar `./mvnw verify` na raiz; exigir `BUILD SUCCESS` com `ArquiteturaTest` e `BancoDeDadosTest` em `target/surefire-reports/`. Opcional (não versionar): prova negativa do quickstart §1 (classe temporária em `dominio` com `@ApplicationScoped` deve fazer o `ArquiteturaTest` falhar; remover depois)

**Checkpoint**: fundação pronta. Nenhuma história começa antes de T008 verde.

---

## Fase 1: User Story 1 - Consultar palestras disponíveis (Prioridade: P1) 🎯 MVP

**Objetivo**: criar palestra, listar e detalhar com `vagasDisponiveis` (FR-001..FR-005).

**Teste independente**: com palestras cadastradas via `POST /palestras`, `GET /palestras` devolve todas com título, descrição, `totalVagas` e `vagasDisponiveis` corretos; lista vazia dá `[]`; id inexistente dá 404 `Palestra não encontrada.`

### Testes da US1 (escrever primeiro; devem falhar)

- [ ] T009 [P] [US1] Teste JUnit 5 puro de `Palestra` em `T/dominio/PalestraTest.java`: título/descrição nulos ou vazios após `trim` rejeitados; `totalVagas` zero e negativo rejeitados com `DadosInvalidosException` listando os campos com erro (FR-002); `vagasDisponiveis(20)` de uma palestra com 50 vagas devolve 30; `temVaga` falso quando as ativas igualam o total (FR-005)
- [ ] T010 [P] [US1] Teste `@QuarkusTest` + REST Assured em `T/api/PalestrasResourceTest.java`: `POST /palestras` válido → 201, header `Location: /palestras/{id}` e `vagasDisponiveis = totalVagas`; `POST` com título vazio, descrição vazia, `totalVagas` 0, negativo ou ausente → 400 com `mensagem` `Dados inválidos.` e `erros` por campo; `GET /palestras` com três palestras, ordenadas por `id`; `GET /palestras` sem palestras → 200 `[]` (US1-3); `GET /palestras/{id}` → 200; `GET /palestras/999` → 404 `Palestra não encontrada.` (US1-4); palestra com 50 vagas e 20 inscrições ativas (inseridas direto pelo `EntityManager` em `@Transactional` no teste) mostra 30 vagas disponíveis (US1-2); inscrições `CANCELADA` não reduzem as vagas

### Implementação da US1

- [ ] T011 [P] [US1] Criar `B/dominio/excecao/DadosInvalidosException.java` (guarda a lista de pares campo/mensagem) e `B/dominio/excecao/PalestraNaoEncontradaException.java` (mensagem `Palestra não encontrada.`), exceções sem dependência de framework
- [ ] T012 [P] [US1] Criar `B/dominio/SituacaoInscricao.java` (enum `ATIVA`, `CANCELADA`; só `ATIVA` ocupa vaga)
- [ ] T013 [US1] Criar `B/dominio/Palestra.java`: POJO imutável com `id` (`Long`, nulo antes de salvar), `titulo` ("Obrigatório, não vazio após `trim`"), `descricao` ("Obrigatória, não vazia após `trim`"), `totalVagas` (`int`, "Inteiro > 0"); fábrica que valida e lança `DadosInvalidosException`; `vagasDisponiveis(long inscricoesAtivas)` e `temVaga(long inscricoesAtivas)` (depende de T011)
- [ ] T014 [P] [US1] Criar a porta `B/dominio/RepositorioPalestras.java` com `salvar(Palestra)`, `buscarPorId(Long)` (devolve `Optional`) e `listarTodas()` ordenada por `id`
- [ ] T015 [P] [US1] Criar a porta `B/dominio/RepositorioInscricoes.java` com `contarAtivas(Long palestraId)` e `contarAtivasPorPalestra(): Map<Long, Long>` (as demais operações chegam na US2)
- [ ] T016 [P] [US1] Criar `B/infra/PalestraEntidade.java` (tabela `palestra`: `id` BIGINT PK identidade; `titulo` VARCHAR(200) NOT NULL; `descricao` VARCHAR(2000) NOT NULL; `total_vagas` INT NOT NULL) e `B/infra/InscricaoEntidade.java` (tabela `inscricao`: `id` BIGINT PK identidade; `palestra_id` BIGINT NOT NULL FK → `palestra.id`; `nome` VARCHAR(200) NOT NULL; `email` VARCHAR(320) NOT NULL (normalizado); `data_hora_inscricao` TIMESTAMP WITH TIME ZONE NOT NULL; `situacao` VARCHAR(20) NOT NULL (`ATIVA`/`CANCELADA`, `@Enumerated(STRING)`); `data_hora_cancelamento` TIMESTAMP WITH TIME ZONE NULL; índices `(palestra_id, situacao)` e `(palestra_id, email)`)
- [ ] T017 [US1] Criar `B/infra/RepositorioPalestrasJpa.java` (`@ApplicationScoped`, Panache) implementando T014 com conversão `PalestraEntidade` ↔ `Palestra` (depende de T013, T014, T016)
- [ ] T018 [US1] Criar `B/infra/RepositorioInscricoesJpa.java` implementando T015; `contarAtivasPorPalestra` é uma única consulta agregada `SELECT palestra_id, COUNT(*) ... WHERE situacao = 'ATIVA' GROUP BY palestra_id`, sem N+1 (research §11) (depende de T015, T016)
- [ ] T019 [P] [US1] Criar em `B/api/dto/`: `NovaPalestraRequisicao` (record com Bean Validation e `@Size` alinhado às colunas: título ≤ 200, descrição ≤ 2000) e `PalestraResposta` (record `id`, `titulo`, `descricao`, `totalVagas`, `vagasDisponiveis`)
- [ ] T020 [P] [US1] Criar em `B/api/erro/`: record `ErroResposta` (`mensagem`, `erros` com `campo`/`mensagem`) e `ExceptionMapper`s de `DadosInvalidosException` (400, `Dados inválidos.`), `ConstraintViolationException` (400, mesmo formato, erros por campo) e `PalestraNaoEncontradaException` (404)
- [ ] T021 [US1] Criar `B/api/PalestrasResource.java` (`/palestras`, `@Transactional`): `POST` (201 + `Location`), `GET` lista (uma chamada a `contarAtivasPorPalestra`) e `GET /{id}` (depende de T013, T017–T020)
- [ ] T022 [US1] **Fechamento da Fase 1**: rodar `./mvnw verify`; exigir `BUILD SUCCESS` com `PalestraTest`, `PalestrasResourceTest`, `ArquiteturaTest` e `BancoDeDadosTest` verdes

**Checkpoint**: a US1 funciona e é testável sozinha (MVP de divulgação da programação).

---

## Fase 2: User Story 2 - Inscrever-se em uma palestra (Prioridade: P1)

**Objetivo**: inscrição com nome e e-mail, respeitando vagas e unicidade, inclusive sob concorrência (FR-006..FR-010, SC-002).

**Teste independente**: inscrever um participante em palestra com vaga devolve 201 e as vagas disponíveis caem em 1; lotada → 409; duplicada → 409; dados inválidos → 400; palestra inexistente → 404; 100 requisições simultâneas pela última vaga resultam em exatamente um 201 e 99 respostas 409.

### Testes da US2 (escrever primeiro; devem falhar)

- [ ] T023 [P] [US2] Teste JUnit 5 puro de `Email` em `T/dominio/EmailTest.java`: `trim` + `toLowerCase(Locale.ROOT)`; formato `^[^@\s]+@[^@\s]+\.[^@\s]+$` (nulo, vazio, sem arroba e sem domínio rejeitados); igualdade pelo valor normalizado (`" Maria@Exemplo.com "` = `"maria@exemplo.com"`)
- [ ] T024 [P] [US2] Teste JUnit 5 puro de `Inscricao` e `ServicoInscricoes.inscrever` em `T/dominio/ServicoInscricoesTest.java`, com repositórios falsos em memória (classes no próprio teste): inscrição válida nasce `ATIVA` com `dataHoraInscricao`; nome vazio e e-mail inválido → `DadosInvalidosException` com ambos os campos; palestra lotada → `PalestraLotadaException`; mesmo e-mail com variação de caixa/espaços → `ParticipanteJaInscritoException`; palestra inexistente → `PalestraNaoEncontradaException`
- [ ] T025 [P] [US2] Teste `@QuarkusTest` em `T/api/InscricoesResourceTest.java` para `POST /palestras/{id}/inscricoes`: 201 com corpo `id`, `palestraId`, `nome`, `email` normalizado, `situacao` `ATIVA`, `dataHoraInscricao` e vagas disponíveis diminuindo em 1 (US2-1); 409 `Não há vagas disponíveis nesta palestra.` (US2-2); 409 `Participante já possui inscrição ativa nesta palestra.` (US2-3); 400 com `erros` por campo para nome vazio/e-mail mal formatado (US2-4); 404 `Palestra não encontrada.` (US2-5)
- [ ] T026 [P] [US2] Teste de concorrência em `T/api/InscricaoConcorrenciaTest.java` (`@QuarkusTest`): palestra com 1 vaga, 100 threads com e-mails distintos disparadas juntas (`CountDownLatch`); exigir exatamente um 201, 99 respostas 409 e `vagasDisponiveis` 0 (SC-002); segundo cenário com o mesmo e-mail enviado em 2 requisições simultâneas em palestra com várias vagas: uma só inscrição ativa (FR-009)

### Implementação da US2

- [ ] T027 [P] [US2] Criar `B/dominio/Email.java` (value object imutável: `trim()` + `toLowerCase(Locale.ROOT)`, regex `^[^@\s]+@[^@\s]+\.[^@\s]+$`, `equals`/`hashCode` pelo valor normalizado)
- [ ] T028 [P] [US2] Criar `B/dominio/excecao/PalestraLotadaException.java` (`Não há vagas disponíveis nesta palestra.`) e `B/dominio/excecao/ParticipanteJaInscritoException.java` (`Participante já possui inscrição ativa nesta palestra.`)
- [ ] T029 [US2] Criar `B/dominio/Inscricao.java`: POJO com `id`, `palestraId`, `nome` ("Obrigatório, não vazio após `trim`"), `email` (`Email`), `dataHoraInscricao` (`Instant`), `situacao` (`ATIVA` na criação), `dataHoraCancelamento` (`Instant`, nulo enquanto ativa); fábrica que acumula erros de nome e e-mail em um só `DadosInvalidosException` (depende de T027)
- [ ] T030 [US2] Estender as portas: `RepositorioPalestras.buscarParaAtualizacao(Long)` em `B/dominio/RepositorioPalestras.java`; `salvar(Inscricao)` e `buscarAtiva(Long palestraId, Email)` em `B/dominio/RepositorioInscricoes.java`
- [ ] T031 [US2] Criar `B/dominio/ServicoInscricoes.java` (POJO com construtor recebendo as duas portas) com `inscrever(Long palestraId, String nome, String email)`: carrega a palestra com `buscarParaAtualizacao`, conta as ativas, recusa lotação, recusa duplicidade e salva (depende de T013, T029, T030)
- [ ] T032 [US2] Em `B/infra/RepositorioPalestrasJpa.java`, implementar `buscarParaAtualizacao` com `LockModeType.PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`, research §9)
- [ ] T033 [US2] Em `B/infra/RepositorioInscricoesJpa.java`, implementar `salvar(Inscricao)` e `buscarAtiva` (conversão `InscricaoEntidade` ↔ `Inscricao`, e-mail já normalizado)
- [ ] T034 [US2] Criar `B/infra/ProdutorDominio.java` (`@ApplicationScoped` com `@Produces` de `ServicoInscricoes` usando as implementações JPA)
- [ ] T035 [P] [US2] Criar em `B/api/dto/`: `NovaInscricaoRequisicao` (record `nome`, `email`, com `@Size` nome ≤ 200 e e-mail ≤ 320) e `InscricaoResposta`; em `B/api/erro/`, `ExceptionMapper`s de `PalestraLotadaException` e `ParticipanteJaInscritoException` (409)
- [ ] T036 [US2] Criar `B/api/InscricoesResource.java` com `POST /palestras/{id}/inscricoes` (201 + corpo; `@Transactional`, de modo que o lock dure até o commit) (depende de T031, T034, T035)
- [ ] T037 [US2] **Fechamento da Fase 2**: rodar `./mvnw verify`; exigir `BUILD SUCCESS` com todos os testes das Fases 0–2 verdes, incluindo `InscricaoConcorrenciaTest`; rodar o teste de concorrência algumas vezes seguidas para descartar flakiness

**Checkpoint**: US1 e US2 funcionam; o limite de vagas e a unicidade valem sob concorrência.

---

## Fase 3: User Story 3 - Cancelar a inscrição (Prioridade: P2)

**Objetivo**: cancelar pelo e-mail, liberar a vaga na hora, manter histórico e permitir reinscrição (FR-011..FR-013).

**Teste independente**: inscrever, cancelar (204) e verificar que a inscrição ficou `CANCELADA`, que a vaga voltou e que um novo participante (ou o mesmo) consegue se inscrever.

### Testes da US3 (escrever primeiro; devem falhar)

- [ ] T038 [P] [US3] Em `T/dominio/ServicoInscricoesTest.java`, casos de `cancelar`: inscrição ativa vira `CANCELADA` com `dataHoraCancelamento`; e-mail normalizado antes da busca; sem inscrição ativa → `InscricaoNaoEncontradaException`; segundo cancelamento → `InscricaoNaoEncontradaException` sem alterar vagas; palestra inexistente → `PalestraNaoEncontradaException`; reinscrição após cancelar cria nova `ATIVA` e mantém a antiga `CANCELADA` (FR-013)
- [ ] T039 [P] [US3] Em `T/api/InscricoesResourceTest.java`, casos de `DELETE /palestras/{id}/inscricoes?email=`: 204 e vagas +1 (US3-1); palestra lotada volta a ter vaga e nova inscrição é aceita (US3-2, SC-005); 404 `Não há inscrição ativa para este e-mail nesta palestra.` sem inscrição e em cancelamento repetido (US3-3); 404 palestra inexistente; 400 com e-mail ausente ou inválido; e-mail com caixa/espaços diferentes é aceito; reinscrição depois do cancelamento devolve 201 (US3-4)

### Implementação da US3

- [ ] T040 [P] [US3] Criar `B/dominio/excecao/InscricaoNaoEncontradaException.java` (`Não há inscrição ativa para este e-mail nesta palestra.`)
- [ ] T041 [US3] Em `B/dominio/Inscricao.java`, adicionar `cancelar()` (`ATIVA` → `CANCELADA`, define `dataHoraCancelamento`; estado final)
- [ ] T042 [US3] Em `B/dominio/ServicoInscricoes.java`, adicionar `cancelar(Long palestraId, String email)`: valida e normaliza o e-mail, bloqueia a palestra com `buscarParaAtualizacao`, busca a inscrição ativa, cancela e salva (depende de T040, T041)
- [ ] T043 [US3] Em `B/infra/RepositorioInscricoesJpa.java`, garantir que `salvar` atualiza `situacao` e `data_hora_cancelamento` de uma inscrição existente (sem apagar a linha)
- [ ] T044 [P] [US3] Em `B/api/erro/`, `ExceptionMapper` de `InscricaoNaoEncontradaException` (404)
- [ ] T045 [US3] Em `B/api/InscricoesResource.java`, adicionar `DELETE /palestras/{id}/inscricoes` com `@QueryParam("email")`, `@Transactional`, 204 em caso de sucesso e 400 para e-mail ausente/inválido (depende de T042, T044)
- [ ] T046 [US3] **Fechamento da Fase 3**: rodar `./mvnw verify`; exigir `BUILD SUCCESS` com todos os testes das Fases 0–3 verdes

**Checkpoint**: as três histórias funcionam juntas e de forma independente.

---

## Fase 4: Acabamento e preocupações transversais

**Objetivo**: erros uniformes (FR-014), desempenho (SC-006), persistência (FR-015) e quickstart completo.

- [ ] T047 [P] Teste em `T/api/ErrosTest.java` e ajuste em `B/api/erro/`: corpo JSON malformado ou `Content-Type` inválido nos três endpoints POST/DELETE responde no formato `{ "mensagem", "erros" }` em PT-BR, e toda recusa da spec traz motivo (SC-004)
- [ ] T048 [P] Teste em `T/api/ListagemDesempenhoTest.java`: 200 palestras com inscrições variadas; `GET /palestras` responde em até 2 s e os `vagasDisponiveis` batem com `totalVagas` menos as ativas de cada uma (SC-003, SC-006)
- [ ] T049 Validar a persistência (FR-015) seguindo o quickstart §2: `./mvnw quarkus:dev`, criar palestra e inscrição, reiniciar e confirmar que `GET /palestras` mantém os dados; conferir que `data/` está no `.gitignore`
- [ ] T050 Executar todo o roteiro do quickstart §2 (os `curl` de US1, US2 e US3) e comparar cada status HTTP com o esperado
- [ ] T051 Revisão final de conformidade: nomes e mensagens em PT-BR, `dominio` livre de framework, nenhuma abstração sem uso (Princípio II) e `specs/001-inscricao-palestras/` coerente com o código entregue
- [ ] T052 **Fechamento da Fase 4**: rodar `./mvnw verify`; exigir `BUILD SUCCESS` final com todos os testes verdes

---

## Dependências e ordem de execução

### Entre fases

- **Fase 0**: sem dependências; bloqueia tudo.
- **Fase 1 (US1)**: depende de T008. Cria `Palestra`, as portas, as entidades JPA e o formato de erro, que as demais fases reutilizam.
- **Fase 2 (US2)**: depende de T022 (precisa de palestras e das entidades).
- **Fase 3 (US3)**: depende de T037 (precisa de inscrições para cancelar).
- **Fase 4**: depende de T046.
- Ao contrário do modelo genérico, as histórias não são independentes entre si no código: cada uma se apoia na anterior. Cada uma continua testável sozinha ao fim da sua fase, e isso é verificado pelo `./mvnw verify` de fechamento.

### Dentro de cada fase

- Testes primeiro, vendo falhar; depois exceções/domínio → portas → infra → DTOs/mapeadores → recurso REST → `./mvnw verify`.
- Tarefas que editam o mesmo arquivo (`ServicoInscricoes.java`, `InscricoesResource.java`, `RepositorioInscricoesJpa.java`, `ServicoInscricoesTest.java`) não são [P] entre si.

### Oportunidades de paralelismo

- Fase 0: T002–T006 (arquivos distintos).
- US1: T009 e T010 (testes); T011, T012, T014, T015, T016, T019 e T020 (arquivos distintos).
- US2: T023–T026 (testes); T027 e T028; T035 em paralelo com T032–T034.
- US3: T038 e T039; T040 e T044.
- Fase 4: T047 e T048.

### Exemplo paralelo: User Story 1

```text
Task: "Teste JUnit de Palestra em T/dominio/PalestraTest.java"
Task: "Teste @QuarkusTest em T/api/PalestrasResourceTest.java"
Task: "Exceções em B/dominio/excecao/"
Task: "Entidades JPA em B/infra/PalestraEntidade.java e InscricaoEntidade.java"
Task: "DTOs em B/api/dto/ e mapeadores de erro em B/api/erro/"
```

---

## Estratégia de implementação

### MVP primeiro (Fase 0 + US1)

1. Fase 0 → T008 verde.
2. Fase 1 (US1) → T022 verde.
3. **Parar e validar**: criar e consultar palestras via `curl` (quickstart §2, bloco US1).

### Entrega incremental

1. Fase 0 + Fase 1 → programação consultável (MVP).
2. Fase 2 → inscrições com controle de vagas e concorrência.
3. Fase 3 → cancelamento e reinscrição.
4. Fase 4 → acabamento e validação ponta a ponta.
5. Cada fase fecha com `./mvnw verify` verde antes da próxima; faça um commit por fase.

---

## Notas

- Não adicionar Lombok, MapStruct, Flyway nem camada "application" (Princípio II, research §13).
- A unicidade da inscrição ativa é garantida pelo lock da palestra, não por índice (o H2 não tem índice parcial).
- Se um teste falhar no fechamento da fase, corrija antes de avançar; não relaxe as regras do `ArquiteturaTest` para passar.
