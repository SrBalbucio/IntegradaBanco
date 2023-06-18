package balbucio.banco.manager;

import balbucio.banco.model.Cobranca;
import balbucio.banco.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CobrancaManager {

    public static HashMap<String, List<Cobranca>> cobrancas = new HashMap<>();

    public static HashMap<String, List<Cobranca>> getCobrancas() {
        return cobrancas;
    }

    public static void setCobrancas(HashMap<String, List<Cobranca>> cobrancas) {
        CobrancaManager.cobrancas = cobrancas;
    }

    public static void addCobranca(User cobrante, User cobrado, long value){
        if(cobrancas.containsKey(cobrado.getToken())){
            cobrancas.get(cobrado.getToken()).add(new Cobranca(cobrante.getToken(), cobrado.getToken(), value));
        } else{
            List<Cobranca> c = new ArrayList<>();
            c.add(new Cobranca(cobrante.getToken(), cobrado.getToken(), value));
            cobrancas.put(cobrado.getToken(), c);
        }
    }
}
