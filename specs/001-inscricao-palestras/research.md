# Research: Inscrição em Palestras

Decisões técnicas que resolvem o Technical Context do [plan.md](./plan.md). Versões consultadas no Maven Central em 2026-10-27.

## 1. Versão do Quarkus

- **Decision**: Quarkus Platform `3.40.1` (BOM `io.quarkus.platform:quarkus-bom`).
- **Rationale**: é a versão estável mais recente da linha 3.x (a linha 4.0 só tem Beta). A constituição exige Quarkus 3.
- **Alternatives considered**: `4.0.0.Beta1`, rejeitada por violar a restrição "Quarkus 3" e por não ser estável; uma LTS 3.x anterior, possível, mas sem ganho para um projeto de demonstração novo.

## 2. JDK 21 no ambiente local

- **Decision**: `maven.compiler.release=21` no `pom.xml` e `.sdkmanrc` com `java=21.0.4-oracle`.
- **Rationale**: o JDK ativo na máquina é o 17, mas o 21.0.4 já está instalado via SDKMAN. Com `.sdkmanrc`, `sdk env` (ou `sdkman_auto_env`) troca para o JDK certo, e `release=21` faz a compilação falhar de forma explícita se rodar com JDK < 21.
- **Alternatives considered**: `maven-enforcer-plugin` com `requireJavaVersion`, rejeitado por ser redundante com `release=21` (Princípio II); toolchains Maven, configuração extra sem benefício para um só desenvolvedor.

## 3. Maven Wrapper e `.gitignore`

- **Decision**: Maven Wrapper `3.3.4` no tipo `only-script`.
- **Rationale**: o `.gitignore` do repositório ignora `*.jar`, o que deixaria de fora o `.mvn/wrapper/maven-wrapper.jar` do tipo padrão. O tipo `only-script` baixa o Maven pelo próprio `mvnw`, sem jar.
- **Alternatives considered**: exceção `!.mvn/wrapper/maven-wrapper.jar` no `.gitignore`, que versiona um binário sem necessidade.

## 4. Extensões Quarkus

