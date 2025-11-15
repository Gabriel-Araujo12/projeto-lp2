public class Eletrodomestico{
    private int codigo;
    private String nome;
    private int quantidadeEstoque;
    private double precoUnitario;

    public Eletrodomestico(int codigo, String nome, int quantidadeEstoque, double precoUnitario){
        this.codigo = codigo;
        this.nome = nome;
        this.quantidadeEstoque = quantidadeEstoque;
        this.precoUnitario = precoUnitario;
    }

    public int getCodigo(){ 
        return codigo; 
    }
    public String getNome(){
        return nome; 
    }
    public int getQuantidadeEstoque(){
        return quantidadeEstoque; 
    }
    public double getPrecoUnitario(){
        return precoUnitario; 
    }

    public void reduzirEstoque(int qntd){
        this.quantidadeEstoque -= qntd;
        
        if(this.quantidadeEstoque < 0){
            this.quantidadeEstoque = 0;
        }
    }

    @Override
    public String toString(){
        return String.format("Código: %d | Nome: %s | Estoque: %d | Preço: R$ %.2f", codigo, nome, quantidadeEstoque, precoUnitario);
    }
}