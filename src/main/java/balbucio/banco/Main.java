package balbucio.banco;

import balbucio.banco.frame.LoginFrame;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.org.ejsl.frame.JLoadingFrame;
import balbucio.org.ejsl.utils.ImageUtils;
import balbucio.responsivescheduler.ResponsiveScheduler;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import balbucio.sqlapi.sqlite.SqliteConfig;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanContrastIJTheme;
import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    private static SQLiteInstance sqlite;
    private static SqliteConfig sqliteConfig;
    private static ResponsiveScheduler scheduler;
    public Main(){
        JLoadingFrame loadingFrame = new JLoadingFrame("Loading", ImageUtils.getImage("https://img.freepik.com/free-vector/bank-building-with-cityscape_1284-52265.jpg?w=2000"), 100);
        sqliteConfig = new SqliteConfig(new File("database.db"));
        sqliteConfig.createFile();
        sqlite = new SQLiteInstance(sqliteConfig);
        sqlite.createTable("users", "name VARCHAR(255), password VARCHAR(255), token VARCHAR(255), saldo BIGINT");
        sqlite.createTable("transferences", "pagante VARCHAR(255), recebedor VARCHAR(255), value BIGINT, time BIGINT");
        sqlite.createTable("acoes", "name VARCHAR(255), recebedor VARCHAR(255), token VARCHAR(255)");
        loadingFrame.setPosition(25);
        new UserManager(sqlite);
        new TransferenceManager(sqlite);
        new MercadoManager(sqlite);
        loadingFrame.setPosition(50);
        scheduler = new ResponsiveScheduler();
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
}