- **Decision**: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2`.
- **Rationale**: é o conjunto mínimo para REST/JSON, validação de entrada na camada `api` e persistência JPA em `infra`. Panache (padrão repositório) reduz o boilerplate das implementações das portas.
- **Alternatives considered**: SmallRye OpenAPI/Swagger UI, útil mas não exigido (fica para depois, se pedido); Hibernate Reactive, complexidade desnecessária.

## 5. H2: memória nos testes, arquivo em dev/prod

- **Decision**: `%test.quarkus.datasource.jdbc.url=jdbc:h2:mem:inscricoes;DB_CLOSE_DELAY=-1` com `drop-and-create`; `%dev`/`%prod` usam `jdbc:h2:file:./data/inscricoes` com `update`.
- **Rationale**: a constituição define H2 em memória nos testes e em arquivo em dev/produção, e o FR-015 exige que os dados sobrevivam a reinicializações. `DB_CLOSE_DELAY=-1` mantém o banco vivo entre conexões durante o teste. Dev Services fica desativado de fato, porque a URL é explícita.
- **Alternatives considered**: Flyway, mais robusto para evolução de esquema, mas YAGNI para um esquema de 2 tabelas em projeto de demonstração; Dev Services com contêiner, que não se aplica ao H2.

## 6. ArchUnit e `ArquiteturaTest`

- **Decision**: `com.tngtech.archunit:archunit-junit5:1.5.1` (escopo `test`). `ArquiteturaTest` usa `@AnalyzeClasses(packages = "br.edu.ifsp.saocarlos.inscricoes", importOptions = ImportOption.DoNotIncludeTests.class)` com uma regra `layeredArchitecture()` (api/dominio/infra) e regras `noClasses()` que proíbem o `dominio` de depender de `io.quarkus..`, `jakarta..`, `javax..`, `org.hibernate..`, `com.fasterxml..` e `io.smallrye..`.
- **Rationale**: valida o Princípio IV a cada `./mvnw verify`. Na Fase 0 as camadas só têm `package-info.java`, que o ArchUnit importa como classe. Assim as regras não falham por "nenhuma classe verificada" (`failOnEmptyShould`) e não é preciso relaxar a configuração.
- **Alternatives considered**: `archunit.properties` com `archRule.failOnEmptyShould=false`, que esconderia regras mal escritas no futuro; `.allowEmptyShould(true)` em cada regra, pelo mesmo motivo.

## 7. Prova do H2 em memória na Fase 0

- **Decision**: `BancoDeDadosTest` (`@QuarkusTest`) injeta `AgroalDataSource`, executa `SELECT 1` e verifica que a URL de conexão começa com `jdbc:h2:mem:`.
- **Rationale**: só o `ArquiteturaTest` não sobe o Quarkus. Este teste prova que a aplicação inicia e que o perfil de teste usa H2 em memória.
- **Alternatives considered**: `quarkus-smallrye-health` com teste de `/q/health`, que é uma dependência a mais só para isso.

## 8. Domínio sem framework, com CDI em `infra`

- **Decision**: o domínio expõe as portas `RepositorioPalestras` e `RepositorioInscricoes` (interfaces) e o serviço `ServicoInscricoes` (POJO com construtor). Em `infra`, `ProdutorDominio` (`@ApplicationScoped` + `@Produces`) instancia o serviço com as implementações JPA. As entidades JPA (`PalestraEntidade`, `InscricaoEntidade`) ficam em `infra` e são convertidas para os objetos de domínio.
- **Rationale**: cumpre o Princípio IV (sem anotações de framework no domínio) sem criar camadas extras.
- **Alternatives considered**: anotar as classes de domínio com JPA, que viola o Princípio IV; uma camada "application" separada, rejeitada pelo Princípio II, porque a transação (`@Transactional`) fica nos métodos dos recursos REST.

## 9. Concorrência: vagas e inscrição duplicada (FR-009, FR-010, SC-002)

- **Decision**: no fluxo de inscrição, `RepositorioPalestras.buscarParaAtualizacao(id)` carrega a palestra com `LockModeType.PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`). Dentro da mesma transação, conta as inscrições ativas, verifica se o e-mail já tem inscrição ativa e persiste a nova. O cancelamento usa o mesmo lock.
- **Rationale**: o lock na linha da palestra serializa todas as inscrições e cancelamentos daquela palestra. Isso garante ao mesmo tempo o limite de vagas e a unicidade da inscrição ativa por e-mail. O H2 (MVStore) suporta `FOR UPDATE` em nível de linha. Para um evento único, a contenção é aceitável.
- **Alternatives considered**: contador de vagas com lock otimista (`@Version`), que exige lógica de retry; índice único parcial `(palestra_id, email) WHERE situacao='ATIVA'`, que o H2 não suporta; `synchronized` em Java, que não protege entre instâncias e mistura infraestrutura com o domínio.

## 10. Normalização do e-mail (FR-009, edge case)

- **Decision**: o value object de domínio `Email` aplica `trim()` + `toLowerCase(Locale.ROOT)` e valida o formato com uma regex simples (`^[^@\s]+@[^@\s]+\.[^@\s]+$`). O e-mail é armazenado já normalizado.
- **Rationale**: "variação de maiúsculas/minúsculas ou espaços nas extremidades é o mesmo participante". Validar no domínio mantém a regra testável sem Quarkus. `@Email` do Bean Validation na API ficaria redundante.
- **Alternatives considered**: `@Email` do Hibernate Validator no DTO, que deixaria a regra fora do domínio; Apache Commons Validator, uma dependência a mais para uma regra trivial.

## 11. Contagem de vagas na listagem (SC-006)

- **Decision**: `RepositorioInscricoes.contarAtivasPorPalestra()` faz uma única consulta `SELECT palestra.id, COUNT(*) ... WHERE situacao = 'ATIVA' GROUP BY palestra.id` e devolve `Map<Long, Long>`, combinado com a lista de palestras.
- **Rationale**: evita N+1 consultas e mantém o cálculo `vagasDisponiveis = totalVagas − ativas` (FR-005) explícito no domínio.
- **Alternatives considered**: coluna `vagasOcupadas` desnormalizada, que é um estado a mais para manter consistente.

## 12. Erros e mensagens (FR-014)

- **Decision**: o domínio lança exceções específicas (`PalestraNaoEncontradaException`, `PalestraLotadaException`, `ParticipanteJaInscritoException`, `InscricaoNaoEncontradaException`, `DadosInvalidosException`), e `ExceptionMapper`s em `api` as convertem em 404/409/409/404/400 com corpo `{ "mensagem", "erros" }`. Violações de Bean Validation nos DTOs também viram 400 com o mesmo formato.
- **Rationale**: um só formato de erro, com mensagens em PT-BR, e status HTTP semânticos.
- **Alternatives considered**: RFC 7807 (`application/problem+json`), válido mas exige mais estrutura do que o necessário.

## 13. Lombok

- **Decision**: não usar na primeira versão.
- **Rationale**: Java 21 tem `record` para DTOs, e as classes de domínio são poucas (Princípio II).
- **Alternatives considered**: Lombok, permitido pela constituição; pode entrar via PR se o boilerplate crescer.
