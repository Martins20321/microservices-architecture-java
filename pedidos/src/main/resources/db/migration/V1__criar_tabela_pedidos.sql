CREATE TABLE tb_pedidos(
    id BIGSERIAL NOT NULL,
    status varchar(50) NOT NULL,
    data_criacao timestamp NOT NULL,
    data_atualizacao timestamp,
    PRIMARY KEY (id)
)