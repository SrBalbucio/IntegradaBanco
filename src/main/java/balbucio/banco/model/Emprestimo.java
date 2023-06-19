package balbucio.banco.model;

import balbucio.banco.utils.TokenCreator;
import com.sun.javafx.css.parser.Token;

public class Emprestimo {

    private String token;
    private String devedor;
    private int valor;
    private long maxTime;

    public Emprestimo(String devedor, int valor, long maxTime) {
        this.token = TokenCreator.createToken(10);
        this.devedor = devedor;
        this.valor = valor;
        this.maxTime = maxTime;
    }

    public Emprestimo(String token, String devedor, int valor, long maxTime) {
        this.token = token;
        this.devedor = devedor;
        this.valor = valor;
        this.maxTime = maxTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDevedor() {
        return devedor;
    }

    public void setDevedor(String devedor) {
        this.devedor = devedor;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }
}
