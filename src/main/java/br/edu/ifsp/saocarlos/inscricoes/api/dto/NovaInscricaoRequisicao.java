package br.edu.ifsp.saocarlos.inscricoes.api.dto;

import jakarta.validation.constraints.Size;

/** Corpo da requisição de inscrição. Obrigatoriedade e formato são validados no domínio. */
public record NovaInscricaoRequisicao(
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres.")
        String nome,

        @Size(max = 320, message = "E-mail deve ter no máximo 320 caracteres.")
        String email) {
}
