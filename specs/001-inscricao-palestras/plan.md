# Implementation Plan: Inscrição em Palestras

**Branch**: `001-inscricao-palestras` | **Date**: 2026-10-27 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-inscricao-palestras/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

API REST para um evento acadêmico em que a organização cria palestras (título, descrição, total de vagas) e participantes listam palestras, se inscrevem (nome + e-mail) e cancelam a inscrição (somente e-mail). O limite de vagas e a unicidade da inscrição ativa por e-mail devem valer inclusive sob concorrência (100 requisições simultâneas pela última vaga).

Abordagem técnica: um único projeto Quarkus 3 (`br.edu.ifsp.saocarlos:inscricoes-api`) na raiz do repositório, com três camadas (`api`, `dominio`, `infra`) e o domínio livre de framework. A persistência usa H2 (em memória nos testes e em arquivo em dev/prod), e a concorrência é controlada com lock pessimista na linha da palestra. O ArchUnit (`ArquiteturaTest`) faz cumprir as camadas. A implementação começa pela **Fase 0 – Fundação**: o esqueleto do projeto com H2 em memória, o `ArquiteturaTest` e `./mvnw verify` passando, ainda sem regras de negócio.

## Technical Context

**Language/Version**: Java 21 (JDK 21.0.4 disponível via SDKMAN; o JDK ativo hoje é o 17, então o projeto fixa o 21 em `.sdkmanrc`)

**Primary Dependencies**: Quarkus 3.40.1 (`quarkus-rest`, `quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2`); Maven 3.9.x via Maven Wrapper 3.3.4

**Storage**: H2 em memória no perfil `test` (`jdbc:h2:mem:inscricoes`) e H2 em arquivo nos perfis `dev`/`prod` (`./data/inscricoes`, para atender FR-015)

**Testing**: JUnit 5 (testes de domínio puros), `@QuarkusTest` + REST Assured (integração/API), ArchUnit 1.5.1 (`ArquiteturaTest`)

**Target Platform**: JVM 21 em servidor Linux (modo JVM; nativo fora do escopo)

**Project Type**: web-service (API REST JSON)

**Performance Goals**: listagem de até 200 palestras em ≤ 2 s (SC-006); a contagem de vagas sai de uma única consulta agregada, sem N+1

**Constraints**: inscrições ativas ≤ total de vagas sob concorrência (FR-010/SC-002); no máximo uma inscrição ativa por (palestra, e-mail normalizado) (FR-009); mensagens de erro em PT-BR (FR-014)

**Scale/Scope**: evento único, centenas de palestras e milhares de inscrições; 5 endpoints REST

Não há itens NEEDS CLARIFICATION: todas as decisões estão resolvidas em [research.md](./research.md).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio / Restrição | Como o plano atende | Status |
|---|---|---|
| I. Teste JUnit para toda regra de negócio | Cada regra (validação de palestra, validação/normalização de e-mail, lotação, duplicidade, cancelamento, reinscrição) tem teste unitário JUnit 5 no domínio e cenário `@QuarkusTest` na API. Há também um teste de concorrência com 100 threads (SC-002). `ArquiteturaTest` com ArchUnit desde a Fase 0. | ✅ |
| II. Simplicidade antes de abstração | Um projeto Maven só, sem Lombok, sem MapStruct e sem camada de "application service" extra. Um serviço de domínio (`ServicoInscricoes`) e duas portas de repositório, que são o mínimo exigido pelo Princípio IV. Esquema gerado pelo Hibernate (sem Flyway). | ✅ |
| III. Domínio e código em PT-BR | Pacotes `api`, `dominio`, `infra`; classes `Palestra`, `Inscricao`, `SituacaoInscricao`, `RepositorioPalestras`; endpoints `/palestras`, `/inscricoes`; mensagens em PT-BR. | ✅ |
| IV. Camadas com domínio independente | `dominio` não depende de `io.quarkus..`, `jakarta..`, `org.hibernate..` nem `com.fasterxml..`. As entidades JPA (`PalestraEntidade`, `InscricaoEntidade`) ficam em `infra`, e o domínio é instanciado por um produtor CDI em `infra`. O `ArquiteturaTest` verifica isso. | ✅ |
| Stack: Java 21, Quarkus 3, Maven, H2 mem (testes) / arquivo (dev/prod), JUnit 5, ArchUnit | Exatamente essa stack. | ✅ |

**Resultado (pré-pesquisa)**: PASS, sem violações.

**Reavaliação pós-design (Fase 1)**: PASS. O [data-model.md](./data-model.md) mantém as entidades de domínio como POJOs e mapeia a persistência em `infra`. O [contrato REST](./contracts/api-rest.md) fica inteiro na camada `api`. Uma observação de interpretação, que não é violação: o Princípio I cita `@QuarkusTest`, mas os testes das regras do domínio rodam em JUnit 5 puro, sem Quarkus, como pede o Princípio IV ("testável isoladamente"). `@QuarkusTest` fica para os testes de integração/API.

## Fases de Implementação

### Fase 0 – Fundação (sem regras de negócio)

Objetivo: projeto Quarkus 3 na **raiz do repositório** compilando, com H2 em memória nos testes, `ArquiteturaTest` ativo e `./mvnw verify` passando.

