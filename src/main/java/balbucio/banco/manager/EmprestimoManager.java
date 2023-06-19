package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Emprestimo;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import com.google.gson.Gson;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmprestimoManager {

    public static List<Emprestimo> emprestimos = new ArrayList<>();

    public EmprestimoManager(){
        List<Object[]> u = Main.getSqlite().getAllValuesFromColumns("emprestimos", "devedor", "value", "time", "token");
        System.out.println(u.size());
        for(Object[] t : u){
            emprestimos.add(new Emprestimo((String) t[3], (String) t[0], (int) t[1], (long) t[2]));
        }
    }

    public static Emprestimo createEmprestimo(User user, int value){
        if(Main.connected()){
            return new Gson().fromJson((String) Main.request("CREATEEMPRESTIMO", user.getToken()+":"+value), Emprestimo.class);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Emprestimo emprestimo = new Emprestimo(user.getToken(), value, calendar.getTimeInMillis());
        emprestimos.add(emprestimo);
        Main.getSqlite().insert("devedor, value, time, token", "'"+emprestimo.getDevedor()+"', '"+emprestimo.getValor()+"', '"+emprestimo.getMaxTime()+"','"+emprestimo.getToken()+"'", "emprestimos");
        return emprestimo;
    }

    public static void deleteEmprestimo(String token){
        emprestimos.stream().filter(e -> e.getToken().equalsIgnoreCase(token)).findFirst().ifPresent(e -> {
            emprestimos.remove(e);
            Main.getSqlite().update("DELETE FROM emprestimos WHERE token = '"+e.getToken()+"'");
            //Main.getSqlite().delete("token", token, "emprestimos");
        });
    }

    public static List<Emprestimo> getEmprestimos(User user){
        if(Main.connected()){
            JSONArray string = (JSONArray)  Main.request("GETEMPRESTIMOS", new Gson().toJson(user));
            List<Emprestimo> emps = new ArrayList<>();
            string.forEach(s -> emps.add(new Gson().fromJson((String) s, Emprestimo.class)));
            return emps;
        }
        return emprestimos.stream().filter(e -> e.getDevedor().equalsIgnoreCase(user.getToken())).toList();
    }

}
