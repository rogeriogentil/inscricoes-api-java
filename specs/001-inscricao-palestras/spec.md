# Feature Specification: Inscrição em Palestras

**Feature Branch**: `001-inscricao-palestras`

**Created**: 2026-10-27

**Status**: Draft

**Input**: User description: "Um evento acadêmico precisa que os participantes se inscrevam em palestras. Cada palestra tem um título, uma descrição e um número máximo de vagas. O participante pode ver as palestras, se inscrever e cancelar a inscrição."

## Clarifications

### Session 2026-10-27

- Q: Como o participante deve provar quem é ao se inscrever e ao cancelar uma inscrição? → A: Apenas pelo e-mail, sem proteção adicional (projeto de demonstração).
- Q: Além de criar palestras, quais outras operações de cadastro de palestras a organização precisa nesta versão? → A: Apenas criar palestras; edição e exclusão ficam fora do escopo (projeto de demonstração).
- Q: O participante precisa consultar as palestras em que está inscrito, informando o e-mail? → A: Não; a consulta das próprias inscrições fica fora do escopo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar palestras disponíveis (Priority: P1)

O participante do evento acadêmico consulta a lista de palestras oferecidas, vendo para cada uma o título, a descrição, o número máximo de vagas e quantas vagas ainda estão disponíveis. Também pode consultar os detalhes de uma palestra específica.

**Why this priority**: Sem conhecer as palestras, o participante não consegue decidir onde se inscrever. É a base para todas as demais jornadas e já entrega valor sozinha (divulgação da programação).

**Independent Test**: Com palestras previamente cadastradas, consultar a lista e verificar que todas aparecem com título, descrição, total de vagas e vagas disponíveis corretos.

**Acceptance Scenarios**:

1. **Given** existem três palestras cadastradas, **When** o participante consulta a lista de palestras, **Then** as três palestras são exibidas com título, descrição, número máximo de vagas e vagas disponíveis.
2. **Given** uma palestra com 50 vagas e 20 inscrições ativas, **When** o participante consulta essa palestra, **Then** são exibidas 30 vagas disponíveis.
3. **Given** não existe nenhuma palestra cadastrada, **When** o participante consulta a lista, **Then** é exibida uma lista vazia, sem erro.
4. **Given** um identificador de palestra que não existe, **When** o participante consulta seus detalhes, **Then** o sistema informa que a palestra não foi encontrada.

---

### User Story 2 - Inscrever-se em uma palestra (Priority: P1)

O participante escolhe uma palestra com vagas disponíveis e se inscreve informando seus dados de identificação. A inscrição ocupa uma vaga da palestra.

**Why this priority**: É o objetivo central da funcionalidade: garantir o lugar do participante na palestra respeitando o limite de vagas.

**Independent Test**: Inscrever um participante em uma palestra com vagas e verificar que a inscrição foi registrada e que as vagas disponíveis diminuíram em uma unidade.

**Acceptance Scenarios**:

1. **Given** uma palestra com vagas disponíveis, **When** o participante se inscreve com nome e e-mail válidos, **Then** a inscrição é confirmada e o número de vagas disponíveis diminui em uma unidade.
2. **Given** uma palestra sem vagas disponíveis (lotada), **When** o participante tenta se inscrever, **Then** a inscrição é recusada com a mensagem de que não há vagas.
3. **Given** o participante já possui inscrição ativa na palestra, **When** tenta se inscrever novamente na mesma palestra, **Then** a inscrição é recusada informando que ele já está inscrito.
4. **Given** dados de identificação ausentes ou inválidos (ex.: e-mail mal formatado), **When** o participante tenta se inscrever, **Then** a inscrição é recusada indicando quais dados estão incorretos.
5. **Given** uma palestra inexistente, **When** o participante tenta se inscrever nela, **Then** o sistema informa que a palestra não foi encontrada.

---

### User Story 3 - Cancelar a inscrição (Priority: P2)

O participante que não poderá comparecer cancela sua inscrição em uma palestra, liberando a vaga para outra pessoa.

**Why this priority**: Importante para o aproveitamento das vagas, mas o evento já funciona (com menor eficiência) mesmo antes de existir o cancelamento.

**Independent Test**: Inscrever um participante, cancelar a inscrição e verificar que ela deixou de estar ativa e que a vaga voltou a ficar disponível.

**Acceptance Scenarios**:

1. **Given** o participante possui inscrição ativa em uma palestra, **When** cancela a inscrição, **Then** a inscrição deixa de estar ativa e o número de vagas disponíveis aumenta em uma unidade.
2. **Given** uma palestra lotada, **When** um inscrito cancela sua inscrição, **Then** a palestra volta a ter uma vaga disponível e um novo participante consegue se inscrever.
3. **Given** o participante não possui inscrição ativa na palestra, **When** tenta cancelar, **Then** o sistema informa que não há inscrição a cancelar.
4. **Given** o participante cancelou sua inscrição e a palestra ainda tem vagas, **When** se inscreve novamente, **Then** a nova inscrição é aceita.

---

### Edge Cases

