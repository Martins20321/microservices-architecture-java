package com.martinsdev.pedidos.model.enums;

public enum StatusPedido {

    REALIZADO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    public boolean avancoPermitido(StatusPedido novoStatus) {
        return novoStatus.ordinal() > this.ordinal() && novoStatus != StatusPedido.CANCELADO;
    }
}
