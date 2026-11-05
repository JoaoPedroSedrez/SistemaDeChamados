package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByChamadoIdOrderByDataCriacaoAscIdAsc(Long chamadoId);
    void deleteByChamadoId(Long chamadoId);
}
