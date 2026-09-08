CREATE TABLE tb_movimentacao_estoque(
    id BIGSERIAL NOT NULL,
    produto_id BIGINT NOT NULL,
    pedido_id BIGINT NOT NULL,
    tipo_movimentacao VARCHAR(50) NOT NULL,
    quantidade INTEGER NOT NULL,
    data_movimentacao timestamp NOT NULL ,
    PRIMARY KEY (id),
    FOREIGN KEY (produto_id) REFERENCES tb_produtos(id)
)