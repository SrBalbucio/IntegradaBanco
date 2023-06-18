package balbucio.banco.model;

import balbucio.banco.utils.TokenCreator;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String token = TokenCreator.createToken(24);
    private String name;
    private String password;
    private long saldo = 0L;

    public User(String token, String name, String password, long saldo) {
        this.token = token;
        this.name = name;
        this.password = password;
        this.saldo = saldo;
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
        this.saldo = saldo;
    }

    public void transference(Transference transference){
        if(transference.getTokenRecebedor().equalsIgnoreCase(token)){
            this.saldo += transference.getValue();
        } else if(transference.getTokenPagante().equalsIgnoreCase(token)){
            this.saldo -= transference.getValue();
        }
    }

    public User clone(){
        return new User(token, name, password, saldo);
    }
}
