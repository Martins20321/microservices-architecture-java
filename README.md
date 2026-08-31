# 🏗️ Microservices Architecture - Java

Projeto backend desenvolvido com foco em **arquitetura de microsserviços**, aplicando na prática os principais padrões e ferramentas do ecossistema Spring Cloud, comunicação orientada a eventos com RabbitMQ, e infraestrutura como código (IaC) para deploy na AWS.

---

## 📌 Sobre o Projeto

Sistema de **Pedidos e Pagamentos** composto por microsserviços independentes que se comunicam de forma síncrona (OpenFeign) e assíncrona (RabbitMQ), orquestrados por um API Gateway localmente e por um Application Load Balancer na AWS. O projeto foi desenvolvido com o objetivo de consolidar conhecimentos em arquitetura distribuída, comunicação entre serviços, mensageria, tolerância a falhas, service discovery e infraestrutura como código.

---

## 🧱 Arquitetura

### Local (Desenvolvimento)

<img width="792" height="412" alt="DiagramaDesenvolvimento drawio" src="https://github.com/user-attachments/assets/9311b585-4d81-4014-989d-83bf7ac31dd4" />


### AWS (Produção)

<img width="1234" height="524" alt="DiagramaAWS drawio" src="https://github.com/user-attachments/assets/ec25131d-7e59-474e-b3bd-b304be6f9ed8" />

> ⚠️ Diagramas em atualização; Ainda não refletem a arquitetura de mensageria com RabbitMQ nem o `notificacoes-service`. Serão redesenhados após a conclusão das próximas etapas do roadmap (`estoque-service`, observabilidade).

### Fluxo principal

1. Cliente cria um pedido via `pedidos-service`
2. Cliente cria um pagamento via `pagamentos-service`, que consulta o pedido via OpenFeign (síncrono)
3. `pagamentos-service` publica um evento `pagamento.aguardado-pedido` no RabbitMQ; `pedidos-service` consome e atualiza o pedido para **AGUARDANDO_CONFIRMAR_PAGAMENTO**
4. Ao aprovar o pagamento, `pagamentos-service` publica `pagamento.aprovado` na Exchange direct (`pagamentos.ex`), roteado simultaneamente para dois consumers: `pedidos-service` (confirma o pedido) e `notificacoes-service` (busca os dados do pedido via Feign e envia e-mail de confirmação ao cliente)
5. Ao recusar o pagamento, `pagamentos-service` publica `pagamento.recusado`, roteado da mesma forma: `pedidos-service` (cancela o pedido) e `notificacoes-service` (envia e-mail de recusa ao cliente)

> A notificação de status entre `pagamentos-service` e `pedidos-service` é 100% assíncrona via RabbitMQ. As comunicações síncronas remanescentes são: a consulta de dados do pedido no momento da criação do pagamento (`pagamentos-service` → `pedidos-service`), e a consulta de detalhes do pedido para montagem do e-mail (`notificacoes-service` → `pedidos-service`).

---

## 🚀 Tecnologias

### Backend
- **Java 17**
- **Spring Boot 4.0.7**
- **Spring Cloud 2025.0.0**
- **Spring Data JPA** (pedidos-service)
- **Spring Data MongoDB** (pagamentos-service)
- **Spring Mail (JavaMailSender)** — envio de notificações por e-mail via SMTP
- **Flyway** (migrations do PostgreSQL)

### Microsserviços & Cloud
- **Spring Cloud Gateway** — ponto único de entrada local, roteamento automático
- **Netflix Eureka** — Service Discovery e registro de instâncias (ambiente local)
- **OpenFeign** — comunicação síncrona entre microsserviços
- **Resilience4j** — Circuit Breaker com fallback para tolerância a falhas
- **RabbitMQ** — mensageria assíncrona para notificação de status de pagamento, com Exchange direct, filas Quorum (replicadas), Dead Letter Queue/Exchange e retry com backoff exponencial

### Banco de Dados
- **PostgreSQL** — pedidos-ms (local e RDS na AWS)
- **MongoDB** — pagamentos-ms (local e DocumentDB na AWS)

