package balbucio.banco;

import balbucio.banco.frame.LoginFrame;
import balbucio.banco.listener.TaskListener;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.utils.NumberUtils;
import balbucio.org.ejsl.frame.JLoadingFrame;
import balbucio.org.ejsl.utils.ImageUtils;
import balbucio.responsivescheduler.RSTask;
import balbucio.responsivescheduler.ResponsiveScheduler;
import balbucio.responsivescheduler.event.Listener;
import balbucio.responsivescheduler.event.impl.TaskFinishedEvent;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import balbucio.sqlapi.sqlite.SqliteConfig;
import co.gongzh.procbridge.Client;
import co.gongzh.procbridge.IDelegate;
import co.gongzh.procbridge.Server;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanContrastIJTheme;
import com.google.gson.Gson;
import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.components.WaitingDialog;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    private SQLiteInstance sqlite;
    private SqliteConfig sqliteConfig;
    private  ResponsiveScheduler scheduler;
    private UiBooster booster;
    public Server server;
    public Client client;
    public boolean connected = false;
    private static Main instance;
    public Main(){
        instance = this;
        JLoadingFrame loadingFrame = new JLoadingFrame("Loading", ImageUtils.getImage("https://img.freepik.com/free-vector/bank-building-with-cityscape_1284-52265.jpg?w=2000"), 100);
        sqliteConfig = new SqliteConfig(new File("database.db"));
        sqliteConfig.createFile();
        sqlite = new SQLiteInstance(sqliteConfig);
        sqlite.createTable("users", "name VARCHAR(255), password VARCHAR(255), token VARCHAR(255), saldo BIGINT");
        sqlite.createTable("transferences", "pagante VARCHAR(255), recebedor VARCHAR(255), value BIGINT, time BIGINT");
        sqlite.createTable("acoes", "name VARCHAR(255), recebedor VARCHAR(255), token VARCHAR(255)");
        loadingFrame.setPosition(25);
        booster = new UiBooster();
        loadingFrame.setPosition(30);
        try {
            scheduler = new ResponsiveScheduler();
            scheduler.getEventManager().registerListener(new TaskListener());
            scheduler.repeatTask(new RSTask() {
                @Override
                public void run() {
                    System.out.println("Reload do mercado");
                    if(!Main.instance.connected) {
                        MercadoManager.valores.forEach((s, i) -> MercadoManager.valores.replace(s, NumberUtils.getRandomNumber(40, 150)));
                        MercadoManager.acoes.forEach(a -> {
                            System.out.println("Mercado se movimentou e o user " + a.getRecebedor() + " ganhou " + MercadoManager.valores.get(a.getActionName()));
                            TransferenceManager.createTransference("Mercado de Ações", a.getRecebedor(), MercadoManager.valores.get(a.getActionName()));
                        });
                    }
                }
            }, 0, 10000);
        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "O agendador não conseguiu iniciar, o mercado de ações está parado!");
        }
        loadingFrame.setPosition(50);
        new UserManager(sqlite);
        new TransferenceManager(sqlite);
        new MercadoManager(sqlite);
        loadingFrame.setPosition(75);
        try {
            UIManager.setLookAndFeel(new FlatMaterialDeepOceanContrastIJTheme());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        new LoginFrame();
        loadingFrame.dispose();
    }

    public static SQLiteInstance getSqlite() {
        return instance.sqlite;
    }

    public static ResponsiveScheduler getScheduler() {
        return instance.scheduler;
    }

    public static UiBooster getBooster(){
        return instance.booster;
    }

    public static boolean connected(){
        return instance.connected;
    }

    public static void createServer(int port){
        WaitingDialog dialog = getBooster().showWaitingDialog("Criando servidor...", "Please wait!");
        dialog.setMessage("");
        instance.server = new Server(port, new IDelegate() {
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
                }
                return null;
            }
        });
        instance.server.start();
        dialog.close();
        JOptionPane.showMessageDialog(null, "Agora informe aos seus amigos ou usuários o IP e porta a se conectar." +
                "\nSe vocês estiverem na mesma rede use o IP interno desta máquina." +
                "\nSe abrir outra aplicação nesta mesma máquina use o IP como localhost." +
                "\n\nO console apartir de agora será rajado de mensagens relacionadas a conexão.");
    }

    public static void connect(String ip, int port){
        WaitingDialog dialog = Main.getBooster().showWaitingDialog("Conectando...", "Please wait!");
        dialog.setLargeMessage("Preparando a conexão...");
        try {
            instance.client = new Client(ip, port);
            dialog.setLargeMessage("Preparando a conexão...\nConexão efetuada, preparando a Task de atualização...");
            getScheduler().repeatTask(new RSTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("PEDINDO ATUALIZACAO DE MERCADO");
                        MercadoManager.valores.clear();
                        JSONObject obj = (JSONObject) request("GETACOESVALORES", "VALORES DO MERCADO");
                        obj.keySet().forEach(e -> MercadoManager.valores.put(e, obj.getInt(e)));
                    } catch (Exception e){
                        e.printStackTrace();
                        setProblem(true);
                        setProblemID(10);
                    }
                }
            }, 0, 1000);
            dialog.setLargeMessage("Preparando a conexão...\nConexão efetuada, preparando a Task de atualização...\nFinalizado!");
        } catch ( Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        instance.connected = true;
        getScheduler().runTaskAfter(new RSTask() {
            @Override
            public void run() {
                dialog.close();
            }
        }, 5000);
    }

    public static Object request(String title, Object payload){
        return instance.client.request(title, payload);
    }
}