- Dois ou mais participantes tentam ocupar a última vaga ao mesmo tempo: apenas um deve ser inscrito; os demais recebem a recusa por falta de vagas. O total de inscrições ativas nunca pode ultrapassar o número máximo de vagas.
- O mesmo participante envia duas solicitações de inscrição simultâneas para a mesma palestra: apenas uma inscrição ativa deve existir.
- O mesmo e-mail informado com variação de maiúsculas/minúsculas ou espaços nas extremidades é considerado o mesmo participante.
- Cancelamento repetido da mesma inscrição: a segunda tentativa informa que não há inscrição ativa, sem alterar a contagem de vagas.
- Palestra cadastrada com número máximo de vagas igual a zero ou negativo: deve ser rejeitada no cadastro.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A organização do evento DEVE poder criar palestras informando título, descrição e número máximo de vagas. Edição e exclusão de palestras não são suportadas nesta versão.
- **FR-002**: O sistema DEVE exigir título e descrição não vazios e número máximo de vagas inteiro maior que zero para cada palestra.
- **FR-003**: Participantes DEVEM poder listar todas as palestras, vendo título, descrição, número máximo de vagas e vagas disponíveis de cada uma.
- **FR-004**: Participantes DEVEM poder consultar os detalhes de uma palestra específica.
- **FR-005**: O sistema DEVE calcular as vagas disponíveis como o número máximo de vagas menos a quantidade de inscrições ativas na palestra.
- **FR-006**: Participantes DEVEM poder se inscrever em uma palestra informando nome e e-mail.
- **FR-007**: O sistema DEVE validar que o nome não está vazio e que o e-mail possui formato válido antes de aceitar uma inscrição.
- **FR-008**: O sistema DEVE recusar inscrições em palestras sem vagas disponíveis.
- **FR-009**: O sistema DEVE impedir que um mesmo participante (identificado pelo e-mail, sem distinção de maiúsculas/minúsculas) tenha mais de uma inscrição ativa na mesma palestra.
- **FR-010**: O sistema DEVE garantir que o total de inscrições ativas de uma palestra nunca ultrapasse seu número máximo de vagas, inclusive com solicitações simultâneas.
- **FR-011**: Participantes DEVEM poder cancelar uma inscrição ativa em uma palestra informando apenas o e-mail usado na inscrição; nenhuma outra comprovação de identidade é exigida.
- **FR-012**: O cancelamento DEVE liberar imediatamente a vaga, tornando-a disponível para novas inscrições.
- **FR-013**: O sistema DEVE permitir que um participante que cancelou volte a se inscrever na mesma palestra, desde que haja vaga.
- **FR-014**: O sistema DEVE retornar mensagens claras, em português, para cada recusa: palestra não encontrada, palestra lotada, participante já inscrito, inscrição inexistente para cancelamento e dados inválidos.
- **FR-015**: O sistema DEVE manter os dados de palestras e inscrições após reinicializações.

### Key Entities

- **Palestra**: Atividade do evento acadêmico na qual participantes se inscrevem. Atributos: título, descrição, número máximo de vagas. Possui informação derivada de vagas disponíveis.
- **Participante**: Pessoa que se inscreve em palestras. Identificado pelo e-mail; possui também nome.
- **Inscrição**: Vínculo entre um participante e uma palestra. Registra o momento da inscrição e sua situação (ativa ou cancelada). Apenas inscrições ativas ocupam vagas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um participante consegue encontrar uma palestra e concluir a inscrição em menos de 1 minuto.
- **SC-002**: Em 100% dos casos, o número de inscrições ativas de uma palestra não ultrapassa seu número máximo de vagas, inclusive em testes com 100 solicitações simultâneas para a última vaga.
- **SC-003**: Em 100% dos casos, o número de vagas disponíveis exibido corresponde ao número máximo de vagas menos as inscrições ativas.
- **SC-004**: 100% das tentativas de inscrição recusadas apresentam ao participante o motivo da recusa.
- **SC-005**: Uma vaga liberada por cancelamento fica disponível para nova inscrição imediatamente após a confirmação do cancelamento.
- **SC-006**: A consulta à lista de palestras é respondida em até 2 segundos com até 200 palestras cadastradas.

## Assumptions

- O cadastro de palestras pela organização do evento se limita à criação; edição, exclusão e relatórios estão fora do escopo desta versão (projeto de demonstração). Uma vez criada, a palestra mantém título, descrição e número máximo de vagas inalterados.
- Não há autenticação nesta versão: o participante se identifica pelo nome e e-mail informados no momento da inscrição, e o cancelamento exige somente o e-mail usado na inscrição. Por se tratar de um projeto de demonstração, aceita-se o risco de que qualquer pessoa que conheça o e-mail de um inscrito possa cancelar a inscrição dele.
- Não há limite de quantas palestras diferentes um mesmo participante pode se inscrever, e não é verificado conflito de horário (a palestra não possui data/horário nesta versão).
- Não há consulta das inscrições de um participante: o participante vê apenas a lista geral de palestras com as vagas disponíveis.
- Não há lista de espera: quando a palestra lota, novas inscrições são simplesmente recusadas.
- Não há envio de e-mail de confirmação de inscrição ou cancelamento nesta versão.
- O evento é único; não há gestão de múltiplos eventos.
- Inscrições canceladas são mantidas como histórico (situação "cancelada"), não removidas.
