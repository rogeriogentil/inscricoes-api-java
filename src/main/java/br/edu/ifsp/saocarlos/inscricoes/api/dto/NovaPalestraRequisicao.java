package br.edu.ifsp.saocarlos.inscricoes.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo da requisição de criação de palestra. */
public record NovaPalestraRequisicao(
        @NotBlank(message = "Título é obrigatório.")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres.")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória.")
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @NotNull(message = "Total de vagas é obrigatório.")
        @Min(value = 1, message = "Total de vagas deve ser maior que zero.")
        Integer totalVagas) {
}
