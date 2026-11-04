package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse salvar(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("E-mail " + request.email() + " já está cadastrado");
        }
        Cliente novo = new Cliente(request.nome(), request.email(), request.telefone());
        return ClienteResponse.from(clienteRepository.save(novo));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(ClienteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteResponse.from(obterCliente(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = obterCliente(id);
        if (clienteRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("E-mail " + request.email() + " já está cadastrado");
        }
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = obterCliente(id);
        clienteRepository.delete(cliente);
    }

    private Cliente obterCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com id " + id + " não encontrado"));
    }
}
