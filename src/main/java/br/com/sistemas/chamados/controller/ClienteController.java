package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.dto.RespostaPaginada;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ChamadoService chamadoService;

    public ClienteController(ClienteService clienteService, ChamadoService chamadoService) {
        this.clienteService = clienteService;
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        ClienteResponse cliente = clienteService.salvar(request);
        URI uri = uriBuilder.path("/clientes/{id}").buildAndExpand(cliente.id()).toUri();
        return ResponseEntity.created(uri).body(cliente);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/chamados")
    public ResponseEntity<RespostaPaginada<ChamadoResponse>> chamadosDoCliente(
            @PathVariable Long id,
            @PageableDefault(sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chamadoService.listarDoCliente(id, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