### Infraestrutura (AWS)
- **AWS CDK (Java)** — Infraestrutura como Código
- **Amazon ECS + Fargate** — orquestração de containers sem servidor
- **Amazon RDS (PostgreSQL)** — banco gerenciado para pedidos-service
- **Amazon DocumentDB** — banco gerenciado compatível com MongoDB para pagamentos-ms
- **Application Load Balancer** — ponto de entrada e balanceamento de carga
- **Amazon ECR** — registry privado de imagens Docker
- **Amazon CloudWatch** — logs e monitoramento
- **Auto Scaling** — escalonamento automático baseado em CPU e memória
- **AWS VPC** — rede isolada com subnets públicas e privadas

### Documentação
- **SpringDoc OpenAPI (Swagger)**

---

## 📦 Estrutura do Projeto

```
microservices-architecture-java/
├── pedidos-service/          # MS de Pedidos (PostgreSQL)
├── pagamentos-service/       # MS de Pagamentos (MongoDB)
├── notificacoes-service/     # MS de Notificações (RabbitMQ + Feign + SMTP)
├── discovery/                # Eureka Server (ambiente local)
├── gateway/                  # Spring Cloud Gateway (ambiente local)
└── infra/                    # Infraestrutura AWS via CDK
    ├── InfraApp.java           # Ponto de entrada — registra e envia as Stacks para a AWS
    ├── InfraStack.java         # Modelo base padrão para criação de Stacks
    ├── VpcStack.java           # Rede — VPC, subnets públicas/privadas, IGW e NAT
    ├── ClusterStack.java       # ECS Cluster — agrupamento lógico dos containers
    ├── RdsStack.java           # Banco relacional — RDS PostgreSQL para pedidos-service
    ├── DocumentDbStack.java    # Banco de documentos — DocumentDB para pagamentos-service
    ├── PedidosServiceStack.java    # ECS Fargate + ALB + Auto Scaling do pedidos-service
    └── PagamentosServiceStack.java # ECS Fargate + ALB + Auto Scaling do pagamentos-service
```

---

## 🔌 Endpoints

### pedidos-service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/pedidos` | Listar todos os pedidos |
| GET | `/v1/pedidos/{id}` | Buscar pedido por ID |
| POST | `/v1/pedidos` | Criar pedido |
| PATCH | `/v1/pedidos/{id}/status` | Atualizar status |
| DELETE | `/v1/pedidos/{id}` | Cancelar pedido |

> A confirmação e o cancelamento de pedido decorrentes de pagamento não são feitos via endpoint — acontecem automaticamente pelo consumo dos eventos do RabbitMQ.

### pagamentos-service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/pagamentos` | Listar todos os pagamentos |
| GET | `/v1/pagamentos/{id}` | Buscar pagamento por ID |
| POST | `/v1/pagamentos` | Criar pagamento |
| PATCH | `/v1/pagamentos/{id}/aprovar` | Aprovar pagamento |
| PATCH | `/v1/pagamentos/{id}/reprovar` | Recusar pagamento |
| PATCH | `/v1/pagamentos/{id}/cancelar` | Cancelar pagamento |

> Localmente todos os endpoints são acessíveis via Gateway na porta **8081**
> Exemplo: `http://localhost:8081/pedidos-service/v1/pedidos`

---

## 🔄 Padrões Implementados

### Service Discovery
Todos os microsserviços se registram automaticamente no **Eureka Server** em ambiente local. Na AWS, o **Application Load Balancer** substitui o Eureka, roteando o tráfego para as instâncias saudáveis.

### Circuit Breaker
Implementado com **Resilience4j** na comunicação síncrona restante (`buscarPedido`, via OpenFeign). Em caso de falha:
- `criarPagamento` → retorna **503 Service Unavailable**

### Mensageria Assíncrona (RabbitMQ)
A notificação de status entre `pagamentos-service` e `pedidos-service` é feita via eventos publicados em um **Exchange direct** (`pagamentos.ex`), consumidos por filas dedicadas:

