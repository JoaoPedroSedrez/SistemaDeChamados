package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        PrioridadeChamado prioridade,
        StatusChamado status,
        ClienteResponse cliente,
        CategoriaResponse categoria,
        TecnicoResponse tecnico
) {

    public static ChamadoResponse from(Chamado chamado) {
        TecnicoResponse tecnico = null;
        if (chamado.getTecnico() != null) {
            tecnico = TecnicoResponse.from(chamado.getTecnico());
        }
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                ClienteResponse.from(chamado.getCliente()),
                CategoriaResponse.from(chamado.getCategoria()),
                tecnico);
    }
}
