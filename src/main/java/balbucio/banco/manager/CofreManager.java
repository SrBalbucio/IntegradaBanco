package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.CofreTransference;
import balbucio.banco.model.Emprestimo;
import balbucio.banco.model.User;
import com.google.gson.Gson;
import jdk.jfr.Unsigned;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CofreManager {

    public static List<CofreTransference> transferences = new ArrayList<>();

    public CofreManager(){
        List<Object[]> u = Main.getSqlite().getAllValuesFromColumns("cofre", "owner", "value", "token");
        System.out.println(u.size());
        for(Object[] t : u){
            transferences.add(new CofreTransference((String) t[0], (int) t[1], (String) t[2]));
        }
    }

    public static List<CofreTransference> getTransferences(User user){
        if(Main.connected()){
            JSONArray string = (JSONArray)  Main.request("GETCOFRE", new Gson().toJson(user));
            List<CofreTransference> emps = new ArrayList<>();
            string.forEach(s -> emps.add(new Gson().fromJson((String) s, CofreTransference.class)));
            return emps;
        }
        return transferences.stream().filter(e -> e.getOwner().equalsIgnoreCase(user.getToken())).toList();
    }

    public static void createTransference(User user, int valor){
        if(Main.connected()){
            Main.request("CREATECOFRE", user.getToken()+":"+valor);
            return;
        }
        CofreTransference t = new CofreTransference(user.getToken(), valor);
        transferences.add(t);
        Main.getSqlite().insert("owner, value, token", "'"+t.getOwner()+"', '"+t.getValor()+"', '"+t.getToken()+"'", "cofre");
    }

    public static void pegaDinheiro(User user, int valor){
        if(Main.connected()){
            Main.request("CREATECOFRE", user.getToken()+":-"+valor);
            return;
        }
        CofreTransference t = new CofreTransference(user.getToken(), (int) Integer.parseInt("-"+valor));
        transferences.add(t);
        Main.getSqlite().insert("owner, value, token", "'"+t.getOwner()+"', '"+t.getValor()+"', '"+t.getToken()+"'", "cofre");
    }
}
