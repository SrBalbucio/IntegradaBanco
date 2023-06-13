package balbucio.banco.frame;

import balbucio.banco.model.User;
import balbucio.org.ejsl.utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MenuFrame extends JFrame {

    private User user;
    public MenuFrame(User user){
        this.user = user;
        this.setLayout(new BorderLayout());
        this.setVisible(true);
    }

    public JPanel menuPanel(){
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel,BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        JLabel img = new JLabel(new ImageIcon(ImageUtils.getImage(this.getClass().getResourceAsStream("/images/logo.png"))));
        panel.add(img);
        return panel;
    }
}
