package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ComentarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
class ComentarioControllerTest {

    private static final String JSON_COMENTARIO = """
            {"mensagem": "Roteador reiniciado", "autor": "Lucas Ferreira"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComentarioService comentarioService;

    @Test
    void comentarDeveRetornar201() throws Exception {
        when(comentarioService.comentar(eq(10L), any(ComentarioRequest.class))).thenReturn(
                new ComentarioResponse(1L, "Roteador reiniciado", "Lucas Ferreira", LocalDateTime.of(2026, 11, 3, 10, 0)));

        mockMvc.perform(post("/chamados/10/comentarios").contentType(MediaType.APPLICATION_JSON).content(JSON_COMENTARIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Roteador reiniciado"))
                .andExpect(jsonPath("$.autor").value("Lucas Ferreira"));
    }

    @Test
    void comentarVazioDeveRetornar400() throws Exception {
        mockMvc.perform(post("/chamados/10/comentarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(2)));

        verifyNoInteractions(comentarioService);
    }

    @Test
    void comentarEmChamadoInexistenteDeveRetornar404() throws Exception {
        when(comentarioService.comentar(eq(50L), any(ComentarioRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Chamado com id 50 não encontrado"));

        mockMvc.perform(post("/chamados/50/comentarios").contentType(MediaType.APPLICATION_JSON).content(JSON_COMENTARIO))
                .andExpect(status().isNotFound());
    }

    @Test
    void comentarEmChamadoFechadoDeveRetornar409() throws Exception {
        when(comentarioService.comentar(eq(10L), any(ComentarioRequest.class)))
                .thenThrow(new RegraNegocioException("Chamado 10 já foi fechado e não aceita novos comentários"));

        mockMvc.perform(post("/chamados/10/comentarios").contentType(MediaType.APPLICATION_JSON).content(JSON_COMENTARIO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Chamado 10 já foi fechado e não aceita novos comentários"));
    }
}
