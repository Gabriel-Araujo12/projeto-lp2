public class ArvoreAVL{
    NoAVL raiz;
    int total;

    ArvoreAVL(){
        this.raiz = null;
        this.total = 0;
    }

    public int getTotal(){
        return total;
    }

    public int obterAltura(NoAVL no){
        if(no == null){
            return 0;
        }

        return no.altura;
    }

    public int max(int a, int b){
        return (a > b) ? a : b;
    }

    public int obterFatorBalanceamento(NoAVL no){
        if(no == null){
            return 0;
        }

        return obterAltura(no.esquerdo) - obterAltura(no.direito);
    }

    public NoAVL rotacaoDireita(NoAVL no){
        NoAVL filhoEsquerdo = no.esquerdo;
        NoAVL temp = filhoEsquerdo.direito;

        filhoEsquerdo.direito = no;
        no.esquerdo = temp;

        no.altura = max(obterAltura(no.esquerdo), obterAltura(no.direito)) + 1;
        filhoEsquerdo.altura = max(obterAltura(filhoEsquerdo.esquerdo), obterAltura(filhoEsquerdo.direito)) + 1;

        return filhoEsquerdo;
    }

    public NoAVL rotacaoEsquerda(NoAVL no){
        NoAVL filhoDireito = no.direito;
        NoAVL temp = filhoDireito.esquerdo;

        filhoDireito.esquerdo = no;
        no.direito = temp;

        no.altura = max(obterAltura(no.esquerdo), obterAltura(no.direito)) + 1;
        filhoDireito.altura = max(obterAltura(filhoDireito.esquerdo), obterAltura(filhoDireito.direito)) + 1;

        return filhoDireito;
    }

    public NoAVL rotacaoDuplaEsquerdaDireita(NoAVL no){
        no.esquerdo = rotacaoEsquerda(no.esquerdo);
        return rotacaoDireita(no);
    }

    public NoAVL rotacaoDuplaDireitaEsquerda(NoAVL no){
        no.direito = rotacaoDireita(no.direito);
        return rotacaoEsquerda(no);
    }

    public NoAVL inserir(NoAVL no, Eletrodomestico produto){
        if(no == null){
            total++;
            return new NoAVL(produto);
        }

        if(produto.getCodigo() < no.getDado().getCodigo()){
            no.esquerdo = inserir(no.esquerdo, produto);
        }
        else if(produto.getCodigo() > no.getDado().getCodigo()){
            no.direito = inserir(no.direito, produto);
        }
        else{
            no.setDado(produto);
            return no;
        }

        no.altura = 1 + max(obterAltura(no.esquerdo), obterAltura(no.direito));
        int fb = obterFatorBalanceamento(no);

        if(fb > 1 && produto.getCodigo() < no.esquerdo.getDado().getCodigo()){
            return rotacaoDireita(no);
        }
        if (fb < -1 && produto.getCodigo() > no.direito.getDado().getCodigo()){
            return rotacaoEsquerda(no);
        }
        if(fb > 1 && produto.getCodigo() > no.esquerdo.getDado().getCodigo()){
            return rotacaoDuplaEsquerdaDireita(no);
        }
        if (fb < -1 && produto.getCodigo() < no.direito.getDado().getCodigo()){
            return rotacaoDuplaDireitaEsquerda(no);
        }

        return no;
    }

    public NoAVL sucessor(NoAVL no){
        if(no.esquerdo != null){
            return sucessor(no.esquerdo);
        }

        return no;
    }

    public NoAVL remover(NoAVL no, int codigo){
        if(no == null){
            return null;
        }

        if(codigo < no.getDado().getCodigo()){
            no.esquerdo = remover(no.esquerdo, codigo);
        }
        else if(codigo > no.getDado().getCodigo()){
            no.direito = remover(no.direito, codigo);
        }
        else{
            if(no.direito == null){
                total--;
                no = no.esquerdo;
            }
            else if(no.esquerdo == null){
                total--;
                no = no.direito;
            }
            else{
                NoAVL temp = sucessor(no.direito);
                no.setDado(temp.getDado());
                no.direito = remover(no.direito, temp.getDado().getCodigo());
            }
        }

        if(no == null){
            return null;
        }

        no.altura = 1 + max(obterAltura(no.esquerdo), obterAltura(no.direito));
        int fb = obterFatorBalanceamento(no);

        if(fb > 1 && obterFatorBalanceamento(no.esquerdo) >= 0){
            return rotacaoDireita(no);
        }
        if (fb < -1 && obterFatorBalanceamento(no.direito) <= 0){
            return rotacaoEsquerda(no);
        }
        if(fb > 1 && obterFatorBalanceamento(no.esquerdo) < 0){
            return rotacaoDuplaEsquerdaDireita(no);
        }
        if (fb < -1 && obterFatorBalanceamento(no.direito) > 0){
            return rotacaoDuplaDireitaEsquerda(no);
        }

        return no;
    }

    public Eletrodomestico buscar(int codigo){
        NoAVL no = raiz;

        while(no != null){
            if(codigo == no.getDado().getCodigo()){
                return no.getDado();
            }
            if (codigo < no.getDado().getCodigo()){
                no = no.esquerdo;
            }
            else{
                no = no.direito;
            }
        }

        return null;
    }

    public void listarEmOrdem(NoAVL no){
        if(no != null){
            listarEmOrdem(no.esquerdo);
            System.out.println(no.getDado());
            listarEmOrdem(no.direito);
        }
    }

    public boolean estaVazia(){
        return this.raiz == null;
    }
}