package balbucio.banco.frame;

import balbucio.banco.Main;
import balbucio.banco.manager.CobrancaManager;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.Acoes;
import balbucio.banco.model.Cobranca;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.org.ejsl.component.EJSLButton;
import balbucio.org.ejsl.component.JImage;
import balbucio.org.ejsl.event.ClickListener;
import balbucio.org.ejsl.utils.ColorUtils;
import balbucio.org.ejsl.utils.ImageUtils;
import balbucio.responsivescheduler.RSTask;
import com.google.gson.Gson;
import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.components.WaitingDialog;
import javafx.scene.chart.ScatterChart;
import org.json.JSONArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuFrame extends JFrame {

    private User user;
    private List<Transference> transferences = new ArrayList<>();
    private List<Acoes> acoesUser = new ArrayList<>();
    private DefaultTableModel model = new DefaultTableModel();
    private DefaultTableModel transferenciasmodel = new DefaultTableModel();
    private JLabel saldoText;
    private JTable table;
    private JTable transferenciasTable;
    private SimpleDateFormat formate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public MenuFrame(User user){
        super("balbBank - "+user.getName());
        WaitingDialog dialog = Main.getBooster().showWaitingDialog("Por favor, aguarde enquanto carregamos todos os detalhes da sua conta!", "Carregando sua conta!");
        this.user = user;
        user.setSaldo(0l);
        transferences = TransferenceManager.getTransferences(user);
        acoesUser = MercadoManager.getAcoes(user);
        transferences.forEach(user::transference);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(1024, 640);
        this.setLayout(new BorderLayout());
        this.add(menuPanel(), BorderLayout.WEST);
        this.add(northPanel(), BorderLayout.NORTH);
        this.add(centerPanel(), BorderLayout.CENTER);
        Main.getScheduler().repeatTask(new RSTask() {
            @Override
            public void run() {
                try {
                    System.out.println("reset frame");
                    user.setSaldo(0l);
                    if (!Main.connected()) {
                        CobrancaManager.getCobrancas().getOrDefault(user.getToken(), new ArrayList<>()).forEach(c -> {
                            Main.getBooster().showConfirmDialog("O usuário " + UserManager.getInstance().getUserName(c.getPedinteToken()) + " quer que você pague $" + c.getValor() + " para ele, você deseja efetuar a transferência?", "Pagamento requisitado", () -> {
                                if (user.getSaldo() >= c.getValor()) {
                                    TransferenceManager.removeTransference(user, c.getDevedorToken(), c.getValor());
                                    CobrancaManager.getCobrancas().get(user.getToken()).remove(c);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Você não tem dinheiro para efetuar essa transação!");
                                }
                            }, () -> {
                                CobrancaManager.getCobrancas().get(user.getToken()).remove(c);
                            });

                        });
                    } else {
                        ((JSONArray) Main.request("GETCOBRANCAS", user.getToken())).forEach(e -> {
                            Cobranca c = new Gson().fromJson((String) e, Cobranca.class);
                            Main.getBooster().showConfirmDialog("O usuário " + UserManager.getInstance().getUserName(c.getPedinteToken()) + " quer que você pague $" + c.getValor() + " para ele, você deseja efetuar a transferência?", "Pagamento requisitado", () -> {
                                if (user.getSaldo() >= c.getValor()) {
                                    TransferenceManager.removeTransference(user, c.getDevedorToken(), c.getValor());
                                    Main.request("DELETECOBRANCA", c.getDevedorToken() + ";" + c.getToken());
                                } else {
                                    JOptionPane.showMessageDialog(null, "Você não tem dinheiro para efetuar essa transação!");
                                }
                            }, () -> {
                                Main.request("DELETECOBRANCA", c.getDevedorToken() + ";" + c.getToken());
                            });
                        });
                    }
                    transferences = TransferenceManager.getTransferences(user);
                    transferences.forEach(user::transference);
                    acoesUser = MercadoManager.getAcoes(user);

                    model = new DefaultTableModel();
                    transferenciasmodel = new DefaultTableModel();
                    model.addColumn("Pagador");
                    model.addColumn("Recebedor");
                    model.addColumn("Valor");
                    transferenciasmodel.addColumn("Pagador");
                    transferenciasmodel.addColumn("Recebedor");
                    transferenciasmodel.addColumn("Valor");
                    transferenciasmodel.addColumn("Data");
                    int limit = 0;
                    for (int i = transferences.size() - 1; i > 0; i--) {
                        Transference t = transferences.get(i);
                        if (limit <= 10) {
                            limit++;
                            model.addRow(new Object[]{UserManager.getInstance().getUserName(t.getTokenPagante()), UserManager.getInstance().getUserName(t.getTokenRecebedor()), t.getValue()});
                        }
                        transferenciasmodel.addRow(new Object[]{UserManager.getInstance().getUserName(t.getTokenPagante()), UserManager.getInstance().getUserName(t.getTokenRecebedor()), t.getValue(), formate.format(new Date(t.getTime()))});
                    }
                    table.setModel(model);
                    transferenciasTable.setModel(transferenciasmodel);
                    saldoText.setText("Saldo Atual: $" + user.getSaldo());
                } catch(Exception e) {
                    e.printStackTrace();
                    setProblem(true);
                    setProblemID(1);
                }
            }
        }, 1000, 5000);
        this.setVisible(true);
        dialog.close();
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
        saldoText = new JLabel("Saldo Atual: $"+user.getSaldo());
        saldoText.setFont(label.getFont().deriveFont(18f));
        info.add(saldoText);
        n.add(info);
        return n;
    }

    private CardLayout menu = new CardLayout();
    private JPanel center;

    public JPanel centerPanel(){
        center = new JPanel();
        center.setLayout(menu);
        center.add(homePanel(), "HOME");
        center.add(transferenciaPanel(), "TRANS");
        center.add(sobrePanel(), "SOBRE");
        center.add(new CardFrame(user), "CARD");
        menu.show(center, "HOME");
        return center;
    }

    public JPanel sobrePanel(){
        Color color = ColorUtils.hexToRgb("#0f101b");
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        JPanel text = new JPanel();
        text.setBackground(Color.white);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JImage image = new JImage(ImageUtils.getImage("https://logosmarcas.net/wp-content/uploads/2020/12/GitHub-Logo-650x366.png"));
        image.setPreferredSize(new Dimension(128, 128));
        image.setMaxSize(true);
        image.setCenter(true);
        image.setBackground(Color.white);
        JLabel label = new JLabel("Feito pelo SrBalbucio");
        label.setFont(label.getFont().deriveFont(16l));
        JLabel version = new JLabel("Versão 1.0 - License GPL V3");
        version.setFont(label.getFont().deriveFont(14l));
        text.add(image);
        text.add(label);
        text.add(version);
        panel.add(text);
        return panel;
    }

    public JScrollPane transferenciaPanel(){
        JScrollPane panel;
        this.transferenciasTable = new JTable(transferenciasmodel);
        transferenciasTable.setFillsViewportHeight(true);
        panel = new JScrollPane(transferenciasTable);
        panel.setVisible(true);
        return panel;
    }

    public JPanel homePanel(){
        JPanel home = new JPanel(new BorderLayout());
        JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton enviar = new JButton("Enviar dinheiro");

        enviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = Main.getBooster().showTextInputDialog("Qual é o usuário que deseja enviar dinheiro?");
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto você deseja enviar?"));
                User target = UserManager.getInstance().getUserByName(userName);
                if(target == null){
                    JOptionPane.showMessageDialog(null, "Esse usuário não existe!", "Usuário Não Encontrado", JOptionPane.ERROR_MESSAGE);
                } else{
                    WaitingDialog dialog = Main.getBooster().showWaitingDialog("Enviando Dinheiro!", "Por favor, aguarde!");
                    TransferenceManager.removeTransference(user, target.getToken(), valor);
                    dialog.close();
                }
            }
        });

        JButton receber = new JButton("Receber dinheiro");

        receber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = Main.getBooster().showTextInputDialog("Para enviar um pedido de pagamento é necessário ser um servidor ou estar conectado a um.\n\nQual é o nome do usuário a ser cobrado?");
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Qual será o valor a ser cobrado?"));
                User co = UserManager.getInstance().getUserByName(userName);
                if(co != null) {
                    CobrancaManager.addCobranca(user, co, valor);
                    JOptionPane.showMessageDialog(null, "A cobrança foi enviada, porém ela dura apenas enquanto o servidor estiver online.", "Aguardando...", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Esse usuário não existe!", "Usuário Não Encontrado", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton comprar = new JButton("Comprar Ações");
        comprar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] acoes = new String[MercadoManager.valores.size()];

                AtomicInteger i = new AtomicInteger();
                MercadoManager.valores.forEach((a, v) -> {
                    acoes[i.getAndIncrement()] = a + "($"+v+")";
                });

                List<String> selected = Main.getBooster().showMultipleSelection(
                        "Quais ações você deseja comprar?\n\nInstruções: Selecione as ações que deseja comprar (e que você tem dinheiro pra pagar) e aguarde o processamento ser efetuado!\n",
                        "Ações Disponíveis", acoes);

                for(String a : selected){
                    MercadoManager.valores.keySet().stream().filter(a::contains).findFirst().ifPresent(name -> {
                        int valor = MercadoManager.valores.get(name);
                        if(user.getSaldo() >= valor) {
                            new Acoes(name, user.getToken());
                            TransferenceManager.removeTransference(user, "Mercado de Ações", valor);
                        } else{
                            JOptionPane.showMessageDialog(null, "Você não tem dinheiro para comprar a ação "+name);
                        }
                    });
                }
            }
        });
        JButton vender = new JButton("Vender Ações");

        vender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] acoes = new String[acoesUser.size()];
                AtomicInteger i = new AtomicInteger();

                acoesUser.forEach(a -> {
                    acoes[i.getAndIncrement()] = a.getActionName()+" ($"+MercadoManager.valores.get(a.getActionName())+")";
                });

                List<String> selected = Main.getBooster().showMultipleSelection(
                        "Quais ações você deseja vender?",
                        "Ações Disponíveis", acoes);

                for(String a : selected){
                    acoesUser.stream().filter(ac -> a.contains(ac.getActionName())).findFirst().ifPresent(ac -> ac.delete());
                }
            }
        });
        button.add(enviar);
        button.add(receber);
        button.add(comprar);
        button.add(vender);
        home.add(button, BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel resumoLabel = new JLabel("Resumo desta semana:");
        resumoLabel.setFont(resumoLabel.getFont().deriveFont(16f));
        panel.add(resumoLabel);
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setLayout(new FlowLayout(FlowLayout.CENTER));
        table.setFillsViewportHeight(true);
        table.setFont(table.getFont().deriveFont(14l));
        table.setBorder(new EmptyBorder(20, 0, 0, 0));
        panel.add(table);
        JScrollPane pane = new JScrollPane(panel);
        home.add(pane, BorderLayout.CENTER);
        return home;
    }

    private MercadoDeAcoesFrame mercadoDeAcoesFrame = null;

    public JPanel menuPanel(){
        JPanel menu = new JPanel();
        Color color = ColorUtils.hexToRgb("#171826");
        menu.setBackground(color);
        BoxLayout layout = new BoxLayout(menu,BoxLayout.Y_AXIS);
        menu.setLayout(layout);
        //JLabel img = new JLabel(new ImageIcon(ImageUtils.getImage(this.getClass().getResourceAsStream("/images/logo.png"))));
        EJSLButton home = new EJSLButton("Home");
        home.setSelected(true);
        home.setPrimaryColor(color);
        menu.add(home);
        EJSLButton mercado = new EJSLButton("Mercado de Ações");
        mercado.setPrimaryColor(color);
        menu.add(mercado);
        EJSLButton transferencias = new EJSLButton("Transferências");
        transferencias.setPrimaryColor(color);
        transferencias.setAnimation(true);
        menu.add(transferencias);
        EJSLButton card = new EJSLButton("Cartão");
        card.setPrimaryColor(color);
        menu.add(card);
        EJSLButton sobre = new EJSLButton("Sobre");
        sobre.setPrimaryColor(color);
        menu.add(sobre);
        EJSLButton sair = new EJSLButton("Sair");

        sair.setPrimaryColor(color);
        menu.add(sair);

        home.getClickListeners().add(e -> {
            this.menu.show(center, "HOME");
            home.setSelected(true);
            mercado.setSelected(false);
            transferencias.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });

        mercado.getClickListeners().add(e -> {
            e.setSelected(false);
            if(mercadoDeAcoesFrame != null && mercadoDeAcoesFrame.isActive()){
                mercadoDeAcoesFrame.dispose();
            }
            mercadoDeAcoesFrame = new MercadoDeAcoesFrame();
        });

        transferencias.getClickListeners().add(e -> {
            this.menu.show(center, "TRANS");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(true);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });
        card.getClickListeners().add(e -> {
            String password = Main.getBooster().showPasswordDialog("Importante: Se seu cartão não aparecer tente trocar o seu JDK pelo Amazon Corretto 20.\nPor favor, confirme sua senha antes de ver seu cartão:", "Confirme sua senha");
            if(password.equalsIgnoreCase(user.getPassword())) {
                this.menu.show(center, "CARD");
                home.setSelected(false);
                mercado.setSelected(false);
                transferencias.setSelected(false);
                card.setSelected(true);
                sobre.setSelected(false);
                revalidateAll();
            } else{
                JOptionPane.showMessageDialog(null, "Você errou sua senha, por isso iremos te desconectar do app.");
                this.dispose();
            }
        });
        sobre.getClickListeners().add(e -> {
            this.menu.show(center, "SOBRE");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(true);
            revalidateAll();
        });
        sair.getClickListeners().add(e -> {
            dispose();
            new LoginFrame();
        });
        return menu;
    }

    public void revalidateAll(){
        Arrays.stream(this.getComponents()).toList().forEach(c -> {
            revalidate(c);
        });
    }

    public void revalidate(Component c){
        if(c instanceof JPanel p){
            Arrays.stream(p.getComponents()).toList().forEach(cp -> revalidate(cp));
        }
        c.revalidate();
        c.repaint();
    }
}
