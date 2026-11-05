package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

public record ChamadoDetalhadoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        PrioridadeChamado prioridade,
        StatusChamado status,
        ClienteResponse cliente,
        CategoriaResponse categoria,
        TecnicoResponse tecnico,
        List<ComentarioResponse> comentarios
) {

    public static ChamadoDetalhadoResponse from(Chamado chamado, List<Comentario> comentarios) {
        ChamadoResponse base = ChamadoResponse.from(chamado);
        return new ChamadoDetalhadoResponse(
                base.id(),
                base.titulo(),
                base.descricao(),
                base.dataAbertura(),
                base.dataFechamento(),
                base.prioridade(),
                base.status(),
                base.cliente(),
                base.categoria(),
                base.tecnico(),
                comentarios.stream().map(ComentarioResponse::from).toList());
    }
}
