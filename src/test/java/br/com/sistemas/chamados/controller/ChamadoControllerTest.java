package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.*;
import br.com.sistemas.chamados.entity.PerfilTecnico;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.FiltroInvalidoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    private static final String JSON_CHAMADO = """
            {"titulo": "Sem internet", "descricao": "Wi-fi caiu", "prioridade": "ALTA",
             "clienteId": 1, "categoriaId": 2}
            """;

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 3, 9, 30);
    private static final ClienteResponse MARIA = new ClienteResponse(1L, "Maria Oliveira", "maria@teste.com", null);
    private static final CategoriaResponse REDE = new CategoriaResponse(2L, "Rede", null);
    private static final TecnicoResponse LUCAS =
            new TecnicoResponse(4L, "Lucas Ferreira", "lucas@teste.com", PerfilTecnico.TECNICO);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService chamadoService;

    private ChamadoResponse resposta(StatusChamado status, TecnicoResponse tecnico) {
        return new ChamadoResponse(10L, "Sem internet", "Wi-fi caiu", AGORA, null,
                PrioridadeChamado.ALTA, status, MARIA, REDE, tecnico);
    }

    @Test
    void abrirDeveRetornar201ComStatusAberto() throws Exception {
        when(chamadoService.abrir(any(ChamadoRequest.class))).thenReturn(resposta(StatusChamado.ABERTO, null));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(JSON_CHAMADO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/chamados/10")))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.categoria.nome").value("Rede"));
    }

    @Test
    void abrirSemCamposDeveRetornar400() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(5)));

        verifyNoInteractions(chamadoService);
    }

    @Test
    void abrirComCategoriaInexistenteDeveRetornar404() throws Exception {
        when(chamadoService.abrir(any(ChamadoRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 2 não encontrada"));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(JSON_CHAMADO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Categoria com id 2 não encontrada"));
    }

    @Test
    void detalharDeveTrazerComentarios() throws Exception {
        when(chamadoService.buscarPorId(10L)).thenReturn(new ChamadoDetalhadoResponse(10L, "Sem internet",
                "Wi-fi caiu", AGORA, null, PrioridadeChamado.ALTA, StatusChamado.EM_ATENDIMENTO, MARIA, REDE, LUCAS,
                List.of(new ComentarioResponse(1L, "Roteador reiniciado", "Lucas Ferreira", AGORA))));

        mockMvc.perform(get("/chamados/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios", hasSize(1)))
                .andExpect(jsonPath("$.comentarios[0].mensagem").value("Roteador reiniciado"));
    }

    @Test
    void listarDeveRepassarFiltrosEPaginacaoPadrao() throws Exception {
        when(chamadoService.pesquisar(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new RespostaPaginada<>(List.of(resposta(StatusChamado.ABERTO, null)), 0, 20, 1, 1));

        mockMvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("prioridade", "ALTA")
                        .param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].titulo").value("Sem internet"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(chamadoService).pesquisar(eq(StatusChamado.ABERTO), eq(PrioridadeChamado.ALTA), eq(1L), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getSort().getOrderFor("dataAbertura").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listarComPrioridadeInexistenteDeveRetornar400() throws Exception {
        mockMvc.perform(get("/chamados").param("prioridade", "URGENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor 'URGENTE' inválido para o parâmetro prioridade"));

        verifyNoInteractions(chamadoService);
    }

    @Test
    void listarComOrdenacaoNaoPermitidaDeveRetornar400() throws Exception {
        when(chamadoService.pesquisar(any(), any(), any(), any(Pageable.class)))
                .thenThrow(new FiltroInvalidoException("Ordenação por 'senha' não é permitida"));

        mockMvc.perform(get("/chamados").param("sort", "senha"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Ordenação por 'senha' não é permitida"));
    }

    @Test
    void atribuirTecnicoDeveRetornar200() throws Exception {
        when(chamadoService.atribuirTecnico(eq(10L), any(AtribuicaoRequest.class)))
                .thenReturn(resposta(StatusChamado.ABERTO, LUCAS));

        mockMvc.perform(patch("/chamados/10/atribuir").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tecnicoId": 4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnico.nome").value("Lucas Ferreira"));
    }

    @Test
    void mudarStatusSemInformarStatusDeveRetornar400() throws Exception {
        mockMvc.perform(patch("/chamados/10/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("status: Status é obrigatório"));

        verifyNoInteractions(chamadoService);
    }

    @Test
    void mudarStatusDeChamadoFechadoDeveRetornar409() throws Exception {
        when(chamadoService.mudarStatus(eq(10L), any(StatusRequest.class)))
                .thenThrow(new RegraNegocioException("Chamado 10 já foi fechado e não pode mais ser alterado"));

        mockMvc.perform(patch("/chamados/10/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ATENDIMENTO"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Chamado 10 já foi fechado e não pode mais ser alterado"));
    }
}
