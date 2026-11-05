package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.AtribuicaoRequest;
import br.com.sistemas.chamados.dto.ChamadoDetalhadoResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.RespostaPaginada;
import br.com.sistemas.chamados.dto.StatusRequest;
import br.com.sistemas.chamados.entity.PrioridadeChamado;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@Valid @RequestBody ChamadoRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        ChamadoResponse chamado = chamadoService.abrir(request);
        URI uri = uriBuilder.path("/chamados/{id}").buildAndExpand(chamado.id()).toUri();
        return ResponseEntity.created(uri).body(chamado);
    }

    @GetMapping
    public ResponseEntity<RespostaPaginada<ChamadoResponse>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) PrioridadeChamado prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chamadoService.pesquisar(status, prioridade, clienteId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoDetalhadoResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ChamadoRequest request) {
        return ResponseEntity.ok(chamadoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/atribuir")
    public ResponseEntity<ChamadoResponse> atribuirTecnico(@PathVariable Long id,
                                                           @Valid @RequestBody AtribuicaoRequest request) {
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> mudarStatus(@PathVariable Long id,
                                                       @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(chamadoService.mudarStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        chamadoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
