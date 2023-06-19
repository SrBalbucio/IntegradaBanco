package balbucio.banco;

import balbucio.banco.frame.LoginFrame;
import balbucio.banco.listener.TaskListener;
import balbucio.banco.manager.*;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Cobranca;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.server.BancoServer;
import balbucio.banco.task.EmprestimoTask;
import balbucio.banco.task.ImpostoTask;
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
import com.zaxxer.hikari.util.SuspendResumeLock;
import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.components.WaitingDialog;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        if(hasArg(args, "--server")){
            BancoServer.createServer();
        } else {
            new Main();
        }
    }

    public static boolean hasArg(String[] args, String arg){
        for(String s : args){
            if(s.equalsIgnoreCase(arg)){
                return true;
            }
        }
        return false;
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
        sqliteConfig.setMaxRows(Integer.MAX_VALUE);
        sqliteConfig.createFile();
        sqlite = new SQLiteInstance(sqliteConfig);
        sqlite.createTable("users", "name VARCHAR(255), password VARCHAR(255), token VARCHAR(255), saldo BIGINT");
        sqlite.createTable("transferences", "pagante VARCHAR(255), recebedor VARCHAR(255), value BIGINT, time BIGINT");
        sqlite.createTable("acoes", "name VARCHAR(255), recebedor VARCHAR(255), token VARCHAR(255)");
        sqlite.createTable("emprestimos", "devedor VARCHAR(255), value BIGINT, time BIGINT, token VARCHAR(255)");
        loadingFrame.setPosition(25);
        booster = new UiBooster();
        loadingFrame.setPosition(30);
        try {
            scheduler = new ResponsiveScheduler();
            scheduler.getEventManager().registerListener(new TaskListener());
            scheduler.repeatTask(new RSTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("Reload do mercado");
                        if (!Main.connected()) {
                            MercadoManager.juros = NumberUtils.getRandomNumber(20, 90);
                            MercadoManager.jurosHistory.put(new Date().getTime(), MercadoManager.juros);
                            MercadoManager.valores.forEach((s, i) -> MercadoManager.valores.replace(s, NumberUtils.getRandomNumber(-300, 500)));
                            MercadoManager.acoes.forEach(a -> {
                                System.out.println("Mercado se movimentou e o user " + a.getRecebedor() + " ganhou " + MercadoManager.valores.get(a.getActionName()));
                                TransferenceManager.createTransference("Mercado de Ações ("+a.getActionName()+")", a.getRecebedor(), MercadoManager.valores.get(a.getActionName()));
                            });
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        setProblemID(2);
                        setProblem(true);
                    }
                }
            }, 0, 10000);
            scheduler.repeatTask(new ImpostoTask(), 0, 20000);
            scheduler.repeatTask(new EmprestimoTask(), 0, 1000);
        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "O agendador não conseguiu iniciar, o mercado de ações está parado!");
        }
        loadingFrame.setPosition(50);
        new UserManager(sqlite);
        new TransferenceManager(sqlite);
        new MercadoManager(sqlite);
        new EmprestimoManager();
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
                        MercadoManager.jurosHistory.clear();
                        JSONObject oobj = (JSONObject) request("GETJUROSHISTORY", "VALORES DOS JUROS");
                        System.out.println(oobj.toString());
                        oobj.keySet().forEach(e -> {
                            if(oobj.has(e)) {
                                MercadoManager.jurosHistory.put(Long.parseLong(e), oobj.getLong(e));
                            }
                        });
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
        if(instance == null){
            System.out.println("Ele é null wtf?????");
        }
        if(instance.client == null){
            System.out.println("Cliente é null wtf?????");
        }
        return instance.client.request(title, payload);
    }
}