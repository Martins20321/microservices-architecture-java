CREATE TABLE tb_produtos(
    id BIGSERIAL NOT NULL,
    nome varchar(50) NOT NULL,
    descricao varchar(100) NOT NULL,
    preco NUMERIC(19,2) NOT NULL,
    quantidade_disponivel INTEGER NOT NULL,
    version INTEGER,
    data_criacao timestamp NOT NULL ,
    PRIMARY KEY (id)
)