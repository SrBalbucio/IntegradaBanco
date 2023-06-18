package balbucio.banco.server;

import balbucio.banco.Main;
import balbucio.banco.listener.TaskListener;
import balbucio.banco.manager.CobrancaManager;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Cobranca;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.server.command.CommandManager;
import balbucio.banco.task.ImpostoTask;
import balbucio.banco.utils.NumberUtils;
import balbucio.responsivescheduler.RSTask;
import balbucio.responsivescheduler.ResponsiveScheduler;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import balbucio.sqlapi.sqlite.SqliteConfig;
import co.gongzh.procbridge.IDelegate;
import co.gongzh.procbridge.Server;
import com.google.gson.Gson;
import de.milchreis.uibooster.components.WaitingDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BancoServer {


    public static Server server;

    public static void createServer(){
        SqliteConfig sqliteConfig;
        SQLiteInstance sqlite;
        sqliteConfig = new SqliteConfig(new File("database.db"));
        sqliteConfig.setMaxRows(Integer.MAX_VALUE);
        sqliteConfig.createFile();
        sqlite = new SQLiteInstance(sqliteConfig);
        sqlite.createTable("users", "name VARCHAR(255), password VARCHAR(255), token VARCHAR(255), saldo BIGINT");
        sqlite.createTable("transferences", "pagante VARCHAR(255), recebedor VARCHAR(255), value BIGINT, time BIGINT");
        sqlite.createTable("acoes", "name VARCHAR(255), recebedor VARCHAR(255), token VARCHAR(255)");
        ResponsiveScheduler scheduler = new ResponsiveScheduler();
        scheduler.getEventManager().registerListener(new TaskListener());
        scheduler.repeatTask(new RSTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Reload do mercado");
                    MercadoManager.juros = NumberUtils.getRandomNumber(1, 50);
                    MercadoManager.valores.forEach((s, i) -> MercadoManager.valores.replace(s, NumberUtils.getRandomNumber(40, 150)));
                    MercadoManager.acoes.forEach(a -> {
                        System.out.println("Mercado se movimentou e o user " + a.getRecebedor() + " ganhou " + MercadoManager.valores.get(a.getActionName()));
                        TransferenceManager.createTransference("Mercado de Ações", a.getRecebedor(), MercadoManager.valores.get(a.getActionName()));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    setProblemID(2);
                    setProblem(true);
                }
            }
        }, 1000, 10000);
        scheduler.repeatTask(new ImpostoTask(), 0, 20000);
        new UserManager(sqlite);
        new TransferenceManager(sqlite);
        new MercadoManager(sqlite);
        server = new Server(25565, new BancoDelegate());
        server.start();
        CommandManager manager = new CommandManager();
        System.out.println("Porta selecionada: 25565");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            manager.execute(scanner.next());
        }
    }

    public static void switchPort(int port){
        server = new Server(port, new BancoDelegate());
        server.start();
        System.out.println("Porta alterada, peça para que os outros usuários reconectem!");
    }

    public static void createServerWithGUI(int port){
        WaitingDialog dialog = Main.getBooster().showWaitingDialog("Criando servidor...", "Please wait!");
        dialog.setMessage("");
        server = new Server(port, new BancoDelegate());
        server.start();
        dialog.close();
        JOptionPane.showMessageDialog(null, "Agora informe aos seus amigos ou usuários o IP e porta a se conectar." +
                "\nSe vocês estiverem na mesma rede use o IP interno desta máquina." +
                "\nSe abrir outra aplicação nesta mesma máquina use o IP como localhost." +
                "\n\nO console apartir de agora será rajado de mensagens relacionadas a conexão.");
    }

}
