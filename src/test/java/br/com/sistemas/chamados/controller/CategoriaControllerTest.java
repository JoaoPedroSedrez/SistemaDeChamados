package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    private static final String JSON_CATEGORIA = """
            {"nome": "Rede", "descricao": "Problemas de conexão"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    void cadastrarDeveRetornar201() throws Exception {
        when(categoriaService.salvar(any(CategoriaRequest.class)))
                .thenReturn(new CategoriaResponse(3L, "Rede", "Problemas de conexão"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(JSON_CATEGORIA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/categorias/3")))
                .andExpect(jsonPath("$.nome").value("Rede"));
    }

    @Test
    void cadastrarSemNomeDeveRetornar400() throws Exception {
        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao": "sem nome"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("nome: Nome é obrigatório"));

        verifyNoInteractions(categoriaService);
    }

    @Test
    void cadastrarNomeRepetidoDeveRetornar409() throws Exception {
        when(categoriaService.salvar(any(CategoriaRequest.class)))
                .thenThrow(new RegraNegocioException("Categoria Rede já está cadastrada"));

        mockMvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content(JSON_CATEGORIA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Categoria Rede já está cadastrada"));
    }

    @Test
    void detalharInexistenteDeveRetornar404() throws Exception {
        when(categoriaService.buscarPorId(50L))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 50 não encontrada"));

        mockMvc.perform(get("/categorias/50"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarDeveRetornar200() throws Exception {
        when(categoriaService.atualizar(eq(3L), any(CategoriaRequest.class)))
                .thenReturn(new CategoriaResponse(3L, "Rede", "Problemas de conexão"));

        mockMvc.perform(put("/categorias/3").contentType(MediaType.APPLICATION_JSON).content(JSON_CATEGORIA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void removerCategoriaComChamadosDeveRetornar409() throws Exception {
        doThrow(new RegraNegocioException("Categoria possui chamados vinculados e não pode ser removida"))
                .when(categoriaService).remover(3L);

        mockMvc.perform(delete("/categorias/3"))
                .andExpect(status().isConflict());
    }
}
