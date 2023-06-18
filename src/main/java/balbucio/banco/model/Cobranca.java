package balbucio.banco.model;

import balbucio.banco.utils.TokenCreator;
import com.sun.javafx.css.parser.Token;

public class Cobranca {

    private String pedinteToken;
    private String devedorToken;
    private long valor;
    private String token;

    public Cobranca(String pedinteToken, String devedorToken, long valor) {
        this.token = TokenCreator.createToken(10);
        this.pedinteToken = pedinteToken;
        this.devedorToken = devedorToken;
        this.valor = valor;
    }

    public String getPedinteToken() {
        return pedinteToken;
    }

    public void setPedinteToken(String pedinteToken) {
        this.pedinteToken = pedinteToken;
    }

    public String getDevedorToken() {
        return devedorToken;
    }

    public void setDevedorToken(String devedorToken) {
        this.devedorToken = devedorToken;
    }

    public long getValor() {
        return valor;
    }

    public void setValor(long valor) {
        this.valor = valor;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
