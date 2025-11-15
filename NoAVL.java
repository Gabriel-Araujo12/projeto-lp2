public class NoAVL{
    private Eletrodomestico dado;
    NoAVL esquerdo;
    NoAVL direito;
    int altura;

    public NoAVL(Eletrodomestico dado){
        this.dado = dado;
        this.esquerdo = null;
        this.direito = null;
        this.altura = 1;
    }

    public Eletrodomestico getDado(){ 
        return dado; 
    }
    public void setDado(Eletrodomestico dado){
        this.dado = dado;
    }
}