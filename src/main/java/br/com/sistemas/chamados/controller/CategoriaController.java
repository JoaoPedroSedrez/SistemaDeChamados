package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        CategoriaResponse categoria = categoriaService.salvar(request);
        URI uri = uriBuilder.path("/categorias/{id}").buildAndExpand(categoria.id()).toUri();
        return ResponseEntity.created(uri).body(categoria);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        categoriaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
