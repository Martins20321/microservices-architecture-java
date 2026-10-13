package com.martinsdev.inventario.infra;

import com.martinsdev.inventario.dto.ProdutoCriarRequestDTO;
import com.martinsdev.inventario.repository.ProdutoRepository;
import com.martinsdev.inventario.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventarioDataSeeder.class);
    private final ProdutoRepository repository;
    private final ProdutoService service;

    // Lista de produtos iniciais
    List<ProdutoCriarRequestDTO> produtosCriados = List.of(
            new ProdutoCriarRequestDTO("Teclado Mecânico RGB", "Teclado mecânico com switches blue e iluminação RGB", new BigDecimal("249.90"), 50),
            new ProdutoCriarRequestDTO("Mouse Gamer 7200 DPI", "Mouse óptico com 6 botões programáveis", new BigDecimal("129.90"), 80),
            new ProdutoCriarRequestDTO("Monitor 24\" Full HD", "Monitor LED 24 polegadas, painel IPS, 75Hz", new BigDecimal("749.00"), 30),
            new ProdutoCriarRequestDTO("Headset Gamer USB", "Headset com microfone retrátil e som surround", new BigDecimal("189.90"), 40),
            new ProdutoCriarRequestDTO("Webcam Full HD 1080p", "Webcam com microfone embutido e foco automático", new BigDecimal("159.90"), 35),
            new ProdutoCriarRequestDTO("Notebook 15.6\"", "Notebook 8GB de RAM, SSD 256GB, tela Full HD", new BigDecimal("2899.00"), 10),
            new ProdutoCriarRequestDTO("SSD NVMe 500GB", "SSD M.2 NVMe com leitura de até 3500MB/s", new BigDecimal("299.90"), 60),
            new ProdutoCriarRequestDTO("Memória RAM DDR4 8GB", "Módulo de memória DDR4 3200MHz", new BigDecimal("169.90"), 70),
            new ProdutoCriarRequestDTO("Hub USB 3.0", "Hub com 4 portas USB 3.0 e cabo de 30cm", new BigDecimal("59.90"), 100),
            new ProdutoCriarRequestDTO("Cabo HDMI 2m", "Cabo HDMI 2.0 com suporte a 4K", new BigDecimal("29.90"), 150),
            new ProdutoCriarRequestDTO("Carregador Portátil 10000mAh", "Power bank com duas saídas USB e carga rápida", new BigDecimal("119.90"), 55),
            new ProdutoCriarRequestDTO("Cadeira de Escritório Ergonômica", "Cadeira com apoio lombar e ajuste de altura", new BigDecimal("899.00"), 15),
            new ProdutoCriarRequestDTO("Mousepad Extra Grande", "Mousepad 90x40cm com base antiderrapante", new BigDecimal("49.90"), 120),
            new ProdutoCriarRequestDTO("Suporte para Notebook", "Suporte de alumínio com ajuste de inclinação", new BigDecimal("79.90"), 65),
            new ProdutoCriarRequestDTO("Microfone Condensador USB", "Microfone com tripé e filtro anti-pop", new BigDecimal("259.90"), 25),
            new ProdutoCriarRequestDTO("Roteador Wi-Fi 6", "Roteador dual band com 4 antenas", new BigDecimal("349.90"), 20),
            new ProdutoCriarRequestDTO("Pen Drive 64GB", "Pen drive USB 3.0 com capa protetora", new BigDecimal("39.90"), 200),
            new ProdutoCriarRequestDTO("Fone Bluetooth", "Fone sem fio com cancelamento de ruído", new BigDecimal("99.90"), 90),
            new ProdutoCriarRequestDTO("Smartphone 128GB", "Smartphone com tela de 6.5\", 128GB e câmera dupla", new BigDecimal("1599.00"), 5),
            new ProdutoCriarRequestDTO("Tablet 10\" 64GB", "Tablet com tela de 10 polegadas e Wi-Fi", new BigDecimal("1199.00"), 8)
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (ProdutoCriarRequestDTO produto : produtosCriados) {
            if (repository.existsByNome(produto.nome())) {
                log.info("Product with name '{}' already exists", produto.nome());
                continue;
            }
            service.criarProduto(produto);
        }
    }
}
