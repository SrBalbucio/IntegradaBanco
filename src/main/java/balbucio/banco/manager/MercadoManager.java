package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Transference;
import balbucio.banco.utils.NumberUtils;
import balbucio.responsivescheduler.RSTask;
import balbucio.sqlapi.sqlite.SQLiteInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MercadoManager {

    private static Map<String, Integer> valores = new HashMap<>();
    private static List<Acoes> acoes = new ArrayList<>();
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
            acoes.add(new Acoes((String) t[0], (String) t[1], (String) t[2]));
        }
        Main.getScheduler().repeatTask(new RSTask() {
            @Override
            public void run() {
                valores.forEach((s, i) -> valores.replace(s, NumberUtils.getRandomNumber(40, 150)));
                acoes.forEach(a -> {
                    new Transference("Mercado de Ações", a.getToken(), valores.get(a.getActionName()));
                });
            }
        }, 0, 10000);
    }
}