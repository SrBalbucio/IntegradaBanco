package balbucio.banco.manager;

import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.sqlapi.sqlite.SQLiteInstance;

import java.util.ArrayList;
import java.util.List;

public class TransferenceManager {

    private static List<Transference> transferences = new ArrayList<>();
    private static TransferenceManager instance;

    public TransferenceManager(SQLiteInstance sqlite){
        instance = this;
        List<Object[]> u = sqlite.getAllValuesFromColumns("transferences", "pagante", "recebedor", "value", "time");
        for(Object[] t : u){
            transferences.add(new Transference((String) t[0], (String) t[1], (int) t[2], (int) t[3]));
        }
    }

    public static List<Transference> getTransferences(User user){
        return transferences.stream().filter(t -> t.getTokenPagante().equalsIgnoreCase(user.getToken()) || t.getTokenRecebedor().equalsIgnoreCase(user.getToken())).toList();
    }
    public static void createTransference(User user, String token, long value){
        Transference transference = new Transference(user.getToken(), token, value);
    }
}
