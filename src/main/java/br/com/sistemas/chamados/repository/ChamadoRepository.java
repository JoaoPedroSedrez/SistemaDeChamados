package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    boolean existsByClienteId(Long clienteId);
    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByTecnicoId(Long tecnicoId);

    @Query("select ch from Chamado ch "
            + "where (:status is null or ch.status = :status) "
            + "and (:prioridade is null or ch.prioridade = :prioridade) "
            + "and (:clienteId is null or ch.cliente.id = :clienteId)")
    Page<Chamado> pesquisar(@Param("status") StatusChamado status,
                            @Param("prioridade") PrioridadeChamado prioridade,
                            @Param("clienteId") Long clienteId,
                            Pageable pageable);
}
