package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.PerfilTecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TecnicoController.class)
class TecnicoControllerTest {

    private static final String JSON_TECNICO = """
            {"nome": "Lucas Ferreira", "email": "lucas@teste.com", "perfil": "TECNICO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TecnicoService tecnicoService;

    @Test
    void cadastrarDeveRetornar201() throws Exception {
        when(tecnicoService.salvar(any(TecnicoRequest.class)))
                .thenReturn(new TecnicoResponse(4L, "Lucas Ferreira", "lucas@teste.com", PerfilTecnico.TECNICO));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(JSON_TECNICO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/tecnicos/4")))
                .andExpect(jsonPath("$.perfil").value("TECNICO"));
    }

    @Test
    void cadastrarVazioDeveListarTodosOsErros() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(3)))
                .andExpect(jsonPath("$.detalhes", hasItem("perfil: Perfil é obrigatório")));

        verifyNoInteractions(tecnicoService);
    }

    @Test
    void cadastrarComPerfilQueNaoExisteDeveRetornar400() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Lucas Ferreira", "email": "lucas@teste.com", "perfil": "SUPERVISOR"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tecnicoService);
    }

    @Test
    void cadastrarEmailRepetidoDeveRetornar409() throws Exception {
        when(tecnicoService.salvar(any(TecnicoRequest.class)))
                .thenThrow(new RegraNegocioException("E-mail lucas@teste.com já está cadastrado para outro técnico"));

        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON).content(JSON_TECNICO))
                .andExpect(status().isConflict());
    }

    @Test
    void detalharInexistenteDeveRetornar404() throws Exception {
        when(tecnicoService.buscarPorId(50L))
                .thenThrow(new RecursoNaoEncontradoException("Técnico com id 50 não encontrado"));

        mockMvc.perform(get("/tecnicos/50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Técnico com id 50 não encontrado"));
    }

    @Test
    void removerTecnicoComChamadosDeveRetornar409() throws Exception {
        doThrow(new RegraNegocioException("Técnico possui chamados vinculados e não pode ser removido"))
                .when(tecnicoService).remover(4L);

        mockMvc.perform(delete("/tecnicos/4"))
                .andExpect(status().isConflict());
    }
}
