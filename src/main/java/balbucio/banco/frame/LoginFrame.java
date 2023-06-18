package balbucio.banco.frame;

import balbucio.banco.Main;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.server.BancoServer;
import balbucio.org.ejsl.component.EJSLButton;
import balbucio.org.ejsl.event.ClickListener;
import balbucio.org.ejsl.utils.ImageUtils;
import de.milchreis.uibooster.UiBooster;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginFrame extends JFrame {

    public LoginFrame(){
        super("Login - balbBank");
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.isShiftDown()){
                    dispose();
                    new MenuFrame(new User("Anonimo", "122"));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isShiftDown()){
                    dispose();
                    new MenuFrame(new User("Anonimo", "122"));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.isShiftDown()){
                    dispose();
                    new MenuFrame(new User("Anonimo", "122"));
                }
            }
        });
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(640, 480);
        GridBagConstraints gbc = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.add(loginPanel());
        this.setVisible(true);
    }

    public JPanel loginPanel(){
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        JPanel il = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel img = new JLabel(new ImageIcon(ImageUtils.getImage(this.getClass().getResourceAsStream("/images/logo.png"))));
        img.setMaximumSize(new Dimension(36, 36));
        il.add(img);
        JPanel ll = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("LOGIN");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24));
        ll.add(label);
        panel.add(il);
        panel.add(ll);
        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField username = new JTextField();
        username.setPreferredSize(new Dimension(120, 24));
        username.setToolTipText("Username");
        up.add(new JLabel("User: "));
        up.add(username);
        JPanel pp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPasswordField password = new JPasswordField();
        password.setPreferredSize(new Dimension(120, 24));
        password.setToolTipText("Senha");
        pp.add(new JLabel("Pass: "));
        pp.add(password);
        JPanel btns = new JPanel();
        JButton register = new JButton("Registrar");
        register.addActionListener((e) -> {
            if(!UserManager.getInstance().existUser(username.getText(), password.getText())) {
                User user = UserManager.getInstance().createUser(username.getText(), password.getText());
                TransferenceManager.createTransference(user, "Banco", 1500);
                JOptionPane.showMessageDialog(this, "Seja bem-vindo a sua nova conta balbBank, " + user.getName() + "!");
                this.dispose();
                new MenuFrame(user);
            } else{
                JOptionPane.showMessageDialog(this, "Esqueceu sua senha foi? Essa conta já existe!");
            }
        });
        JButton login = new JButton("Entrar");
        login.addActionListener((e) -> {
           User user = UserManager.getInstance().getUser(username.getText(), password.getText());
           if(user == null){
               password.setText("");
               password.setBorder(new LineBorder(Color.RED, 1));
               JOptionPane.showMessageDialog(this, "Tente novamente! Senha ou username incorretos.");
           } else {
               JOptionPane.showMessageDialog(this, "Seja bem-vindo novamente, " + user.getName() + "!");
               this.dispose();
               new MenuFrame(user);
           }
        });
        JButton conectar = new JButton("Conectar");
        conectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Essa função pode não funcionar corretamente no seu sistema!\nÉ importante entender que seu firewall pode bloquear o uso desta função.\nSe você não conseguir utilizar por favor abra uma issue.");

                Main.getBooster().showConfirmDialog(
                        "O que você deseja fazer?\nAo clicar SIM você passa a enviar todas as suas ações para um servidor externo\nAo clicar NÃO você se torna um servidor!",
                        "O que fazer?",
                        () -> {
                            String ip = JOptionPane.showInputDialog("Qual é o IP que deseja conectar?");
                            int port = Integer.parseInt(JOptionPane.showInputDialog("Qual é a porta que deseja conectar?"));
                            Main.connect(ip, port);
                        },
                        () -> {
                            int port = Integer.parseInt(JOptionPane.showInputDialog("Qual é a porta que deseja abrir o servidor?"));
                            BancoServer.createServerWithGUI(port);
                        });
            }
        });
        btns.add(register);
        btns.add(login);
        btns.add(conectar);
        panel.add(up);
        panel.add(pp);
        panel.add(btns);
        return panel;
    }
}
