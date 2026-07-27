# 🏗️ Microservices Architecture - Java

Projeto backend desenvolvido com foco em **arquitetura de microsserviços**, aplicando na prática os principais padrões e ferramentas do ecossistema Spring Cloud.

---

## 📌 Sobre o Projeto

Sistema de **Pedidos e Pagamentos** composto por microsserviços independentes que se comunicam via HTTP (OpenFeign) e são orquestrados por um API Gateway. O projeto foi desenvolvido com o objetivo de consolidar conhecimentos em arquitetura distribuída, comunicação entre serviços, tolerância a falhas e service discovery.

---

## 🧱 Arquitetura

```
Cliente → API Gateway (8081)
              ↓
         Eureka Server (Service Discovery)
              ↓
    ┌─────────────────────────┐
    │                         │
pedidos-ms              pagamentos-ms
(PostgreSQL)              (MongoDB)
```

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
- **Spring Cloud Gateway** — ponto único de entrada, roteamento automático
- **Netflix Eureka** — Service Discovery e registro de instâncias
- **OpenFeign** — comunicação síncrona entre microsserviços
- **Resilience4j** — Circuit Breaker com fallback para tolerância a falhas

### Banco de Dados
- **PostgreSQL** — pedidos-ms
- **MongoDB** — pagamentos-ms

### Documentação
- **SpringDoc OpenAPI (Swagger)**

---

## 📦 Estrutura do Projeto

```
microservices-architecture-java/
├── pedidos/          # MS de Pedidos (PostgreSQL)
├── pagamentos/       # MS de Pagamentos (MongoDB)
├── discovery/        # Eureka Server
└── gateway/          # Spring Cloud Gateway
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

> Todos os endpoints são acessíveis via Gateway na porta **8081**
> Exemplo: `http://localhost:8081/pedidos-ms/v1/pedidos`

---

## 🔄 Padrões Implementados

### Service Discovery
Todos os microsserviços se registram automaticamente no **Eureka Server**. O Gateway consulta o Eureka para descobrir os endereços dinamicamente, sem configuração estática de portas.

### Circuit Breaker
Implementado com **Resilience4j** nos métodos de comunicação entre serviços. Em caso de falha no `pedidos-ms`:

- `criarPagamento` → retorna **503 Service Unavailable**
- `aprovarPagamento` → salva com status **APROVADO_SEM_INTEGRACAO**
- `recusarPagamento` → salva com status **RECUSADO_SEM_INTEGRACAO**

### Consistência Distribuída
Cada microsserviço possui seu próprio banco de dados. A comunicação é feita via OpenFeign (síncrona), com fallback para cenários de indisponibilidade.

---

## ⚙️ Como Executar

### Pré-requisitos
- Java 17+
- PostgreSQL
- MongoDB
- Maven

### Ordem de inicialização

1. **discovery** — Eureka Server (`localhost:8761`)
2. **pedidos** — MS de Pedidos
3. **pagamentos** — MS de Pagamentos
4. **gateway** — API Gateway (`localhost:8081`)

### Variáveis de ambiente (pedidos-ms)
```
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
```

---

## 👨‍💻 Autor

Desenvolvido por **Gabriel Martins**  
[![LINKEDIN](https://go-skill-icons.vercel.app/api/icons?i=linkedin)](https://www.linkedin.com/in/josé-martins-3b8491320)
[![GITHUB](https://go-skill-icons.vercel.app/api/icons?i=github)](https://github.com/Martins20321)
