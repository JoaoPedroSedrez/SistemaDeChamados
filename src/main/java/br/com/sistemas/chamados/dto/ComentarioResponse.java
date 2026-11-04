package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Comentario;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String mensagem, String autor, LocalDateTime dataCriacao) {

    public static ComentarioResponse from(Comentario comentario) {
        return new ComentarioResponse(comentario.getId(), comentario.getMensagem(),
                comentario.getAutor(), comentario.getDataCriacao());
    }
}
