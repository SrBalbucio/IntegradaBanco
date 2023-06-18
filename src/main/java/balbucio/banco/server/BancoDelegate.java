package balbucio.banco.server;

import balbucio.banco.Main;
import balbucio.banco.manager.CobrancaManager;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Cobranca;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import co.gongzh.procbridge.IDelegate;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BancoDelegate implements IDelegate {

    @Override
    public @Nullable Object handleRequest(@Nullable String s, @Nullable Object o) {
        System.out.println("REQUEST DO SERVIDOR: "+o);
        if(s.equalsIgnoreCase("GETUSER")){
            String[] cre = ((String) o).split(":");
            return new Gson().toJson(UserManager.getInstance().getUser(cre[0], cre[1]));
        } else if(s.equalsIgnoreCase("EXISTUSER")){
            String[] cre = ((String) o).split(":");
            return UserManager.getInstance().existUser(cre[0], cre[1]);
        } else if(s.equalsIgnoreCase("CREATEUSER")){
            String[] cre = ((String) o).split(":");
            return new Gson().toJson(UserManager.getInstance().createUser(cre[0], cre[1]));
        } else if(s.equalsIgnoreCase("GETUSERNAME")){
            return UserManager.getInstance().getUserName((String) o);
        } else if(s.equalsIgnoreCase("GETUSERBYNAME")){
            return new Gson().toJson(UserManager.getInstance().getUserByName((String) o));
        } else if(s.equalsIgnoreCase("CREATETRANSFERENCE")){
            TransferenceManager.addTransference(new Gson().fromJson((String) o, Transference.class));
        } else if(s.equalsIgnoreCase("GETRANSFERENCES")){
            List<Transference> transferenceList = TransferenceManager.getTransferences(new Gson().fromJson((String) o, User.class));
            List<String> sc = new ArrayList<>();
            transferenceList.forEach(t -> sc.add(new Gson().toJson(t)));
            return sc;
        } else if(s.equalsIgnoreCase("GETACOES")){
            List<Acoes> acoesList = MercadoManager.getAcoes(new Gson().fromJson((String) o, User.class));
            List<String> sc = new ArrayList<>();
            acoesList.forEach(t -> sc.add(new Gson().toJson(t)));
            return sc;
        } else if(s.equalsIgnoreCase("GETACOESVALORES")){
            return MercadoManager.valores;
        } else if(s.equalsIgnoreCase("CREATEACAO")){
            Acoes acao = new Gson().fromJson((String) o, Acoes.class);
            acao.reload();
        } else if(s.equalsIgnoreCase("DELETEACAO")){
            Main.getSqlite().delete("token", (String) o, "acoes");
            MercadoManager.acoes.stream().filter(a -> a.getToken().equalsIgnoreCase((String) o)).findFirst().ifPresent(a -> MercadoManager.acoes.remove(a));
        } else if(s.equalsIgnoreCase("GETCOBRANCAS")){
            List<String> string = new ArrayList<>();
            if(CobrancaManager.cobrancas.containsKey((String) o)){
                for(Cobranca c : CobrancaManager.cobrancas.get((String) o)){
                    string.add(new Gson().toJson(c));
                }
            }
            return string;
        } else if(s.equalsIgnoreCase("DELETECOBRANCA")){
            String[] cre = ((String) o).split(";");
            if(CobrancaManager.getCobrancas().containsKey(cre[0])){
                CobrancaManager.getCobrancas().get(cre[0]).stream().filter(c -> c.getToken().equalsIgnoreCase(cre[1])).findFirst().ifPresent(e -> CobrancaManager.getCobrancas().get(cre[0]).remove(e));
            }
        } else if(s.equalsIgnoreCase("GETJUROS")){
            return MercadoManager.juros;
        }
        return null;
    }
}
