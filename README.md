# API de Inscrições em Palestras

Código de apoio da palestra **"Desenvolvimento de Software com SDD - Usando Spec Kit e Aplicando Arquitetura com Harness na Prática"**, apresentada em 2026 no **Instituto Federal de São Paulo (IFSP), campus São Carlos - SP**.

O projeto é uma API REST simples para gerenciar palestras e as inscrições de participantes: criar e consultar palestras, inscrever participantes (com controle de vagas e de inscrição duplicada) e cancelar inscrições. O domínio é pequeno de propósito, porque o foco da palestra é o **processo** de desenvolvimento, não o problema de negócio.

## O que observar no repositório

O repositório foi construído com **SDD (Spec-Driven Development)** usando o [Spec Kit](https://github.com/github/spec-kit). Para acompanhar a palestra, vale olhar:

| Onde | O que mostra |
|---|---|
| [.specify/memory/constitution.md](.specify/memory/constitution.md) | **Constituição** do projeto: princípios que guiam toda decisão (teste para toda regra de negócio, simplicidade, domínio em PT-BR, arquitetura em camadas). |
| [specs/001-inscricao-palestras/](specs/001-inscricao-palestras/) | Artefatos do Spec Kit da funcionalidade: [spec.md](specs/001-inscricao-palestras/spec.md) (o quê e por quê), [plan.md](specs/001-inscricao-palestras/plan.md) (como), [research.md](specs/001-inscricao-palestras/research.md), [data-model.md](specs/001-inscricao-palestras/data-model.md), [contracts/api-rest.md](specs/001-inscricao-palestras/contracts/api-rest.md), [quickstart.md](specs/001-inscricao-palestras/quickstart.md) e [tasks.md](specs/001-inscricao-palestras/tasks.md). |
| [.claude/skills/](.claude/skills/) | Skills do Spec Kit (`speckit-specify`, `speckit-plan`, `speckit-tasks`, `speckit-implement`, `speckit-converge` etc.) usadas com o Claude Code em cada etapa do fluxo. |
| [ArquiteturaTest.java](src/test/java/br/edu/ifsp/saocarlos/inscricoes/ArquiteturaTest.java) | O **harness de arquitetura**: regras do ArchUnit que fazem a build falhar se a arquitetura for violada. |
| `git log` | Um commit por fase (`tasks.md`): Fase 0 (fundação), Fase 1 (US1: listar e criar palestras), Fase 2 (US2: inscrever), Fase 3 (US3: cancelar + convergência). Mostra a evolução incremental guiada pelas tarefas. |

### Arquitetura e harness

O código segue uma arquitetura em camadas, em pacotes sob `br.edu.ifsp.saocarlos.inscricoes`:

- `api`: recursos REST, DTOs e *exception mappers*. Pode depender do domínio, nunca da infraestrutura.
- `dominio`: regras de negócio em Java puro (entidades, serviço, interfaces de repositório e exceções). **Não depende de nenhum framework** (Quarkus, Jakarta, Hibernate, Jackson).
- `infra`: persistência com JPA/Panache e a implementação dos repositórios.

Essas regras não ficam só em documentação: o `ArquiteturaTest` as verifica automaticamente a cada `./mvnw verify`. É isso que chamamos de *harness*: limites executáveis que mantêm a implementação (inclusive a gerada por IA) dentro da arquitetura combinada.

## Stack tecnológica

- **Java 21**
- **Quarkus 3.40** (Quarkus REST, Jackson, Hibernate Validator, Hibernate ORM com Panache, ArC)
- **H2** como banco de dados (em memória nos testes; em arquivo em `./data/inscricoes` nos perfis dev/prod)
- **Maven** via Maven Wrapper (`./mvnw`)
- **JUnit 5**, **REST Assured** e **ArchUnit** para testes
- **Spec Kit** + **Claude Code** para o fluxo SDD

## Pré-requisitos

- **JDK 21** (o projeto traz um [.sdkmanrc](.sdkmanrc); com o [SDKMAN!](https://sdkman.io/) basta rodar `sdk env install` ou `sdk env` na raiz).
- Acesso à internet na primeira execução: o `./mvnw` baixa o Maven e as dependências.
- Não é necessário instalar o Maven, nem um banco de dados.

Confira com `java -version` (deve mostrar `21.x`).

## Como executar

```bash
sdk env                # opcional: ativa o JDK 21 definido em .sdkmanrc
./mvnw quarkus:dev     # modo dev, com live reload, em http://localhost:8080
```

Para gerar e executar o pacote:

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Os dados ficam em `./data/inscricoes` e sobrevivem a reinicializações.

### Exemplos de uso

```bash
# Criar uma palestra (201 + Location)
curl -i -X POST localhost:8080/palestras -H 'Content-Type: application/json' \
  -d '{"titulo":"Quarkus na prática","descricao":"Introdução","totalVagas":1}'

# Listar palestras e ver as vagas disponíveis
curl -s localhost:8080/palestras

# Inscrever um participante (201; e-mail normalizado)
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":"maria@exemplo.com"}'

# Cancelar a inscrição (204)
curl -i -X DELETE 'localhost:8080/palestras/1/inscricoes?email=maria@exemplo.com'
```

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/palestras` | Cria uma palestra |
| `GET` | `/palestras` | Lista as palestras |
| `GET` | `/palestras/{id}` | Detalha uma palestra |
| `POST` | `/palestras/{id}/inscricoes` | Inscreve um participante (409 se lotada ou já inscrito) |
| `DELETE` | `/palestras/{id}/inscricoes?email=` | Cancela a inscrição ativa |

O contrato completo (formatos, códigos HTTP e mensagens de erro) está em [contracts/api-rest.md](specs/001-inscricao-palestras/contracts/api-rest.md), e o roteiro completo de validação em [quickstart.md](specs/001-inscricao-palestras/quickstart.md).

## Como executar os testes

```bash
./mvnw test      # executa os testes
./mvnw verify    # compila, executa os testes e valida o pacote
```

Para rodar uma única classe de teste:

```bash
./mvnw test -Dtest=ArquiteturaTest
```

A suíte cobre:

- **Domínio** (`EmailTest`, `PalestraTest`, `ServicoInscricoesTest`): regras de negócio em testes unitários, sem Quarkus.
- **API** (`PalestrasResourceTest`, `InscricoesResourceTest`): testes `@QuarkusTest` com REST Assured.
- **Concorrência** (`InscricaoConcorrenciaTest`): 100 requisições simultâneas pela última vaga resultam em exatamente 1 `201` e 99 `409`.
- **Arquitetura** (`ArquiteturaTest`): regras de camadas com ArchUnit.
- **Banco de dados** (`BancoDeDadosTest`): o Quarkus sobe com H2 em memória no perfil `test`.

Os relatórios ficam em `target/surefire-reports/`.
