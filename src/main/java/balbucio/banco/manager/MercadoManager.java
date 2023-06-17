package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.utils.NumberUtils;
import balbucio.responsivescheduler.RSTask;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MercadoManager {

    public static Map<String, Integer> valores = new HashMap<>();
    public static List<Acoes> acoes = new ArrayList<>();
    private static MercadoManager instance;

    public MercadoManager(SQLiteInstance sqlite){
        instance = this;
        valores.put("LAA2", 40);
        valores.put("LBA2", 40);
        valores.put("LCA2", 40);
        valores.put("LDA2", 40);
        valores.put("L2A2", 40);
        valores.put("CA2", 40);
        valores.put("DA2", 40);
        valores.put("7AA2", 40);
        valores.put("LFA2", 40);
        valores.put("zzA2", 40);

        List<Object[]> u = sqlite.getAllValuesFromColumns("acoes", "name", "recebedor", "token");
        for(Object[] t : u){
            acoes.add(new Acoes((String) t[2], (String) t[0], (String) t[1]));
        }
        Main.getScheduler().repeatTask(new RSTask() {
            @Override
            public void run() {
                if(Main.connected){
                    valores = (Map<String, Integer>) Main.request("GETACOESVALORES", "");
                    return;
                }
                valores.forEach((s, i) -> valores.replace(s, NumberUtils.getRandomNumber(40, 150)));
                acoes.forEach(a -> {
                    System.out.println("Mercado se movimentou e o user "+a.getRecebedor()+" ganhou "+valores.get(a.getActionName()));
                    TransferenceManager.createTransference("Mercado de Ações", a.getRecebedor(), valores.get(a.getActionName()));
                });
            }
        }, 0, 10000);
    }

    public static List<Acoes> getAcoes(User user){
        if(Main.connected){
            List<String> string = (List<String>) Main.request("GETACOES", new Gson().toJson(user));
            List<Acoes> acoesG = new ArrayList<>();
            string.forEach(s -> acoesG.add(new Gson().fromJson(s, Acoes.class)));
            return acoesG;
        }
        return acoes.stream().filter(a -> a.getRecebedor().equalsIgnoreCase(user.getToken())).toList();
    }
}
