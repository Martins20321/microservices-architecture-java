# N+1 Query Problem — GET /v1/pedidos

## Sintoma
Ao listar todos os pedidos (`GET /v1/pedidos`), a requisição levava ~1.2s mesmo
com poucos registros no banco. Investigando via OpenTelemetry/Jaeger, a trace
tree mostrou 11 queries SQL para uma única chamada: 1 SELECT para os pedidos,
e mais 10 SELECTs separados, um para cada pedido, buscando seus itens.

<img width="1902" height="802" alt="Captura de tela 2026-10-21 103349" src="https://github.com/user-attachments/assets/eec3b1b6-4c3f-43c1-8152-2a8616353616" />


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

<img width="1918" height="558" alt="Captura de tela 2026-10-21 120924" src="https://github.com/user-attachments/assets/e979243e-9c2e-4e22-b0cb-6241bdf78027" />

## Pendência (atualizado)

A duração total da requisição (~1.2s) não caiu proporcionalmente à redução
de queries. Investigação feita via Grafana, adicionando um painel novo com
métricas do HikariCP (`hikaricp_connections_active`, `_idle`, `_pending`):

<img width="1522" height="332" alt="image" src="https://github.com/user-attachments/assets/d0af31f1-3dee-4ef9-adc3-caf0d6b49dd5" />

Resultado: `Idle = 10` constante, `Active = 0`, `Pending = 0` — o pool de
conexões está saudável e nunca esteve sob pressão. Isso descarta a hipótese
de que a demora vinha do pool "esquentando" conexões.

### Conclusão
A lentidão isolada na primeira requisição após o boot é consistente com um
**cold start**, a combinação de dois fenômenos que só acontecem uma vez:

1. **Query plan cache do Hibernate**: a primeira execução de uma JPQL
   específica precisa ser parseada e compilada para SQL; esse plano fica
   em um cache interno, e as execuções seguintes reaproveitam o trabalho já feito.
2. **JIT Compiler da JVM**: código executado pela primeira vez roda em modo
   interpretado (mais lento); após identificar um "hot path", a JVM compila
   esse trecho para código de máquina otimizado.

Esse comportamento é esperado em qualquer aplicação JVM e não representa um
bug. É irrelevante para uma aplicação que sobe uma vez e roda por longos
períodos (caso atual, ambiente local), mas é relevante em cenários de
auto-scaling (ex: ECS Fargate), onde cada nova instância sofre o mesmo cold
start ao subir sob demanda.
