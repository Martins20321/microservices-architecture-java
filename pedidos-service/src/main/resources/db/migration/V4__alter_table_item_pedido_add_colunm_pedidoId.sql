TRUNCATE TABLE tb_item_pedido;

ALTER TABLE tb_item_pedido add column produto_id BIGINT NOT NULL;
ALTER TABLE tb_item_pedido rename column valor to valor_unitario;
ALTER TABLE tb_item_pedido drop column descricao;