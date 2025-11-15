import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Gerencia o registro de eventos tanto no console quanto no arquivo log_avl.txt.
 * 
 */
public class GerenciadorLog {
    private String nomeArquivo = "log_avl.txt"; // 
    private PrintWriter escritorArquivo;

    public GerenciadorLog() {
        try {
            // Abre o arquivo em modo de 'false' (sobrescrever) no início da execução
            this.escritorArquivo = new PrintWriter(new FileWriter(nomeArquivo, false));
        } catch (IOException e) {
            System.err.println("ERRO CRÍTICO: Não foi possível abrir o arquivo de log: " + e.getMessage());
        }
    }

    /**
     * Registra uma mensagem no console e no arquivo de log. [cite: 51, 67]
     */
    public void registrar(String mensagem) {
        // 1. Exibir em tela [cite: 51]
        System.out.println(mensagem);

        // 2. Gravar em arquivo [cite: 67]
        if (escritorArquivo != null) {
            escritorArquivo.println(mensagem);
            escritorArquivo.flush(); // Garante a escrita imediata
        }
    }

    /**
     * Fecha o arquivo de log e registra a mensagem final se a árvore estiver vazia.
     */
    public void fechar(boolean arvoreVazia) {
        if (escritorArquivo != null) {
            // A mensagem final SÓ deve ser registrada se a condição de encerramento for atingida [cite: 83]
            if (arvoreVazia) {
                // Mensagem final de encerramento [cite: 85]
                registrar("\nSimulação encerrada: árvore AVL vazia. Todos os produtos foram consumidos.");
            } else {
                registrar("\nSimulação (baseada em vendas.csv) concluída. Produtos podem ter restado em estoque.");
            }
            escritorArquivo.close();
        }
    }
}

public class Main {

    private ArvoreAVL inventario;
    private GerenciadorLog log;

    public Main() {
        this.inventario = new ArvoreAVL();
        this.log = new GerenciadorLog();
    }

    public static void main(String[] args) {
        Main simulador = new Main();
        simulador.iniciar();
    }

    public void iniciar() {
        // 1. Carregamento Inicial [cite: 22]
        carregarCatalogo("catalogo.csv");

        // 2. Simulador de Consumo Automático [cite: 33]
        executarSimulacao("vendas.csv");
        
        // 3. Listagem final (para verificar o que sobrou)
        log.registrar("\n--- RELATÓRIO FINAL DE ESTOQUE (EM ORDEM) ---");
        // Precisamos chamar listarEmOrdem passando a raiz da árvore
        inventario.listarEmOrdem(inventario.raiz);
        log.registrar("----------------------------------------------");

        // 4. Encerrar o log
        // Passa o status da árvore para o log decidir se imprime a msg final [cite: 83]
        log.fechar(inventario.estaVazia()); 
    }

