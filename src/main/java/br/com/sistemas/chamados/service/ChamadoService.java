package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.AtribuicaoRequest;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.RespostaPaginada;
import br.com.sistemas.chamados.dto.StatusRequest;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.FiltroInvalidoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ChamadoService {

    private static final Set<String> ORDENACOES_PERMITIDAS = Set.of(
            "id", "titulo", "dataAbertura", "dataFechamento", "prioridade", "status",
            "cliente.nome", "categoria.nome");

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse abrir(ChamadoRequest request) {
        Chamado novo = new Chamado(
                request.titulo(),
                request.descricao(),
                request.prioridade(),
                obterCliente(request.clienteId()),
                obterCategoria(request.categoriaId()),
                obterTecnicoOpcional(request.tecnicoId()));
        return ChamadoResponse.from(chamadoRepository.save(novo));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ChamadoResponse> pesquisar(StatusChamado status, PrioridadeChamado prioridade,
                                                       Long clienteId, Pageable pageable) {
        verificarOrdenacao(pageable.getSort());
        return RespostaPaginada.from(
                chamadoRepository.pesquisar(status, prioridade, clienteId, pageable).map(ChamadoResponse::from));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ChamadoResponse> listarDoCliente(Long clienteId, Pageable pageable) {
        obterCliente(clienteId);
        return pesquisar(null, null, clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return ChamadoResponse.from(obterChamado(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = obterChamadoEmAberto(id);
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());
        chamado.setCliente(obterCliente(request.clienteId()));
        chamado.setCategoria(obterCategoria(request.categoriaId()));
        return ChamadoResponse.from(chamado);
    }

    @Transactional
    public ChamadoResponse atribuirTecnico(Long id, AtribuicaoRequest request) {
        Chamado chamado = obterChamadoEmAberto(id);
        chamado.setTecnico(obterTecnicoOpcional(request.tecnicoId()));
        return ChamadoResponse.from(chamado);
    }

    @Transactional
    public ChamadoResponse mudarStatus(Long id, StatusRequest request) {
        Chamado chamado = obterChamadoEmAberto(id);
        chamado.alterarStatus(request.status());
        return ChamadoResponse.from(chamado);
    }

    @Transactional
    public void remover(Long id) {
        Chamado chamado = obterChamado(id);
        chamadoRepository.delete(chamado);
    }

    private void verificarOrdenacao(Sort sort) {
        for (Sort.Order ordem : sort) {
            if (!ORDENACOES_PERMITIDAS.contains(ordem.getProperty())) {
                throw new FiltroInvalidoException("Ordenação por '" + ordem.getProperty()
                        + "' não é permitida. Use um destes campos: " + ORDENACOES_PERMITIDAS);
            }
        }
    }

    private Chamado obterChamado(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado com id " + id + " não encontrado"));
    }

    private Chamado obterChamadoEmAberto(Long id) {
        Chamado chamado = obterChamado(id);
        if (chamado.estaFechado()) {
            throw new RegraNegocioException("Chamado " + id + " já foi fechado e não pode mais ser alterado");
        }
        return chamado;
    }

    private Cliente obterCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com id " + id + " não encontrado"));
    }

    private Categoria obterCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria com id " + id + " não encontrada"));
    }

    private Tecnico obterTecnicoOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico com id " + id + " não encontrado"));
    }
}
