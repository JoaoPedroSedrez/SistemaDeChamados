package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
    String mensagem,

    @NotBlank(message = "Autor é obrigatório")
    @Size(max = 100, message = "Autor deve ter no máximo 100 caracteres")
    String autor
    ) {}
