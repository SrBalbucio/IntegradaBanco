package balbucio.banco.model;

import balbucio.banco.utils.TokenCreator;
import com.sun.javafx.css.parser.Token;

public class CofreTransference {

    private String owner;
    private int valor;

    private String token;

    public CofreTransference(String owner, int valor, String token) {
        this.owner = owner;
        this.valor = valor;
        this.token = token;
    }

    public CofreTransference(String owner, int valor) {
        this.token = TokenCreator.createToken(10);
        this.owner = owner;
        this.valor = valor;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