    /**
     * Carrega o arquivo catalogo.csv e insere os produtos na árvore AVL.
     * [cite: 23, 91]
     */
    private void carregarCatalogo(String nomeArquivo) {
        log.registrar("=== INICIANDO CARGA DO CATÁLOGO: " + nomeArquivo + " ===");
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha = br.readLine(); // Pular cabeçalho 

            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(";");
                
                try {
                    int codigo = Integer.parseInt(campos[0].trim());
                    String nome = campos[1].trim();
                    int estoque = Integer.parseInt(campos[2].trim());
                    double preco = Double.parseDouble(campos[3].trim());

                    // Apenas incluir produtos com estoque > 0 [cite: 24]
                    if (estoque > 0) {
                        Eletrodomestico produto = new Eletrodomestico(codigo, nome, estoque, preco);
                        
                        // *** IMPORTANTE: Adequação ao seu código ***
                        // Seu 'inserir' é recursivo, então precisamos reatribuir a raiz [cite: 26]
                        inventario.raiz = inventario.inserir(inventario.raiz, produto);
                        
                        log.registrar("Inserindo produto " + codigo + ": " + nome); // [cite: 54, 55]
                    } else {
                        log.registrar("Ignorado (estoque 0): " + nome);
                    }
                } catch (Exception e) {
                    log.registrar("ERRO: Linha mal formatada no catálogo: " + linha);
                }
            }
        } catch (IOException e) {
            log.registrar("ERRO CRÍTICO: Falha ao ler " + nomeArquivo + ": " + e.getMessage());
        }
        log.registrar("=== CARGA DO CATÁLOGO CONCLUÍDA. Total de produtos: " + inventario.getTotal() + " ===");
    }

    /**
     * Executa a simulação de consumo lendo o arquivo vendas.csv.
     * [cite: 33, 102]
     */
    private void executarSimulacao(String nomeArquivo) {
        log.registrar("\n=== INICIANDO SIMULAÇÃO DE CONSUMO (vendas.csv) ===");
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha = br.readLine(); // Pular cabeçalho 

            while ((linha = br.readLine()) != null) {
                // Se a árvore estiver vazia, a simulação DEVE parar [cite: 34, 83]
                if (inventario.estaVazia()) {
                    log.registrar("AVISO: A árvore está vazia. Interrompendo simulação de vendas.");
                    break;
                }
                
                // Ignora comentários ou linhas mal formadas
                if (linha.startsWith("//") || linha.trim().isEmpty()) {
                    log.registrar("Info: Ignorando linha de comentário/vazia: " + linha);
                    continue;
                }

                String[] campos = linha.split(";");
                if (campos.length < 2) continue;

                try {
                    int codigo = Integer.parseInt(campos[0].trim());
                    int qtdConsumir = Integer.parseInt(campos[1].trim());

                    log.registrar("\n-> Processando Venda: Cód " + codigo + ", Qtd: " + qtdConsumir);
                    processarConsumo(codigo, qtdConsumir);
                    
                    // Logar a quantidade total após cada operação [cite: 72, 78]
                    log.registrar("AVL atualmente com " + inventario.getTotal() + " produtos."); // [cite: 58]

                } catch (Exception e) {
                    log.registrar("ERRO: Linha mal formatada em vendas: " + linha);
                }
            }
        } catch (IOException e) {
            log.registrar("ERRO CRÍTICO: Falha ao ler " + nomeArquivo + ": " + e.getMessage());
        }
    }

    /**
     * Processa o consumo (venda) de um único produto.
     * [cite: 36-44]
     */
    private void processarConsumo(int codigo, int qtdConsumir) {
        // 1. Busca o produto na árvore [cite: 37]
        Eletrodomestico produto = inventario.buscar(codigo);

        // 2. Ações possíveis [cite: 38]
        
        // Caso: Produto esgotado (não está na árvore) [cite: 39]
        if (produto == null) {
            log.registrar("Alerta: Produto " + codigo + " fora de estoque (já removido)."); // [cite: 57, 71]
            return;
        }

        // Exemplo: 105;0 (não faz nada) [cite: 114]
        if (qtdConsumir <= 0) {
            log.registrar("Info: Tentativa de consumo de 0 unidades do produto " + codigo + ". Estoque inalterado.");
            return;
        }

        int estoqueAtual = produto.getQuantidadeEstoque();

        // Caso: Consumo total (estoque vai a zero ou menos) [cite: 42]
        if (qtdConsumir >= estoqueAtual) {
            log.registrar("Consumo total: Produto " + codigo + " (" + produto.getNome() + "). Estoque era " + estoqueAtual + ", consumo " + qtdConsumir + ".");
            
            // *** IMPORTANTE: Adequação ao seu código ***
            // Seu 'remover' é recursivo, então precisamos reatribuir a raiz [cite: 42]
            inventario.raiz = inventario.remover(inventario.raiz, codigo);
            
            log.registrar("Produto " + codigo + " removido do inventário."); // [cite: 56, 70, 77]
        }
        // Caso: Consumo parcial [cite: 41]
        else {
            produto.reduzirEstoque(qtdConsumir); // Usando seu método de Eletrodomestico
            log.registrar("Consumo parcial: Produto " + codigo + " (" + produto.getNome() + "). Novo estoque: " + produto.getQuantidadeEstoque());
        }
    }
}