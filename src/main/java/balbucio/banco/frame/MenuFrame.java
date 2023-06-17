package balbucio.banco.frame;

import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.utils.DropShadowBorder;
import balbucio.org.ejsl.component.EJSLButton;
import balbucio.org.ejsl.component.JImage;
import balbucio.org.ejsl.component.panel.JCornerPanel;
import balbucio.org.ejsl.utils.ColorUtils;
import balbucio.org.ejsl.utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MenuFrame extends JFrame {

    private User user;
    private List<Transference> transferences = new ArrayList<>();

    public MenuFrame(User user){
        this.user = user;
        user.setSaldo(0l);
        transferences = TransferenceManager.getTransferences(user);
        transferences.forEach(t -> user.transference(t));
        this.setSize(640, 480);
        this.setLayout(new BorderLayout());
        this.setVisible(true);
        this.add(menuPanel(), BorderLayout.WEST);
        this.add(northPanel(), BorderLayout.NORTH);
        this.add(centerPanel(), BorderLayout.CENTER);
    }

    public JPanel northPanel(){
        Color color = ColorUtils.hexToRgb("#171826");
        JPanel n = new JPanel(new FlowLayout(FlowLayout.LEFT));
        n.setBackground(color);
        n.setBorder(new EmptyBorder(10, 10, 10, 10));

        JImage image = new JImage(ImageUtils.getImage(this.getClass().getResourceAsStream("/images/logo.png")));
        image.setPreferredSize(new Dimension(64, 64));
        image.setBackground(color);
        image.setMaxSize(true);
        image.setCenter(true);
        n.add(image);


        JPanel info = new JPanel();
        info.setBackground(color);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Olá, "+user.getName());
        label.setFont(label.getFont().deriveFont(16f));
        info.add(label);
        JLabel saldoText = new JLabel("Saldo Atual: $"+user.getSaldo());
        saldoText.setFont(label.getFont().deriveFont(18f));
        info.add(saldoText);
        n.add(info);
        return n;
    }

    private CardLayout menu = new CardLayout();

    public JPanel centerPanel(){
        JPanel center = new JPanel();
        center.setLayout(menu);
        center.add(homePanel(), "HOME");
        menu.show(center, "HOME");
        return center;
    }

    public JPanel homePanel(){
        JPanel home = new JPanel();
        JPanel button = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton enviar = new JButton("Enviar dinheiro");
        JButton receber = new JButton("Receber dinheiro");
        JButton comprar = new JButton("Comprar Ações");
        JButton vender = new JButton("Vender Ações");
        button.add(enviar);
        button.add(receber);
        button.add(comprar);
        button.add(vender);
        home.add(button);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel resumoLabel = new JLabel("Resumo desta semana:");
        resumoLabel.setFont(resumoLabel.getFont().deriveFont(16f));
        panel.add(resumoLabel);
        DefaultTableModel models = new DefaultTableModel();
        models.addColumn("Pagador");
        models.addColumn("Recebedor");
        models.addColumn("Valor");
        int i = 10;
        for(Transference t : transferences){
            if(i == 0){
                break;
            }
            i--;
            models.addRow(new Object[] {UserManager.getInstance().getUserName(t.getTokenPagante()), UserManager.getInstance().getUserName(t.getTokenRecebedor()), t.getValue()});
        }
        JTable table = new JTable(models);
        table.setFont(table.getFont().deriveFont(14l));
        table.setBorder(new EmptyBorder(20, 0, 0, 0));
        panel.add(table);
        home.add(panel);
        return home;
    }

    public JPanel menuPanel(){
        JPanel menu = new JPanel();
        Color color = ColorUtils.hexToRgb("#171826");
        menu.setBackground(color);
        BoxLayout layout = new BoxLayout(menu,BoxLayout.Y_AXIS);
        menu.setLayout(layout);
        //JLabel img = new JLabel(new ImageIcon(ImageUtils.getImage(this.getClass().getResourceAsStream("/images/logo.png"))));
        EJSLButton home = new EJSLButton("Home");
        home.setPrimaryColor(color);
        menu.add(home);
        EJSLButton mercado = new EJSLButton("Mercado");
        mercado.setPrimaryColor(color);
        menu.add(mercado);
        EJSLButton transferencias = new EJSLButton("Transferências");
        transferencias.setPrimaryColor(color);
        menu.add(transferencias);
        EJSLButton sobre = new EJSLButton("Sobre");
        sobre.setPrimaryColor(color);
        menu.add(sobre);
        return menu;
    }
}
