# N+1 Query Problem — GET /v1/pedidos

## Sintoma
Ao listar todos os pedidos (`GET /v1/pedidos`), a requisição levava ~1.2s mesmo
com poucos registros no banco. Investigando via OpenTelemetry/Jaeger, a trace
tree mostrou 11 queries SQL para uma única chamada: 1 SELECT para os pedidos,
e mais 10 SELECTs separados, um para cada pedido, buscando seus itens.

## Investigação
O relacionamento `Pedido -> List<ItemPedido>` estava configurado com
`FetchType.EAGER`, o que levou à suposição inicial de que os itens já
viriam junto na consulta principal. O trace revelou que não era esse o caso:
mesmo com EAGER, o Hibernate ainda fazia uma consulta separada por pedido
para buscar os itens relacionados, EAGER muda apenas *quando* a busca
acontece (antecipada), não *como* ela é feita.

## Causa raiz
`FetchType.EAGER` não implica JOIN automático. Sem uma instrução explícita
de JOIN, o Hibernate resolve cada associação com uma query independente,
gerando o clássico padrão N+1 (1 consulta principal + N consultas,
uma por item da lista).

## Correção
Criado um método customizado no `PedidoRepository`, usando `@Query` com
`JOIN FETCH`:

\```java
@Query("SELECT p FROM Pedido p JOIN FETCH p.itens")
Page<Pedido> findAllWithItens(Pageable pageable);
\```

O relacionamento foi revertido para `FetchType.LAZY`, já que o carregamento
agora é controlado explicitamente pela query, não pelo padrão da entidade.


## Resultado
Número de queries caiu de 11 para 2 (a consulta principal com JOIN, mais
uma de contagem gerada pelo `Pageable`), confirmado via nova trace no Jaeger.