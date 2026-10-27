# API de Inscrições - Constituição do Projeto

## Core Principles

### I. Teste JUnit para Toda Regra de Negócio
Cada regra de negócio implementada deve ser acompanhada de teste JUnit correspondente. Testes são parte integrante do design, não adição posterior. A cobertura não é opcional: regras sem testes são débito técnico acumulado.

**Por que**: Testes garantem que as regras de negócio funcionam como especificado. Detectam regressões cedo e documentam comportamento esperado para futuros mantenedores.

**Como aplicar**: Escreva testes JUnit 5 com `@QuarkusTest` antes ou simultaneamente com a implementação da regra de negócio. Use ArchUnit para validar conformidade arquitetural.

### II. Simplicidade Antes de Abstração
Código simples e direto é preferido sobre abstrações prematuras. Evite criar padrões ou helper methods até que o problema se repita 3 vezes. YAGNI: You Aren't Gonna Need It.

**Por que**: Abstrações prematuras introduzem complexidade desnecessária e dificultam manutenção. Simplicidade é mais importante que flexibilidade imaginada.

**Como aplicar**: Implemente o caminho direto primeiro. Refatore apenas quando múltiplas responsabilidades ou duplicação forem evidentes. Justifique abstrações com exemplos concretos.

### III. Domínio e Código em Português do Brasil
Nomes de variáveis, métodos, classes e documentação devem estar em português do Brasil, exceto para termos técnicos universalmente reconhecidos (exemplo: "API", "REST", "JSON"). O domínio reflete a realidade brasileira e o código acompanha.

**Por que**: Melhora compreensão para o time brasileiro. Reduz fricção na comunicação entre código e requisitos de negócio. Facilita onboarding de novos membros da equipe.

**Como aplicar**: Use PT-BR em todos os identificadores. Documentação técnica pode usar inglês para termos estruturais, mas domínio é PT-BR. Exemplo: `class InscricaoParaVestibular` não `InscriptionForVestibularExam`.

### IV. Arquitetura em Camadas com Domain Independente
O projeto segue três camadas: API (recursos REST), Domain (lógica de negócio) e Infra (persistência, configuração). O módulo Domain é **independente de framework**: não herda de classe do Quarkus, não usa anotações de framework, não acoplado a HTTP ou persistência.

Domain pode usar bibliotecas de apoio utilitárias (Apache Commons, Lombok para reduzir boilerplate, etc.) mas não frameworks específicos.

**Por que**: Torna a lógica de negócio testável isoladamente. Facilita mudanças de framework sem reescrever regras. Clareia responsabilidades.

**Como aplicar**: Domain contém classes POJO puras, lógica de validação, cálculos de negócio. API orquestra chamadas ao Domain. Infra fornece implementations de repositórios. Evite injeção de dependência Quarkus no Domain.

## Restrições Técnicas

**Stack Obrigatório**:
- **Linguagem**: Java 21
- **Framework**: Quarkus 3
- **Build**: Maven
- **Database**: H2 em memória (testes), H2 file-based (desenvolvimento/produção)
- **Testes**: JUnit 5, @QuarkusTest para testes de integração
- **Arquitetura**: ArchUnit para validação de camadas

Dependências de apoio permitidas:
- Apache Commons (Lang, Collections, Codec)
- Lombok (geração de getters/setters/construtores)
- Qualquer biblioteca específica de domínio aprovada via PR

## Governança

Esta constituição supersede todas as outras práticas de desenvolvimento. Mudanças em princípios ou restrições requerem:
1. Documentação clara da mudança proposta e rationale
2. Aprovação explícita do time
3. Atualização da versão (semântica: MAJOR.MINOR.PATCH)
4. Nova data de emenda registrada

Todos os pull requests devem verificar conformidade com os princípios e restrições técnicas. Complexidade ou desvios devem ser justificados.

---

**Versão**: 1.0.0 | **Ratificada**: 2026-10-27 | **Última Emenda**: 2026-10-27
