## Tracing distribuído (OpenTelemetry + Jaeger)

Os quatro serviços são instrumentados com o Java Agent do OpenTelemetry. Os traces
são enviados por OTLP para o OpenTelemetry Collector, que os repassa ao Jaeger.
Uma única requisição pode ser acompanhada de ponta a ponta, entre os serviços.

- **Chamadas HTTP (OpenFeign):** o `trace_id` é propagado automaticamente pelo header `traceparent`.
- **Mensageria (RabbitMQ):** o publisher do pagamentos-service injeta o `traceparent` nos headers
  da mensagem, e o consumer do pedidos-service o extrai para continuar o mesmo trace.

### Criar pedido
O `pedidos-service` salva o pedido e chama o `inventario-service` por Feign para reservar o
estoque. As duas partes aparecem no mesmo trace, com cada consulta ao banco como um span.

![Trace: criar pedido, com a reserva de estoque no inventario-service](docs/images/jaeger-trace-criar-pedido.png)

### Criar pagamento
O `pagamentos-service` busca o pedido no `pedidos-service`, valida e salva o pagamento,
e publica a mensagem na exchange `pagamentos.ex`. O processamento do lado dos pedidos
aparece no mesmo trace.

![Trace: criar pagamento, com a busca do pedido e a publicação no RabbitMQ](docs/images/jaeger-trace-criar-pagamento.png)

### Aprovar pagamento
O `PATCH .../aprovar` atualiza o pagamento e publica o evento de aprovação. O trace
continua no consumer do `pedidos-service`, que busca o pedido e atualiza o status.

![Trace: aprovar pagamento, atravessando o RabbitMQ](docs/images/jaeger-trace-aprovar-pagamento.png)