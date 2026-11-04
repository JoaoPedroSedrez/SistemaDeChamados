package br.com.sistemas.chamados.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record RespostaPaginada<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas
) {

    public static <T> RespostaPaginada<T> from(Page<T> page) {
        return new RespostaPaginada<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
