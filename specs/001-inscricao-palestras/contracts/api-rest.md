# Contrato REST: Inscrição em Palestras

Base: `http://localhost:8080`. Todo corpo é `application/json; charset=UTF-8`. Os nomes de campos estão em PT-BR (Princípio III).

> A Fase 0 não expõe nenhum endpoint de negócio. Este contrato é implementado nas Fases 1–3.

## Formato de erro (todas as recusas, FR-014)

```json
{
  "mensagem": "Dados inválidos.",
  "erros": [ { "campo": "email", "mensagem": "E-mail em formato inválido." } ]
}
```

`erros` aparece apenas em 400 (pode ser lista vazia nos demais casos).

| Situação | HTTP | `mensagem` |
|---|---|---|
| Dados inválidos (palestra ou inscrição) | 400 | `Dados inválidos.` (+ `erros` por campo) |
| Palestra não encontrada | 404 | `Palestra não encontrada.` |
| Inscrição ativa inexistente para cancelar | 404 | `Não há inscrição ativa para este e-mail nesta palestra.` |
| Palestra lotada | 409 | `Não há vagas disponíveis nesta palestra.` |
| Participante já inscrito | 409 | `Participante já possui inscrição ativa nesta palestra.` |

## Recurso `Palestra` (resposta)

```json
{ "id": 1, "titulo": "Quarkus na prática", "descricao": "Introdução ao Quarkus", "totalVagas": 50, "vagasDisponiveis": 30 }
```

## Endpoints

### `POST /palestras`: criar palestra (FR-001, FR-002)

Requisição:

```json
{ "titulo": "Quarkus na prática", "descricao": "Introdução ao Quarkus", "totalVagas": 50 }
```

- `201 Created` + header `Location: /palestras/{id}` + corpo `Palestra` (`vagasDisponiveis = totalVagas`).
- `400` se o título ou a descrição estiver vazio ou se `totalVagas` for ≤ 0 ou estiver ausente.

### `GET /palestras`: listar (FR-003, FR-005)

- `200 OK` + `[Palestra, ...]`, ordenada por `id`; `[]` quando não há palestras (US1-3).

### `GET /palestras/{id}`: detalhar (FR-004)

- `200 OK` + `Palestra`.
- `404` palestra não encontrada (US1-4).

### `POST /palestras/{id}/inscricoes`: inscrever (FR-006..FR-010)

Requisição:

```json
{ "nome": "Maria Silva", "email": "Maria@Exemplo.com " }
```

- `201 Created` + corpo:

```json
{ "id": 7, "palestraId": 1, "nome": "Maria Silva", "email": "maria@exemplo.com", "situacao": "ATIVA", "dataHoraInscricao": "2026-10-27T18:00:00Z" }
```

- `400` nome vazio ou e-mail inválido (US2-4).
- `404` palestra inexistente (US2-5).
- `409` lotada (US2-2) ou já inscrito (US2-3).

### `DELETE /palestras/{id}/inscricoes?email={email}`: cancelar (FR-011, FR-012)

- O e-mail é normalizado antes da busca (`trim` + minúsculas).
- `204 No Content`: a inscrição passa a `CANCELADA` e a vaga é liberada na hora.
- `400` e-mail ausente ou inválido.
- `404` palestra inexistente, ou nenhuma inscrição ativa para o e-mail (US3-3; cancelamento repetido).
