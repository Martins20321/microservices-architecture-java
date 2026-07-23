CREATE TABLE tb_item_pedido(
    id BIGSERIAL NOT NULL,
    pedido_id bigint NOT NULL,
    descricao varchar(255) NOT NULL,
    quantidade integer NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id)
)