1. Gerar o projeto na raiz com o `quarkus-maven-plugin:3.40.1:create` (`-DprojectGroupId=br.edu.ifsp.saocarlos -DprojectArtifactId=inscricoes-api -DjavaVersion=21`), com as extensões listadas no Technical Context. Depois mover o conteúdo gerado para a raiz, preservando `.git`, `.specify`, `.claude` e `specs`.
2. Remover o código de exemplo gerado (`GreetingResource`, `GreetingResourceTest`, `GreetingResourceIT`).
3. `pom.xml`: `maven.compiler.release=21` e, no escopo `test`, `com.tngtech.archunit:archunit-junit5:1.5.1`, `quarkus-junit5` e `rest-assured`.
4. Maven Wrapper 3.3.4 no tipo `only-script` (`mvn wrapper:wrapper -Dtype=only-script`), porque o `.gitignore` atual ignora `*.jar` e esse tipo dispensa o `maven-wrapper.jar`. `chmod +x mvnw`.
5. `.sdkmanrc` com `java=21.0.4-oracle`. No `.gitignore`, acrescentar `target/` e `data/`.
6. `src/main/resources/application.properties` com datasource H2: `%test` em memória (`jdbc:h2:mem:inscricoes;DB_CLOSE_DELAY=-1`, `drop-and-create`) e `%dev`/`%prod` em arquivo (`jdbc:h2:file:./data/inscricoes`, `update`).
7. Criar os pacotes das camadas com `package-info.java` documentado (em PT-BR): `br.edu.ifsp.saocarlos.inscricoes.api`, `.dominio`, `.infra`. Isso garante que as regras do ArchUnit não rodem sobre camadas vazias.
8. `src/test/java/br/edu/ifsp/saocarlos/inscricoes/ArquiteturaTest.java` (`@AnalyzeClasses` com `ImportOption.DoNotIncludeTests`) com as regras:
   - arquitetura em camadas: `api` → `dominio`; `infra` → `dominio`; `dominio` não acessa `api` nem `infra`; ninguém acessa `api`;
   - `dominio` não depende de `io.quarkus..`, `jakarta..`, `javax..`, `org.hibernate..`, `com.fasterxml..` nem `io.smallrye..`;
   - `api` não depende de `infra`.
9. `BancoDeDadosTest` (`@QuarkusTest`): injeta o `AgroalDataSource`, executa `SELECT 1` e confirma que a URL do perfil de teste é `jdbc:h2:mem:`. Prova que o Quarkus sobe com H2 em memória.
10. **Critério de pronto**: `./mvnw verify` termina com `BUILD SUCCESS` sob JDK 21, com `ArquiteturaTest` e `BancoDeDadosTest` executados (ver [quickstart.md](./quickstart.md) §1).

### Fase 1 – US1 Consultar palestras (P1)

Domínio `Palestra` (validação FR-002), porta `RepositorioPalestras`, criação (`POST /palestras`), listagem e detalhe com `vagasDisponiveis` (FR-001..FR-005).

### Fase 2 – US2 Inscrever-se (P1)

`Inscricao`, `Email` (normalização), `ServicoInscricoes.inscrever` com lock pessimista na palestra e o teste de concorrência de 100 threads (FR-006..FR-010, SC-002).

### Fase 3 – US3 Cancelar inscrição (P2)

`ServicoInscricoes.cancelar`, situação `CANCELADA` mantida como histórico, reinscrição (FR-011..FR-013).

### Fase 4 – Acabamento

Mapeamento uniforme de erros (FR-014), verificação de persistência após reinício (FR-015) e execução do quickstart completo.

## Project Structure

### Documentation (this feature)

```text
specs/001-inscricao-palestras/
├── plan.md              # Este arquivo
├── research.md          # Decisões técnicas (Phase 0)
├── data-model.md        # Modelo de domínio e persistência (Phase 1)
├── quickstart.md        # Roteiro de validação (Phase 1)
├── contracts/
│   └── api-rest.md      # Contrato HTTP/JSON (Phase 1)
└── tasks.md             # Gerado por /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
mvnw
.mvn/wrapper/maven-wrapper.properties
.sdkmanrc
src/
├── main/
│   ├── java/br/edu/ifsp/saocarlos/inscricoes/
│   │   ├── api/                      # Recursos REST, DTOs (records), ExceptionMappers
│   │   │   ├── PalestrasResource.java
│   │   │   ├── InscricoesResource.java
│   │   │   ├── dto/
│   │   │   └── erro/
│   │   ├── dominio/                  # POJOs + regras, sem framework
│   │   │   ├── Palestra.java
│   │   │   ├── Inscricao.java
│   │   │   ├── SituacaoInscricao.java
│   │   │   ├── Email.java
│   │   │   ├── ServicoInscricoes.java
│   │   │   ├── RepositorioPalestras.java
│   │   │   ├── RepositorioInscricoes.java
│   │   │   └── excecao/
│   │   └── infra/                    # JPA/Panache, implementações das portas, produtores CDI
│   │       ├── PalestraEntidade.java
│   │       ├── InscricaoEntidade.java
│   │       ├── RepositorioPalestrasJpa.java
│   │       ├── RepositorioInscricoesJpa.java
│   │       └── ProdutorDominio.java
│   └── resources/application.properties
└── test/
    ├── java/br/edu/ifsp/saocarlos/inscricoes/
    │   ├── ArquiteturaTest.java      # Fase 0
    │   ├── BancoDeDadosTest.java     # Fase 0
    │   ├── dominio/                  # JUnit 5 puro
    │   └── api/                      # @QuarkusTest + REST Assured (inclui concorrência)
    └── resources/
```

**Structure Decision**: projeto único na raiz, com pacote base `br.edu.ifsp.saocarlos.inscricoes` e subpacotes `api`, `dominio` e `infra`, que correspondem às três camadas do Princípio IV. Na Fase 0 apenas `package-info.java` existe em cada camada. As classes acima chegam nas Fases 1–3.

## Complexity Tracking

Sem violações à constituição. Nada a justificar.
