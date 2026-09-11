package com.martinsdev.inventario.dto;

public record ProdutoDetailsReposicaoDTO(Long idMovimentacao,
                                         Long produtoId,
                                         String nome,
                                         Integer quantidadeAdicionada,
                                         Integer quantidadeAtualizada) {
}