- **Filas Quorum**, replicadas entre nós do cluster para alta disponibilidade
- **Dead Letter Exchange (DLX) + Dead Letter Queue (DLQ)** por fila, para mensagens que falham no processamento
- **Retry com backoff exponencial** (3 tentativas, intervalo crescente) antes de uma mensagem ser considerada definitivamente falha
- Eventos carregam apenas os dados estritamente necessários (ex: `pedidoId`) — o tipo de ação é definido pela fila/routing key, não pelo conteúdo da mensagem
<img width="601" height="411" alt="image" src="https://github.com/user-attachments/assets/be534b8e-b36e-4b55-ab5d-99026e41ffb7" />


#### Multi-consumer com Exchange Direct

O exchange `pagamentos.ex` roteia os eventos de aprovação/recusa de pagamento para **dois consumers independentes** através de bindings com a mesma routing key:

- `pedidos-service` — atualiza o status do pedido
- `notificacoes-service` — envia e-mail de confirmação/recusa ao cliente, buscando os detalhes do pedido via Feign síncrono

Essa arquitetura evita o acoplamento de "enriquecer o evento" com dados que só um dos consumers usaria — cada serviço busca exatamente o que precisa, quando precisa.

#### Garantias de consistência e tolerância a falhas

- **Idempotência via Ack Manual**: cada consumer confirma o processamento manualmente, só após validar que a ação de negócio não foi previamente executada — protege contra reentrega de mensagens (`redelivered`)
- **Publisher Confirms + Returns**: o producer recebe confirmação assíncrona de que a mensagem chegou ao Exchange (`Confirm`) e é alertado caso ela não seja roteada a nenhuma fila (`Return`), com rastreabilidade via `CorrelationData`
- **Lock Otimista (`@Version`)** na entidade `Pedido`, protegendo contra race conditions entre consumers concorrentes
- **Escalonamento dinâmico de consumers** (1 a 3 por fila), aumentando o throughput de processamento sob demanda

Essa migração eliminou os antigos status `APROVADO_SEM_INTEGRACAO`/`RECUSADO_SEM_INTEGRACAO`, que existiam apenas como fallback do Circuit Breaker para falhas na comunicação síncrona de notificação — problema que a mensageria resolve estruturalmente.

### Consistência Distribuída
Cada microsserviço possui seu próprio banco de dados. A comunicação de consulta é síncrona (OpenFeign, com fallback); a notificação de mudança de estado é assíncrona (RabbitMQ), desacoplando os serviços no tempo.

### Auto Scaling (AWS)
Escalonamento automático baseado em métricas:
- **CPU > 70%** → sobe novas instâncias
- **Memória > 65%** → sobe novas instâncias
- Mínimo: 1 instância | Máximo: 3 instâncias

---

## ⚙️ Como Executar Localmente

### Pré-requisitos
- Java 17+
- PostgreSQL
- MongoDB
- RabbitMQ
- Maven

### Variáveis de ambiente (pedidos-service)
```
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
```

### Variáveis de ambiente (RabbitMQ, ambos os serviços)
```
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=seu_usuario
RABBITMQ_PASSWORD=sua_senha
```

### Ordem de inicialização
1. **discovery** — Eureka Server (`localhost:8761`)
2. **RabbitMQ** — broker de mensageria (`localhost:5672`, management em `localhost:15672`)
3. **pedidos** — MS de Pedidos
4. **pagamentos** — MS de Pagamentos
5. **gateway** — API Gateway (`localhost:8081`)

---

## ☁️ Deploy na AWS

### Pré-requisitos
- AWS CLI configurado
- AWS CDK instalado (`npm install -g aws-cdk`)
- Docker Desktop

### Bootstrap (apenas na primeira vez)
```bash
cdk bootstrap aws://ACCOUNT_ID/us-east-1
```

### Deploy
```bash
cd infra
cdk deploy --all \
  --parameters MsRdsPedidos:DbPassword=sua_senha \
  --parameters MsDocumentDb:dbPasswordDocdb=sua_senha
```

### Destruir recursos
```bash
cdk destroy --all
```

⚠️ Sempre destrua os recursos após os estudos para evitar cobranças desnecessárias.

---

## 👨‍💻 Autor

Desenvolvido por **Gabriel Martins**

[![LinkedIn](https://go-skill-icons.vercel.app/api/icons?i=linkedin)](https://www.linkedin.com/in/josé-martins-3b8491320)
[![GitHub](https://go-skill-icons.vercel.app/api/icons?i=github)](https://github.com/Martins20321)
