package balbucio.banco;

import balbucio.banco.frame.LoginFrame;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.org.ejsl.frame.JLoadingFrame;
import balbucio.org.ejsl.utils.ImageUtils;
import balbucio.responsivescheduler.ResponsiveScheduler;
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

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    private static SQLiteInstance sqlite;
    private static SqliteConfig sqliteConfig;
    private static ResponsiveScheduler scheduler;
    public static Server server;
    public static Client client;
    public static boolean connected = false;
    public Main(){
        JLoadingFrame loadingFrame = new JLoadingFrame("Loading", ImageUtils.getImage("https://img.freepik.com/free-vector/bank-building-with-cityscape_1284-52265.jpg?w=2000"), 100);
        sqliteConfig = new SqliteConfig(new File("database.db"));
        sqliteConfig.createFile();
        sqlite = new SQLiteInstance(sqliteConfig);
        sqlite.createTable("users", "name VARCHAR(255), password VARCHAR(255), token VARCHAR(255), saldo BIGINT");
        sqlite.createTable("transferences", "pagante VARCHAR(255), recebedor VARCHAR(255), value BIGINT, time BIGINT");
        sqlite.createTable("acoes", "name VARCHAR(255), recebedor VARCHAR(255), token VARCHAR(255)");
        loadingFrame.setPosition(25);
        try {
            scheduler = new ResponsiveScheduler();
        } catch(Exception e){
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
        return sqlite;
    }

    public static ResponsiveScheduler getScheduler() {
        return scheduler;
    }

    public static void createServer(int port){
        WaitingDialog dialog = new UiBooster().showWaitingDialog("Criando servidor...", "Please wait!");
        dialog.setMessage("");
        server = new Server(port, new IDelegate() {
            @Override
            public @Nullable Object handleRequest(@Nullable String s, @Nullable Object o) {
                System.out.println(o);
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
                }
                return null;
            }
        });
        server.start();
        dialog.close();
        JOptionPane.showMessageDialog(null, "Agora informe aos seus amigos ou usuários o IP e porta a se conectar.\nSe vocês estiverem na mesma rede use o IP como localhost!");
    }

    public static void connect(String ip, int port){
        WaitingDialog dialog = new UiBooster().showWaitingDialog("Conectando...", "Please wait!");
        try {
            client = new Client(ip, port);
        } catch ( Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        connected = true;
        dialog.close();
    }

    public static Object request(String title, Object payload){
        return client.request(title, payload);
    }
}