package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TransferenceManager {

    private static List<Transference> transferences = new ArrayList<>();
    private static TransferenceManager instance;

    public TransferenceManager(SQLiteInstance sqlite){
        instance = this;
        List<Object[]> u = sqlite.getAllValuesFromColumns("transferences", "pagante", "recebedor", "value", "time");
        for(Object[] t : u){
            transferences.add(new Transference((String) t[0], (String) t[1], (int) t[2], (long) t[3]));
        }
    }

    public static List<Transference> getTransferences(User user){
        if(Main.connected){
            List<String> string = (List<String>) Main.request("GETRANSFERENCES", user);
            List<Transference> transferencesG = new ArrayList<>();
            string.forEach(s -> transferencesG.add(new Gson().fromJson(s, Transference.class)));
            return transferencesG;
        }
        return transferences.stream().filter(t -> t.getTokenPagante().equalsIgnoreCase(user.getToken()) || t.getTokenRecebedor().equalsIgnoreCase(user.getToken())).toList();
    }
    public static void createTransference(User user, String token, long value){
        Transference transference = new Transference(token, user.getToken(), value);
        if(Main.connected){
            Main.request("CREATETRANSFERENCE", new Gson().toJson(transference));
        }
        transferences.add(transference);
    }

    public static void createTransference(String t, String token, long value){
        Transference transference = new Transference(t, token, value);
        if(Main.connected){
            Main.request("CREATETRANSFERENCE", new Gson().toJson(transference));
        }
        transferences.add(transference);
    }

    public static void removeTransference(User user, String token, long value){
        Transference transference = new Transference(user.getToken(), token, value);
        if(Main.connected){
            Main.request("CREATETRANSFERENCE", new Gson().toJson(transference));
        }
        transferences.add(transference);
    }

    public static void addTransference(Transference transference){
        transferences.add(transference);
        transference.readd();
    }
}
