package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveSalvarERecuperarCategoria() {
        Categoria rede = categoriaRepository.save(new Categoria("Rede", "Problemas de conexão"));

        assertThat(rede.getId()).isNotNull();
        assertThat(categoriaRepository.findById(rede.getId()).orElseThrow().getDescricao())
                .isEqualTo("Problemas de conexão");
    }

    @Test
    void deveVerificarNomeSemDiferenciarMaiusculas() {
        categoriaRepository.save(new Categoria("Rede", null));

        assertThat(categoriaRepository.existsByNomeIgnoreCase("REDE")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Software")).isFalse();
    }

    @Test
    void deveIgnorarAPropriaCategoriaAoVerificarNome() {
        Categoria rede = categoriaRepository.save(new Categoria("Rede", null));
        Categoria software = categoriaRepository.save(new Categoria("Software", null));

        assertThat(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Rede", rede.getId())).isFalse();
        assertThat(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Rede", software.getId())).isTrue();
    }

    @Test
    void naoDevePermitirNomeRepetidoNoBanco() {
        categoriaRepository.saveAndFlush(new Categoria("Rede", null));

        assertThatThrownBy(() -> categoriaRepository.saveAndFlush(new Categoria("Rede", "duplicada")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
