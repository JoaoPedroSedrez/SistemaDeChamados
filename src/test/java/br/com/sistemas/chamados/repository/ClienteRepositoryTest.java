package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deveSalvarERecuperarCliente() {
        Cliente maria = clienteRepository.save(new Cliente("Maria Oliveira", "maria@teste.com", "4733330000"));

        Cliente encontrado = clienteRepository.findById(maria.getId()).orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("Maria Oliveira");
        assertThat(encontrado.getTelefone()).isEqualTo("4733330000");
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertThat(clienteRepository.findById(12345L)).isNotPresent();
    }

    @Test
    void deveVerificarSeEmailJaExiste() {
        clienteRepository.save(new Cliente("Maria Oliveira", "maria@teste.com", null));

        assertThat(clienteRepository.existsByEmail("maria@teste.com")).isTrue();
        assertThat(clienteRepository.existsByEmail("ninguem@teste.com")).isFalse();
    }

    @Test
    void deveIgnorarOProprioClienteAoVerificarEmail() {
        Cliente maria = clienteRepository.save(new Cliente("Maria Oliveira", "maria@teste.com", null));
        Cliente pedro = clienteRepository.save(new Cliente("Pedro Santos", "pedro@teste.com", null));

        assertThat(clienteRepository.existsByEmailAndIdNot("maria@teste.com", maria.getId())).isFalse();
        assertThat(clienteRepository.existsByEmailAndIdNot("maria@teste.com", pedro.getId())).isTrue();
    }

    @Test
    void naoDevePermitirEmailRepetidoNoBanco() {
        clienteRepository.saveAndFlush(new Cliente("Maria Oliveira", "maria@teste.com", null));

        assertThatThrownBy(() -> clienteRepository.saveAndFlush(new Cliente("Outra Maria", "maria@teste.com", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
