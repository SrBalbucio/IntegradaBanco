package balbucio.banco.frame;

import balbucio.banco.model.User;
import com.sun.javafx.application.PlatformImpl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Scanner;

public class CardFrame extends JPanel {

    private Stage stage;
    private WebView browser;
    private JFXPanel jfxPanel;
    private JButton swingButton;
    private User user;

    public CardFrame(User user){
        this.user = user;
        jfxPanel = new JFXPanel();
        PlatformImpl.startup(new Runnable() {
            @Override
            public void run() {

                stage = new Stage();

                stage.setTitle("Hello Java FX");
                stage.setResizable(true);

                Group root = new Group();
                Scene scene = new Scene(root, jfxPanel.getWidth(), jfxPanel.getHeight());
                stage.setScene(scene);

                browser = new WebView();
                browser.getEngine().setJavaScriptEnabled(true);
                browser.getEngine().setUserDataDirectory(new File("browser"));
                Scanner scanner = new Scanner(this.getClass().getResourceAsStream("/html/card.html"));
                String content = new String();
                while(scanner.hasNext()){
                    content += "\n"+ scanner.next().replace("{name}", user.getName());
                }
                browser.getEngine().loadContent(content);
                root.getChildren().add(browser);
                browser.setMaxSize(jfxPanel.getWidth(), jfxPanel.getHeight());
                browser.setMinSize(jfxPanel.getWidth(), jfxPanel.getHeight());
                jfxPanel.setScene(scene);
            }
        });
        this.add(jfxPanel);
        this.setVisible(true);
    }
}
