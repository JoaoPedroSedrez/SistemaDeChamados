package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String JSON_CLIENTE = """
            {"nome": "Maria Oliveira", "email": "maria@teste.com", "telefone": "4733330000"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private ChamadoService chamadoService;

    @Test
    void cadastrarDeveRetornar201() throws Exception {
        when(clienteService.salvar(any(ClienteRequest.class)))
                .thenReturn(new ClienteResponse(7L, "Maria Oliveira", "maria@teste.com", "4733330000"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_CLIENTE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/clientes/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void cadastrarComCamposInvalidosDeveRetornar400() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "nao-e-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: Nome é obrigatório", "email: E-mail inválido")));

        verifyNoInteractions(clienteService);
    }

    @Test
    void cadastrarEmailRepetidoDeveRetornar409() throws Exception {
        when(clienteService.salvar(any(ClienteRequest.class)))
                .thenThrow(new RegraNegocioException("E-mail maria@teste.com já está cadastrado"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_CLIENTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("E-mail maria@teste.com já está cadastrado"));
    }

    @Test
    void listarDeveRetornarTodos() throws Exception {
        when(clienteService.listarTodos()).thenReturn(List.of(
                new ClienteResponse(1L, "Maria Oliveira", "maria@teste.com", null),
                new ClienteResponse(2L, "Pedro Santos", "pedro@teste.com", null)));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nome").value("Pedro Santos"));
    }

    @Test
    void detalharInexistenteDeveRetornar404() throws Exception {
        when(clienteService.buscarPorId(50L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente com id 50 não encontrado"));

        mockMvc.perform(get("/clientes/50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cliente com id 50 não encontrado"));
    }

    @Test
    void atualizarInexistenteDeveRetornar404() throws Exception {
        when(clienteService.atualizar(eq(50L), any(ClienteRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente com id 50 não encontrado"));

        mockMvc.perform(put("/clientes/50").contentType(MediaType.APPLICATION_JSON).content(JSON_CLIENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerDeveRetornar204() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService).remover(1L);
    }

    @Test
    void removerClienteComChamadosDeveRetornar409() throws Exception {
        doThrow(new RegraNegocioException("Cliente possui chamados vinculados e não pode ser removido"))
                .when(clienteService).remover(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void chamadosDeClienteInexistenteDeveRetornar404() throws Exception {
        when(chamadoService.listarDoCliente(eq(50L), any(Pageable.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente com id 50 não encontrado"));

        mockMvc.perform(get("/clientes/50/chamados"))
                .andExpect(status().isNotFound());
    }
}
