package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Cliente maria;
    private Cliente pedro;
    private Categoria rede;
    private Tecnico lucas;

    @BeforeEach
    void setUp() {
        maria = clienteRepository.save(new Cliente("Maria Oliveira", "maria@teste.com", null));
        pedro = clienteRepository.save(new Cliente("Pedro Santos", "pedro@teste.com", null));
        rede = categoriaRepository.save(new Categoria("Rede", null));
        lucas = tecnicoRepository.save(new Tecnico("Lucas Ferreira", "lucas@teste.com", PerfilTecnico.TECNICO));
    }

    private Chamado novoChamado(String titulo, PrioridadeChamado prioridade, Cliente cliente) {
        return chamadoRepository.save(new Chamado(titulo, "descrição", prioridade, cliente, rede, null));
    }

    @Test
    void deveSalvarChamadoAbertoComRelacionamentos() {
        Chamado chamado = chamadoRepository.save(
                new Chamado("Sem internet", "Wi-fi caiu", PrioridadeChamado.ALTA, maria, rede, lucas));

        Chamado encontrado = chamadoRepository.findById(chamado.getId()).orElseThrow();

        assertThat(encontrado.getStatus()).isEqualTo(StatusChamado.ABERTO);
        assertThat(encontrado.getDataAbertura()).isNotNull();
        assertThat(encontrado.getCliente().getNome()).isEqualTo("Maria Oliveira");
        assertThat(encontrado.getTecnico().getNome()).isEqualTo("Lucas Ferreira");
    }

    @Test
    void deveDizerSeCadastrosPossuemChamados() {
        chamadoRepository.save(new Chamado("Sem internet", "x", PrioridadeChamado.ALTA, maria, rede, lucas));

        assertThat(chamadoRepository.existsByClienteId(maria.getId())).isTrue();
        assertThat(chamadoRepository.existsByClienteId(pedro.getId())).isFalse();
        assertThat(chamadoRepository.existsByCategoriaId(rede.getId())).isTrue();
        assertThat(chamadoRepository.existsByTecnicoId(lucas.getId())).isTrue();
    }

    @Test
    void devePesquisarComFiltros() {
        novoChamado("Sem internet", PrioridadeChamado.ALTA, maria);
        Chamado vpn = novoChamado("VPN lenta", PrioridadeChamado.BAIXA, maria);
        vpn.alterarStatus(StatusChamado.EM_ATENDIMENTO);
        novoChamado("Cabo rompido", PrioridadeChamado.ALTA, pedro);
        PageRequest pagina = PageRequest.of(0, 10);

        assertThat(chamadoRepository.pesquisar(null, null, null, pagina).getTotalElements()).isEqualTo(3);
        assertThat(chamadoRepository.pesquisar(StatusChamado.ABERTO, null, null, pagina).getTotalElements()).isEqualTo(2);
        assertThat(chamadoRepository.pesquisar(null, PrioridadeChamado.BAIXA, null, pagina).getTotalElements()).isEqualTo(1);
        assertThat(chamadoRepository.pesquisar(null, null, pedro.getId(), pagina).getContent())
                .extracting(Chamado::getTitulo).containsExactly("Cabo rompido");
        assertThat(chamadoRepository.pesquisar(StatusChamado.FECHADO, null, null, pagina).getContent()).isEmpty();
    }

    @Test
    void devePaginarEOrdenar() {
        novoChamado("Chamado C", PrioridadeChamado.MEDIA, maria);
        novoChamado("Chamado A", PrioridadeChamado.MEDIA, maria);
        novoChamado("Chamado B", PrioridadeChamado.MEDIA, maria);

        Page<Chamado> pagina = chamadoRepository.pesquisar(null, null, null,
                PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "titulo")));

        assertThat(pagina.getTotalPages()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(Chamado::getTitulo).containsExactly("Chamado A");
    }

    @Test
    void deveListarComentariosEmOrdemEApagarSoOsDoChamado() {
        Chamado internet = novoChamado("Sem internet", PrioridadeChamado.ALTA, maria);
        Chamado cabo = novoChamado("Cabo rompido", PrioridadeChamado.ALTA, pedro);
        comentarioRepository.save(new Comentario("Primeiro", "Maria", internet));
        comentarioRepository.save(new Comentario("Segundo", "Lucas", internet));
        comentarioRepository.save(new Comentario("Do cabo", "Pedro", cabo));

        assertThat(comentarioRepository.findByChamadoIdOrderByDataCriacaoAscIdAsc(internet.getId()))
                .extracting(Comentario::getMensagem).containsExactly("Primeiro", "Segundo");

        comentarioRepository.deleteByChamadoId(internet.getId());

        assertThat(comentarioRepository.findAll()).extracting(Comentario::getMensagem).containsExactly("Do cabo");
    }
}
