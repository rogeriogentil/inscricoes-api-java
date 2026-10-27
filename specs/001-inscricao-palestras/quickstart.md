# Quickstart: validação da Inscrição em Palestras

Roteiro para provar que cada fase funciona. Contrato em [contracts/api-rest.md](./contracts/api-rest.md); modelo em [data-model.md](./data-model.md).

## Pré-requisitos

- JDK 21 ativo: `sdk env` na raiz (lê `.sdkmanrc`) e depois `java -version` deve mostrar `21.x`.
- Acesso à internet na primeira execução (o `./mvnw` baixa o Maven 3.9.x e as dependências).
- Não é preciso ter Maven instalado: use sempre `./mvnw`.

## 1. Fase 0: Fundação

```bash
sdk env
./mvnw verify
```

Resultado esperado:

- `BUILD SUCCESS`.
- No relatório do Surefire, `ArquiteturaTest` e `BancoDeDadosTest` executados sem falhas (`target/surefire-reports/`).
- O log do `BancoDeDadosTest` mostra o Quarkus subindo no perfil `test` com datasource `jdbc:h2:mem:inscricoes`.

Verificações rápidas da estrutura:

```bash
grep -E '<groupId>br.edu.ifsp.saocarlos</groupId>|<artifactId>inscricoes-api</artifactId>' pom.xml
ls src/main/java/br/edu/ifsp/saocarlos/inscricoes/{api,dominio,infra}/package-info.java
test -x mvnw && echo "wrapper ok"
```

Prova negativa do ArchUnit (opcional, não versionar): crie temporariamente uma classe em `dominio` com `import jakarta.enterprise.context.ApplicationScoped;` anotada com `@ApplicationScoped`, rode `./mvnw verify` e confirme que `ArquiteturaTest` **falha**. Depois remova a classe.

## 2. Fases 1–3: API em execução

```bash
./mvnw quarkus:dev     # perfil dev, H2 em arquivo ./data/inscricoes
```

Em outro terminal:

```bash
# US1: criar e consultar
curl -i -X POST localhost:8080/palestras -H 'Content-Type: application/json' \
  -d '{"titulo":"Quarkus na prática","descricao":"Introdução","totalVagas":1}'   # 201, Location: /palestras/1
curl -s localhost:8080/palestras                                                  # vagasDisponiveis: 1
curl -i localhost:8080/palestras/999                                              # 404

# US2: inscrever
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":" Maria@Exemplo.com "}'                              # 201, email normalizado
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":"maria@exemplo.com"}'                                # 409 já inscrito
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"João","email":"joao@exemplo.com"}'                                  # 409 lotada
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"","email":"invalido"}'                                              # 400 com erros por campo

# US3: cancelar e reinscrever
curl -i -X DELETE 'localhost:8080/palestras/1/inscricoes?email=maria@exemplo.com' # 204
curl -i -X DELETE 'localhost:8080/palestras/1/inscricoes?email=maria@exemplo.com' # 404 (repetido)
curl -i -X POST localhost:8080/palestras/1/inscricoes -H 'Content-Type: application/json' \
  -d '{"nome":"João","email":"joao@exemplo.com"}'                                  # 201 (vaga liberada)
```

Persistência (FR-015): pare o `quarkus:dev`, inicie de novo e confirme que `GET /palestras` ainda devolve a palestra com `vagasDisponiveis: 0`.

## 3. Concorrência (SC-002)

Coberta por teste automatizado `@QuarkusTest` executado em `./mvnw verify`: 100 requisições simultâneas pela última vaga resultam em exatamente 1 resposta `201` e 99 respostas `409`, e no fim `vagasDisponiveis` é `0`.
