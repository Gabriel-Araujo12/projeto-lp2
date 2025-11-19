import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Log{
    private String nomeArquivo = "log_avl.txt";
    private PrintWriter escritorLog;

    public Log(){
        try{
            this.escritorLog = new PrintWriter(new FileWriter(nomeArquivo, false));
        } 
        catch(IOException e){
            System.err.println("ERRO: Não foi possível abrir o arquivo de log: " + e.getMessage());
        }
    }

    public void registrar(String mensagem){
        System.out.println(mensagem);

        if(escritorLog != null){
            escritorLog.println(mensagem);
            escritorLog.flush();
        }
    }

    public void fechar(){
        if(escritorLog != null){
            escritorLog.close();
        }
    }
}

class Csv{
    private PrintWriter escritorCsv;

    public Csv(){
        try {
            this.escritorCsv = new PrintWriter(new FileWriter("vendas.csv", false));
            
            this.escritorCsv.println("Código;Quantidade");
            this.escritorCsv.flush();
        } 
        catch(IOException e){
            System.err.println("ERRO: Não foi possível criar vendas.csv: " + e.getMessage());
        }
    }

    public void registrarVenda(int codigo, int quantidade){
        if(escritorCsv != null){
            escritorCsv.println(codigo + ";" + quantidade);
            escritorCsv.flush();
        }
    }

    public void fechar(){
        if(escritorCsv != null){
            escritorCsv.close();
        }
    }
}

public class Main{
    private ArvoreAVL inventario;
    private Log log;
    private Csv csv;

    private int[] listaAuxiliar;
    private Random gerador;

    public Main(){
        this.inventario = new ArvoreAVL();
        this.log = new Log();
        this.csv = new Csv();
        this.gerador = new Random();
    }

    public static void main(String[] args){
        Scanner entrada = new Scanner(System.in);
        Main simulador = new Main();
        int opcao = 0;

        do{
            System.out.println("|------------------------------|");
            System.out.println("| 1 - Carregamento Inicial     |");
            System.out.println("| 2 - Executar Simulação       |");
            System.out.println("| 3 - Listar Catálogo          |");
            System.out.println("| 4 - Sair                     |");
            System.out.println("|------------------------------|");
            System.out.print("Digite uma opção: ");

            try {
                opcao = entrada.nextInt();
            } 
            catch(Exception e){
                System.out.println("\nAlerta: Digite apenas números.");
                entrada.nextLine();
                opcao = 0;
                continue;
            }

            switch(opcao){
                case 1:
                    System.out.println();
                    simulador.carregar("catalogo.csv");
                    System.out.println();
                    break;

                case 2:
                    if(simulador.listaAuxiliar == null){
                        System.out.println("\nAlerta: Carregue o catálogo antes da simulação.\n");
                    } 
                    else{
                        simulador.executar();
                        simulador.log.registrar("\nSimulação encerrada: árvore AVL vazia. Todos os produtos foram consumidos.");
                        System.out.println();
                    }

                    break;

                case 3:
                    simulador.listarCatalogo();
                    break;

                case 4:
                    System.out.println("\nEncerrando Sistema!");
                    simulador.csv.fechar();
                    simulador.log.fechar();
                    break;

                default:
                    System.out.print("\nOpção Inválida!");
                    break;    
            }
        } while(opcao != 4);
    }

    private void carregar(String nomeArquivo){
        System.out.println("==============      INICIANDO CARREGAMENTO DO CATÁLOGO      ==============");
        List<Integer> codigosTemp = new ArrayList<>(); 

        try(BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))){
            String linha = br.readLine();

            while((linha = br.readLine()) != null){
                String[] campos = linha.split(";");
                try{
                    int codigo = Integer.parseInt(campos[0].trim());
                    String nome = campos[1].trim();
                    int estoque = Integer.parseInt(campos[2].trim());
                    double preco = Double.parseDouble(campos[3].trim());

                    if(estoque > 0){ 
                        Eletrodomestico produto = new Eletrodomestico(codigo, nome, estoque, preco);
                        
                        inventario.raiz = inventario.inserir(inventario.raiz, produto); 
                        log.registrar("Inserindo produto " + codigo + " - " + nome);
                        
                        codigosTemp.add(codigo); 
                    } 
                    else{
                        log.registrar("Alerta: Produto " + codigo + " não foi inserido pois a quantidade no estoque é incompatível.");
                    }
                }
                catch (Exception e){
                    log.registrar("Alerta: Linha mal formatada no catálogo: " + linha);
                }
            }
        } 
        catch (IOException e){
            log.registrar("Alerta: Falha ao ler " + nomeArquivo + ": " + e.getMessage());
        }
        
        this.listaAuxiliar = new int[codigosTemp.size()];
        for(int i = 0; i < codigosTemp.size(); i++){
            this.listaAuxiliar[i] = codigosTemp.get(i);
        }

        log.registrar("AVL atualmente com " + inventario.getTotal() + " produtos.");
    }

    private void executar(){
        System.out.println("\n==============        INICIANDO SIMULAÇÃO AUTOMÁTICA        ==============");
        
        if(this.listaAuxiliar == null || this.listaAuxiliar.length == 0){
            log.registrar("ERRO: Lista de sorteio vazia.");

            return;
        }

        while(!inventario.estaVazia()){
            int indiceSorteado = gerador.nextInt(this.listaAuxiliar.length);
            int codigoSorteado = this.listaAuxiliar[indiceSorteado];
            int qntd = gerador.nextInt(10) + 1;

            consumir(codigoSorteado, qntd);
        }
    }

    private void consumir(int codigo, int qntd){
        Eletrodomestico produto = inventario.buscar(codigo);

        if(produto == null){
            log.registrar("Alerta: Produto " + codigo + " fora de estoque.");

            return;
        }

        int estoqueAtual = produto.getQuantidadeEstoque();
        int quantidadeVendida = 0;
        boolean vendaConfirmada = false;

        if(estoqueAtual <= qntd){
            quantidadeVendida = estoqueAtual;

            log.registrar("Produto " + codigo + " consumido - " + quantidadeVendida + " unidades.");
            inventario.raiz = inventario.remover(inventario.raiz, codigo);
            log.registrar("Produto " + codigo + " removido - estoque esgotado.");
            vendaConfirmada = true;
        } 
        else{
            quantidadeVendida = qntd;

            produto.reduzirEstoque(quantidadeVendida);
            log.registrar("Produto " + codigo + " consumido - " + quantidadeVendida + " unidades.");
            vendaConfirmada = true;
        }

        if(vendaConfirmada){
            csv.registrarVenda(codigo, quantidadeVendida);
            
            log.registrar("AVL atualmente com " + inventario.getTotal() + " produtos.");
        }
    }

    public void listarCatalogo(){
        if(inventario.estaVazia()){
            System.out.println();
            System.out.println("Alerta: O catálogo está vazio.");
            System.out.println();
        } 
        else{
            System.out.println("\n==============                ESTOQUE ATUAL                 ==============");
            inventario.listarEmOrdem(inventario.raiz);
            System.out.println();
        }
    }
}