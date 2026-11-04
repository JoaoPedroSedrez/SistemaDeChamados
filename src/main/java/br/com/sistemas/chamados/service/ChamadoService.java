package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChamadoService {

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
    public List<ChamadoResponse> listarTodos() {
        return chamadoRepository.findAll().stream()
                .map(ChamadoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return ChamadoResponse.from(obterChamado(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = obterChamado(id);
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());
        chamado.setCliente(obterCliente(request.clienteId()));
        chamado.setCategoria(obterCategoria(request.categoriaId()));
        chamado.setTecnico(obterTecnicoOpcional(request.tecnicoId()));
        return ChamadoResponse.from(chamado);
    }

    @Transactional
    public void remover(Long id) {
        Chamado chamado = obterChamado(id);
        chamadoRepository.delete(chamado);
    }

    private Chamado obterChamado(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado com id " + id + " não encontrado"));
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
