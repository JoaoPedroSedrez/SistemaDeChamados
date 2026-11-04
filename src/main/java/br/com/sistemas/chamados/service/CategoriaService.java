package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ChamadoRepository chamadoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ChamadoRepository chamadoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public CategoriaResponse salvar(CategoriaRequest request) {
        if (categoriaRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("Categoria " + request.nome() + " já está cadastrada");
        }
        Categoria nova = new Categoria(request.nome(), request.descricao());
        return CategoriaResponse.from(categoriaRepository.save(nova));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.from(obterCategoria(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obterCategoria(id);
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new RegraNegocioException("Categoria " + request.nome() + " já está cadastrada");
        }
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return CategoriaResponse.from(categoria);
    }

    @Transactional
    public void remover(Long id) {
        Categoria categoria = obterCategoria(id);
        if (chamadoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria possui chamados vinculados e não pode ser removida");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria obterCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria com id " + id + " não encontrada"));
    }
}
