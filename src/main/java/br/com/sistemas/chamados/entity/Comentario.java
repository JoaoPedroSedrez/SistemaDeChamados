package br.com.sistemas.chamados.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    public Comentario() {}

    public Comentario(String mensagem, String autor, Chamado chamado) {
        this.mensagem = mensagem;
        this.autor = autor;
        this.chamado = chamado;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getMensagem() { return mensagem; }
    public String getAutor() { return autor; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Chamado getChamado() { return chamado; }
}
