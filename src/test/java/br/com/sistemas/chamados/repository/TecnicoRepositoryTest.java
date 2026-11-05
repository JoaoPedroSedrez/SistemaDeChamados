package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.PerfilTecnico;
import br.com.sistemas.chamados.entity.Tecnico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TecnicoRepositoryTest {

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Test
    void deveSalvarTecnicoComPerfil() {
        Tecnico lucas = tecnicoRepository.save(new Tecnico("Lucas Ferreira", "lucas@teste.com", PerfilTecnico.ADMIN));

        assertThat(tecnicoRepository.findById(lucas.getId()).orElseThrow().getPerfil())
                .isEqualTo(PerfilTecnico.ADMIN);
    }

    @Test
    void deveVerificarEmailIgnorandoOProprioTecnico() {
        Tecnico lucas = tecnicoRepository.save(new Tecnico("Lucas Ferreira", "lucas@teste.com", PerfilTecnico.TECNICO));
        Tecnico julia = tecnicoRepository.save(new Tecnico("Julia Costa", "julia@teste.com", PerfilTecnico.TECNICO));

        assertThat(tecnicoRepository.existsByEmail("lucas@teste.com")).isTrue();
        assertThat(tecnicoRepository.existsByEmailAndIdNot("lucas@teste.com", lucas.getId())).isFalse();
        assertThat(tecnicoRepository.existsByEmailAndIdNot("lucas@teste.com", julia.getId())).isTrue();
    }

    @Test
    void naoDevePermitirEmailRepetidoNoBanco() {
        tecnicoRepository.saveAndFlush(new Tecnico("Lucas Ferreira", "lucas@teste.com", PerfilTecnico.TECNICO));

        assertThatThrownBy(() -> tecnicoRepository.saveAndFlush(
                new Tecnico("Outro Lucas", "lucas@teste.com", PerfilTecnico.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
