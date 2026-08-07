# 🏗️ Microservices Architecture - Java

Projeto backend desenvolvido com foco em **arquitetura de microsserviços**, aplicando na prática os principais padrões e ferramentas do ecossistema Spring Cloud, com infraestrutura como código (IaC) para deploy na AWS.

---

## 📌 Sobre o Projeto

Sistema de **Pedidos e Pagamentos** composto por microsserviços independentes que se comunicam via HTTP (OpenFeign) e são orquestrados por um API Gateway localmente e por um Application Load Balancer na AWS. O projeto foi desenvolvido com o objetivo de consolidar conhecimentos em arquitetura distribuída, comunicação entre serviços, tolerância a falhas, service discovery e infraestrutura como código.

---

## 🧱 Arquitetura

### Local (Desenvolvimento)

<img width="792" height="412" alt="DiagramaDesenvolvimento drawio" src="https://github.com/user-attachments/assets/9311b585-4d81-4014-989d-83bf7ac31dd4" />


### AWS (Produção)

<img width="1234" height="524" alt="DiagramaAWS drawio" src="https://github.com/user-attachments/assets/ec25131d-7e59-474e-b3bd-b304be6f9ed8" />


### Fluxo principal

1. Cliente cria um pedido via `pedidos-ms`
2. Cliente cria um pagamento via `pagamentos-ms`, que consulta o pedido via OpenFeign
3. Ao aprovar o pagamento, `pagamentos-ms` notifica `pedidos-ms` para atualizar o status
4. Ao recusar o pagamento, `pedidos-ms` cancela o pedido automaticamente

---

## 🚀 Tecnologias

### Backend
- **Java 17**
- **Spring Boot 4.0.7**
- **Spring Cloud 2025.0.0**
- **Spring Data JPA** (pedidos-ms)
- **Spring Data MongoDB** (pagamentos-ms)
- **Flyway** (migrations do PostgreSQL)

### Microsserviços & Cloud
- **Spring Cloud Gateway** — ponto único de entrada local, roteamento automático
- **Netflix Eureka** — Service Discovery e registro de instâncias (ambiente local)
- **OpenFeign** — comunicação síncrona entre microsserviços
- **Resilience4j** — Circuit Breaker com fallback para tolerância a falhas

### Banco de Dados
- **PostgreSQL** — pedidos-ms (local e RDS na AWS)
- **MongoDB** — pagamentos-ms (local e DocumentDB na AWS)

### Infraestrutura (AWS)
- **AWS CDK (Java)** — Infraestrutura como Código
- **Amazon ECS + Fargate** — orquestração de containers sem servidor
- **Amazon RDS (PostgreSQL)** — banco gerenciado para pedidos-ms
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
├── pedidos/          # MS de Pedidos (PostgreSQL)
├── pagamentos/       # MS de Pagamentos (MongoDB)
├── discovery/        # Eureka Server (ambiente local)
├── gateway/          # Spring Cloud Gateway (ambiente local)
└── infra/            # Infraestrutura AWS via CDK
    ├── InfraApp.java           # Ponto de entrada — registra e envia as Stacks para a AWS
    ├── InfraStack.java         # Modelo base padrão para criação de Stacks
    ├── VpcStack.java           # Rede — VPC, subnets públicas/privadas, IGW e NAT
    ├── ClusterStack.java       # ECS Cluster — agrupamento lógico dos containers
    ├── RdsStack.java           # Banco relacional — RDS PostgreSQL para pedidos-ms
    ├── DocumentDbStack.java    # Banco de documentos — DocumentDB para pagamentos-ms
    ├── PedidosServiceStack.java    # ECS Fargate + ALB + Auto Scaling do pedidos-ms
    └── PagamentosServiceStack.java # ECS Fargate + ALB + Auto Scaling do pagamentos-ms
```

---

## 🔌 Endpoints

### pedidos-ms

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/pedidos` | Listar todos os pedidos |
| GET | `/v1/pedidos/{id}` | Buscar pedido por ID |
| POST | `/v1/pedidos` | Criar pedido |
| PATCH | `/v1/pedidos/{id}/status` | Atualizar status |
| DELETE | `/v1/pedidos/{id}` | Cancelar pedido |

### pagamentos-ms

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/pagamentos` | Listar todos os pagamentos |
| GET | `/v1/pagamentos/{id}` | Buscar pagamento por ID |
| POST | `/v1/pagamentos` | Criar pagamento |
| PATCH | `/v1/pagamentos/{id}/aprovar` | Aprovar pagamento |
| PATCH | `/v1/pagamentos/{id}/reprovar` | Recusar pagamento |
| PATCH | `/v1/pagamentos/{id}/cancelar` | Cancelar pagamento |

> Localmente todos os endpoints são acessíveis via Gateway na porta **8081**
> Exemplo: `http://localhost:8081/pedidos-ms/v1/pedidos`

---

## 🔄 Padrões Implementados

### Service Discovery
Todos os microsserviços se registram automaticamente no **Eureka Server** em ambiente local. Na AWS, o **Application Load Balancer** substitui o Eureka, roteando o tráfego para as instâncias saudáveis.

### Circuit Breaker
Implementado com **Resilience4j** nos métodos de comunicação entre serviços. Em caso de falha no `pedidos-ms`:

- `criarPagamento` → retorna **503 Service Unavailable**
- `aprovarPagamento` → salva com status **APROVADO_SEM_INTEGRACAO**
- `recusarPagamento` → salva com status **RECUSADO_SEM_INTEGRACAO**

### Consistência Distribuída
Cada microsserviço possui seu próprio banco de dados. A comunicação é feita via OpenFeign (síncrona), com fallback para cenários de indisponibilidade.

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
- Maven

### Variáveis de ambiente (pedidos-ms)
```
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
```

### Ordem de inicialização
1. **discovery** — Eureka Server (`localhost:8761`)
2. **pedidos** — MS de Pedidos
3. **pagamentos** — MS de Pagamentos
4. **gateway** — API Gateway (`localhost:8081`)

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
