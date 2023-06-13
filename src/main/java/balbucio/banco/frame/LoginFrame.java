package balbucio.banco.frame;

import balbucio.banco.manager.UserManager;
import balbucio.banco.model.User;
import balbucio.org.ejsl.component.JImage;
import balbucio.org.ejsl.component.panel.JImagePanel;
import balbucio.org.ejsl.utils.ColorUtils;
import balbucio.org.ejsl.utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    public LoginFrame(){
        super("Login - balbBank");
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
            User user = UserManager.getInstance().createUser(username.getText(), password.getText());
            JOptionPane.showMessageDialog(this, "Seja bem-vindo a sua nova conta balbBank, "+user.getName()+"!");
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
           }
        });
        btns.add(register);
        btns.add(login);
        panel.add(up);
        panel.add(pp);
        panel.add(btns);
        return panel;
    }
}
