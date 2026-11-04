package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public TecnicoResponse salvar(TecnicoRequest request) {
        if (tecnicoRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("E-mail " + request.email() + " já está cadastrado para outro técnico");
        }
        Tecnico novo = new Tecnico(request.nome(), request.email(), request.perfil());
        return TecnicoResponse.from(tecnicoRepository.save(novo));
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listarTodos() {
        return tecnicoRepository.findAll().stream()
                .map(TecnicoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscarPorId(Long id) {
        return TecnicoResponse.from(obterTecnico(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = obterTecnico(id);
        if (tecnicoRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("E-mail " + request.email() + " já está cadastrado para outro técnico");
        }
        tecnico.setNome(request.nome());
        tecnico.setEmail(request.email());
        tecnico.setPerfil(request.perfil());
        return TecnicoResponse.from(tecnico);
    }

    @Transactional
    public void remover(Long id) {
        Tecnico tecnico = obterTecnico(id);
        tecnicoRepository.delete(tecnico);
    }

    private Tecnico obterTecnico(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico com id " + id + " não encontrado"));
    }
}
