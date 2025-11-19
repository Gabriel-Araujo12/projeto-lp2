import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            registrar("\nSimulação encerrada: árvore AVL vazia.");

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
        Main simulador = new Main();
        simulador.iniciar();
    }

    public void iniciar(){
        carregar("catalogo.csv");

        executar();
        
        csv.fechar();
        log.fechar();
    }

    private void carregar(String nomeArquivo){
        log.registrar("==============      INICIANDO CARREGAMENTO DO CATÁLOGO      ==============");
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
                    log.registrar("ERRO: Linha mal formatada no catálogo: " + linha);
                }
            }
        } 
        catch (IOException e){
            log.registrar("ERRO: Falha ao ler " + nomeArquivo + ": " + e.getMessage());
        }
        
        this.listaAuxiliar = new int[codigosTemp.size()];
        for(int i = 0; i < codigosTemp.size(); i++){
            this.listaAuxiliar[i] = codigosTemp.get(i);
        }

        log.registrar("AVL atualmente com " + inventario.getTotal() + " produtos.");
    }

    private void executar(){
        log.registrar("\n==============   INICIANDO SIMULAÇÃO AUTOMÁTICA   ==============");
        
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
}