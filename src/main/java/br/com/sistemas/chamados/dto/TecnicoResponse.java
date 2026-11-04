package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.PerfilTecnico;
import br.com.sistemas.chamados.entity.Tecnico;

public record TecnicoResponse(Long id, String nome, String email, PerfilTecnico perfil) {

    public static TecnicoResponse from(Tecnico tecnico) {
        return new TecnicoResponse(tecnico.getId(), tecnico.getNome(), tecnico.getEmail(), tecnico.getPerfil());
    }
}
