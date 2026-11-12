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

<img width="1234" height="524" alt="Captura de tela 2026-11-11 120931" src="https://github.com/user-attachments/assets/d0c990d0-97b7-4584-8021-405664dd14c9" />

### Criar pagamento
O `pagamentos-service` busca o pedido no `pedidos-service`, valida e salva o pagamento,
e publica a mensagem na exchange `pagamentos.ex`. O processamento do lado dos pedidos
aparece no mesmo trace.

<img width="1234" height="524" alt="Captura de tela 2026-11-11 121026" src="https://github.com/user-attachments/assets/f74ba79d-8032-48e9-ae9b-75a1d69bf997" />

### Aprovar pagamento
O `PATCH .../aprovar` atualiza o pagamento e publica o evento de aprovação. O trace
continua no consumer do `pedidos-service`, que busca o pedido e atualiza o status.

<img width="1234" height="524" alt="Captura de tela 2026-11-11 121157" src="https://github.com/user-attachments/assets/5a9c1dd7-0afc-4776-9d94-9b1686d349fb" />
