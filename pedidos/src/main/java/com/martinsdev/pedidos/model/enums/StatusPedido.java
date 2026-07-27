package com.martinsdev.pedidos.model.enums;

public enum StatusPedido {

    REALIZADO,
    CONFIRMADO,
    CANCELADO;

    public boolean avancoPermitido(StatusPedido novoStatus) {
        return novoStatus.ordinal() > this.ordinal() && novoStatus != StatusPedido.CANCELADO;
    }
    }
