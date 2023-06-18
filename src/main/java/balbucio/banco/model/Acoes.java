package balbucio.banco.model;

import balbucio.banco.Main;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.utils.TokenCreator;
import com.google.gson.Gson;

public class Acoes {

    private String token;
    private String actionName;
    private String recebedor;

    public Acoes(String actionName, String recebedor) {
        this.token = TokenCreator.createToken(10);
        this.actionName = actionName;
        this.recebedor = recebedor;
        Main.getSqlite().insert("name, recebedor, token", "'"+actionName+"', '"+recebedor+"', '"+token+"'", "acoes");
        MercadoManager.acoes.add(this);
        if(Main.connected()) {
            Main.request("CREATEACAO", new Gson().toJson(this));
        }
    }

    public void reload(){
        Main.getSqlite().insert("name, recebedor, token", "'"+actionName+"', '"+recebedor+"', '"+token+"'", "acoes");
        MercadoManager.acoes.add(this);
    }

    public Acoes(String token, String actionName, String recebedor) {
        this.token = token;
        this.actionName = actionName;
        this.recebedor = recebedor;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getRecebedor() {
        return recebedor;
    }

    public void setRecebedor(String recebedor) {
        this.recebedor = recebedor;
    }

    public void delete(){
        if(Main.connected()) {
            MercadoManager.acoes.remove(this);
            Main.getSqlite().delete("token", token, "acoes");
        } else{
            Main.request("REMOVEACAO", token);
        }
    }
}
