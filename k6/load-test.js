// Importa o módulo http para realizar requisições HTTP
import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE_URL = 'http://localhost:8081' // URL base do gateway
const produtoIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20] // IDs dos produtos disponíveis no inventário
const formaPagamentos = ["CARTAO_CREDITO", "CARTAO_DEBITO", "PIX","BOLETO"] // Formas de pagamento disponíveis

const endpointsGet = [
    {method: 'GET', path: `${BASE_URL}/pedidos-service/v1/pedidos`},
    {method: 'GET', path: `${BASE_URL}/pagamentos-service/v1/pagamentos`},
    {method: 'GET', path: `${BASE_URL}/inventario-service/v1/produtos`},
]

// Define as opções do teste de carga
export const options = {
    stages: [
        { duration: '30s', target: 10 }, // Aumenta gradualmente o número de usuários virtuais para 10 em 30 segundos
        { duration: '1m', target: 10 }, // Mantém 10 usuários virtuais por 1 minuto
        { duration: '30s', target: 0 }, // Reduz gradualmente o número de usuários virtuais para 0 em 30 segundos
    ],
}

// Função principal do teste de carga
export default function () {
    const produtoId = produtoIds[Math.floor(Math.random() * produtoIds.length)] // Seleciona um ID de produto aleatório
    const formaPagamento = formaPagamentos[Math.floor(Math.random() * formaPagamentos.length)] // Seleciona uma forma de pagamento aleatória
    const quantidade = Math.floor(Math.random() * 5) + 1 // Gera uma quantidade aleatória entre 1 e 5
    const endpointGet = endpointsGet[Math.floor(Math.random() * endpointsGet.length)] // Seleciona um endpoint GET aleatório
    const REQUEST_DELAY = Math.floor(Math.random() * 3) + 1 // Gera um tempo aleatório entre 1 e 3 segundos para aguardar entre as requisições

    const pedidoResponse = http.post(`${BASE_URL}/pedidos-service/v1/pedidos`, JSON.stringify({itens: [{ produtoId: produtoId, quantidade: quantidade }] }), { headers: { 'Content-Type': 'application/json' } }) // Realiza uma requisição POST para o gateway criando um novo produto
    check(pedidoResponse, {
         'pedido criado com sucesso': (r) => r.status === 201 
        }) // Verifica se o pedido foi criado com sucesso
    if (pedidoResponse.status !== 201) {
        console.error(`Falha ao criar pedido. Status: ${pedidoResponse.status}, Body: ${pedidoResponse.body}`)
        return // Encerra a execução da função se o pedido não for criado com sucesso
    }

    const pagamentoResponse = http.post(`${BASE_URL}/pagamentos-service/v1/pagamentos`, JSON.stringify({pedidoId: pedidoResponse.json().id, formaPagamento: formaPagamento }), { headers: { 'Content-Type': 'application/json' } }) // Realiza uma requisição POST para o gateway criando um novo produto
    check(pagamentoResponse, {
         'pagamento criado com sucesso': (r) => r.status === 201 
        }) // Verifica se o pagamento foi criado com sucesso
    if (pagamentoResponse.status !== 201) {
        console.error(`Falha ao criar pagamento. Status: ${pagamentoResponse.status}, Body: ${pagamentoResponse.body}`)
        return // Encerra a execução da função se o pagamento não for criado com sucesso
    }

    const pagamentoAprovadoResponse = http.patch(`${BASE_URL}/pagamentos-service/v1/pagamentos/${pagamentoResponse.json().id}/aprovar`, { headers: { 'Content-Type': 'application/json' } }) // Realiza uma requisição PATCH para o gateway aprovando o pagamento
    check(pagamentoAprovadoResponse, {
         'pagamento aprovado com sucesso': (r) => r.status === 200 
        }) // Verifica se o pagamento foi aprovado com sucesso
    
    const getResponse = http.request(endpointGet.method, endpointGet.path) // Realiza uma requisição GET para o endpoint selecionado
    check(getResponse, {
         'requisição GET bem-sucedida': (r) => r.status === 200 
        }) // Verifica se a requisição GET foi bem-sucedida
    
    sleep(REQUEST_DELAY) // Aguarda um tempo aleatório entre 1 e 3 segundos antes de iniciar a próxima iteração
}