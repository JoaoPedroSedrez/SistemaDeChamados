package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @PostMapping
    public ResponseEntity<TecnicoResponse> cadastrar(@Valid @RequestBody TecnicoRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        TecnicoResponse tecnico = tecnicoService.salvar(request);
        URI uri = uriBuilder.path("/tecnicos/{id}").buildAndExpand(tecnico.id()).toUri();
        return ResponseEntity.created(uri).body(tecnico);
    }

    @GetMapping
    public ResponseEntity<List<TecnicoResponse>> listar() {
        return ResponseEntity.ok(tecnicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicoResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(tecnicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TecnicoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody TecnicoRequest request) {
        return ResponseEntity.ok(tecnicoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        tecnicoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
