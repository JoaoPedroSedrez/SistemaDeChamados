package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;

    public ComentarioService(ComentarioRepository comentarioRepository, ChamadoRepository chamadoRepository) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public ComentarioResponse comentar(Long chamadoId, ComentarioRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado com id " + chamadoId + " não encontrado"));
        if (chamado.estaFechado()) {
            throw new RegraNegocioException("Chamado " + chamadoId + " já foi fechado e não aceita novos comentários");
        }
        Comentario comentario = new Comentario(request.mensagem(), request.autor(), chamado);
        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }
}
