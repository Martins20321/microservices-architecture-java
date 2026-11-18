CREATE TABLE tb_usuarios
(
    id               uuid               NOT NULL,
    nome             varchar(100)       NOT NULL,
    email            varchar(80) UNIQUE NOT NULL,
    password_hash    varchar(80)        NOT NULL,
    ativo            BOOLEAN            NOT NULL,
    data_criacao     timestamp          NOT NULL,
    data_atualizacao timestamp,
    PRIMARY KEY (id)
)