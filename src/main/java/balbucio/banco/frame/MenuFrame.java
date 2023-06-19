package balbucio.banco.frame;

import balbucio.banco.Main;
import balbucio.banco.manager.*;
import balbucio.banco.model.*;
import balbucio.banco.task.RandomEventTask;
import balbucio.banco.utils.NumberUtils;
import balbucio.org.ejsl.component.EJSLButton;
import balbucio.org.ejsl.component.JImage;
import balbucio.org.ejsl.utils.ColorUtils;
import balbucio.org.ejsl.utils.ImageUtils;
import balbucio.responsivescheduler.RSTask;
import com.google.gson.Gson;
import de.milchreis.uibooster.components.WaitingDialog;
import de.milchreis.uibooster.model.ListElement;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
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
    private long cofreValue;
    private long cofreJuros;
    private List<Transference> transferences = new ArrayList<>();
    private Map<Long, Long> saldo = new HashMap<>();
    private List<Acoes> acoesUser = new ArrayList<>();
    private DefaultTableModel model = new DefaultTableModel();
    private DefaultTableModel transferenciasmodel = new DefaultTableModel();
    private JLabel saldoText;
    private JLabel jurosLabel;
    private JLabel cofreLabel;
    private JLabel cofreJurosLabel;
    private JLabel cofreSaldoLabel;
    private JTable table;
    private JTable transferenciasTable;
    private JTable emprestimosTable;
    private ChartPanel chartpanel;
    private ChartPanel lucrochart;
    private SimpleDateFormat formate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public MenuFrame(User user) {
        super("balbBank - " + user.getName());
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

                    transferences = TransferenceManager.getTransferences(user);
                    transferences.forEach(user::transference);
                    acoesUser = MercadoManager.getAcoes(user);

                    if (!Main.connected()) {
                        CobrancaManager.getCobrancas().getOrDefault(user.getToken(), new ArrayList<>()).forEach(c -> {
                            Main.getBooster().createNotification("O usuário " + UserManager.getInstance().getUserName(c.getPedinteToken()) + " está te cobrando!", "Nova Cobrança!");
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

                    saldo.put(new Date().getTime(), user.getSaldo());

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
                    jurosLabel.setText("O juros atual está em: %" + MercadoManager.getJuros());
                    DefaultCategoryDataset ldata = new DefaultCategoryDataset();
                    List<Map.Entry<Long, Long>> svlues = saldo.entrySet().stream().toList();
                    limit = 0;
                    for (int i = svlues.size() - 1; i > 0; i--) {
                        if (limit == 10) {
                            break;
                        }
                        limit++;
                        ldata.addValue(svlues.get(i).getValue(), "saldo", formate.format(new Date(svlues.get(i).getKey())));
                    }

                    JFreeChart lucro = ChartFactory.createLineChart(
                            "Histórico do seu saldo", "Data",
                            "Valor",
                            ldata, PlotOrientation.VERTICAL,
                            true, true, false);
                    lucrochart.setChart(lucro);
                    DefaultCategoryDataset data = new DefaultCategoryDataset();
                    List<Map.Entry<Long, Long>> vlues = MercadoManager.getJurosHistory().entrySet().stream().toList();
                    limit = 0;
                    for (int i = vlues.size() - 1; i > 0; i--) {
                        if (limit == 10) {
                            break;
                        }
                        limit++;
                        data.addValue(vlues.get(i).getValue(), "juros", formate.format(new Date(vlues.get(i).getKey())));
                    }
                    JFreeChart jurosChart = ChartFactory.createLineChart(
                            "Histórico dos Juros", "Data",
                            "Valor",
                            data, PlotOrientation.VERTICAL,
                            true, true, false);
                    chartpanel.setChart(jurosChart);

                    DefaultTableModel emmodel = new DefaultTableModel();
                    emmodel.addColumn("Valor");
                    emmodel.addColumn("Vencimento");
                    for (Emprestimo e : EmprestimoManager.getEmprestimos(user)) {
                        emmodel.addRow(new Object[]{e.getValor(), formate.format(new Date(e.getMaxTime()))});
                    }
                    emprestimosTable.setModel(emmodel);
                    AtomicInteger cv = new AtomicInteger();
                    CofreManager.getTransferences(user).forEach(t -> {
                        cv.addAndGet(t.getValor());
                    });
                    cofreJuros = MercadoManager.getJuros() * 2;
                    cofreLabel.setText("O saldo do seu cofrinho é $" + cv);
                    cofreJurosLabel.setText("O cofre está redendendo %" + cofreJuros);
                    cofreSaldoLabel.setText("Você tem ao total (cofre + saldo) $" + (cv.get() + user.getSaldo()));
                } catch (Exception e) {
                    e.printStackTrace();
                    setProblem(true);
                    setProblemID(1);
                }
            }
        }, 1000, 5000);
        Main.getScheduler().repeatTask(new RSTask() {
            @Override
            public void run() {
                try {
                    cofreValue = 0;
                    cofreJuros = MercadoManager.getJuros() * 2;
                    CofreManager.getTransferences(user).forEach(t -> {
                        cofreValue += t.getValor();
                    });
                    CofreManager.createTransference(user, Math.toIntExact(cofreValue % cofreJuros));
                    System.out.println("O user " + user.getToken() + " vai receber " + Math.toIntExact(cofreValue % cofreJuros) + " por causa do cofre!");
                } catch (Exception e) {
                    e.printStackTrace();
                    setProblem(true);
                    setProblemID(8);
                }
            }
        }, 1000, 30000);
        Main.getScheduler().repeatTask(new RandomEventTask(user), 1000, 1000*60*5);
        this.setVisible(true);
        dialog.close();
    }

    public JPanel northPanel() {
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
        JLabel label = new JLabel("Olá, " + user.getName());
        label.setFont(label.getFont().deriveFont(16f));
        info.add(label);
        saldoText = new JLabel("Saldo Atual: $" + user.getSaldo());
        saldoText.setFont(label.getFont().deriveFont(18f));
        info.add(saldoText);
        n.add(info);
        return n;
    }

    private CardLayout menu = new CardLayout();
    private JPanel center;

    public JPanel centerPanel() {
        center = new JPanel();
        center.setLayout(menu);
        center.add(homePanel(), "HOME");
        center.add(transferenciaPanel(), "TRANS");
        center.add(sobrePanel(), "SOBRE");
        center.add(new CardFrame(user), "CARD");
        center.add(emprestimosPanel(), "EMP");
        center.add(cofrePanel(), "COFRE");
        menu.show(center, "HOME");
        return center;
    }

    public JPanel sobrePanel() {
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

    public JScrollPane transferenciaPanel() {
        JScrollPane panel;
        this.transferenciasTable = new JTable(transferenciasmodel);
        transferenciasTable.setFillsViewportHeight(true);
        panel = new JScrollPane(transferenciasTable);
        panel.setVisible(true);
        return panel;
    }

    public JPanel emprestimosPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addEmp = new JButton("Pegar Emprestimo");
        addEmp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto dinheiro você quer pegar neste emprestimo?"));
                boolean confirm = Main.getBooster().showConfirmDialog("Você aceita pegar $" + valor + " e pagar esse valor mais 15% do juros atual?", "Confirme o emprestimo");
                if (confirm) {
                    TransferenceManager.createTransference(user, "Banco (Emprestimo)", valor);
                    EmprestimoManager.createEmprestimo(user, valor);
                    JOptionPane.showMessageDialog(null, "Você pegou um emprestimo!\nSe você não pagar ele a tempo você terá que pagar o valor do empréstimo + o juros atual.");
                }
            }
        });
        JButton pagarEmp = new JButton("Pagar Emprestimo");

        pagarEmp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Emprestimo> em = EmprestimoManager.getEmprestimos(user);
                String[] ab = new String[em.size()];

                for (int i = 0; i < em.size(); i++) {
                    ab[i] = "$" + em.get(i).getValor() + " / " + em.get(i).getToken();
                }

                List<String> list = Main.getBooster().showMultipleSelection("Quais emprestimos você deseja pagar agora?", "Emprestimos a pagar", true, ab);

                for (String emp : list) {
                    String[] cre = emp.split(" / ");
                    em.stream().filter(emc -> emc.getToken().equalsIgnoreCase(cre[1])).findFirst().ifPresent(emc -> {
                        if (user.getSaldo() >= emc.getValor()) {
                            TransferenceManager.removeTransference(user, "Banco (Emprestimo)", emc.getValor());
                        } else {
                            JOptionPane.showMessageDialog(null, "Você não tem dinheiro suficiente para pagar este empréstimo.");
                        }

                    });
                    EmprestimoManager.deleteEmprestimo(cre[1]);
                }
            }
        });
        buttons.add(addEmp);
        buttons.add(pagarEmp);
        JScrollPane panel;
        DefaultTableModel model = new DefaultTableModel();
        this.emprestimosTable = new JTable(model);
        emprestimosTable.setFillsViewportHeight(true);
        panel = new JScrollPane(emprestimosTable);
        panel.setVisible(true);
        p.add(buttons);
        p.add(panel);
        return p;
    }

    public JPanel cofrePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton add = new JButton("Adicionar dinheiro");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto você deseja por no cofre?\nAo colocar o seu dinheiro no cofre ele passa a render quando você está ativo."));
                if (user.getSaldo() >= valor) {
                    TransferenceManager.removeTransference(user, "Cofre", valor);
                    CofreManager.createTransference(user, valor);
                } else {
                    JOptionPane.showMessageDialog(null, "Você não tem esse dinheiro para por no cofre!");
                }
            }
        });

        JButton pegar = new JButton("Pegar dinheiro");
        pegar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto você deseja pegar do cofre?\nÉ importante alertar que ao tirar o seu dinheiro do cofre ele para de render."));
                if (cofreValue >= valor) {
                    CofreManager.pegaDinheiro(user, valor);
                    TransferenceManager.createTransference(user, "Cofre", valor);
                } else {
                    JOptionPane.showMessageDialog(null, "Você não tem esse dinheiro no cofre!");
                }
            }
        });
        buttons.add(add);
        buttons.add(pegar);
        cofreLabel = new JLabel("O saldo do seu cofrinho é $");
        cofreLabel.setFont(cofreLabel.getFont().deriveFont(18l));
        cofreSaldoLabel = new JLabel("Você tem ao total (cofre + saldo) $");
        cofreSaldoLabel.setFont(cofreLabel.getFont().deriveFont(18l));
        cofreJurosLabel = new JLabel("O cofre está redendendo %");
        cofreJurosLabel.setFont(cofreLabel.getFont().deriveFont(16l));
        JLabel info = new JLabel("Importante: O Cofre é separado dos outros sistemas, você precisa estar online para que seu cofre renda.");
        p.add(buttons, BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(cofreLabel);
        panel.add(cofreSaldoLabel);
        panel.add(cofreJurosLabel);
        panel.add(info);
        p.add(panel, BorderLayout.CENTER);
        return p;
    }

    public JPanel homePanel() {
        JPanel home = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel apostaButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton enviar = new JButton("Enviar dinheiro");

        enviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = Main.getBooster().showTextInputDialog("Qual é o usuário que deseja enviar dinheiro?");
                int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto você deseja enviar?"));
                User target = UserManager.getInstance().getUserByName(userName);
                if (target == null) {
                    JOptionPane.showMessageDialog(null, "Esse usuário não existe!", "Usuário Não Encontrado", JOptionPane.ERROR_MESSAGE);
                } else {
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
                if (co != null) {
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
                    if (v > 0) {
                        acoes[i.getAndIncrement()] = a + "($" + v + ")";
                    } else {
                        acoes[i.getAndIncrement()] = a + "($" + 0 + ")";
                    }
                });

                List<String> selected = Main.getBooster().showMultipleSelection(
                        "Quais ações você deseja comprar?\n\n" +
                                "Importante: As ações funcionam da seguinte maneira, quando você compra uma ação é aplicado um juros (que varia com tempo) e ao finalizar a compra da ação ela passa a render na sua conta até que você venda ela.",
                        "Ações Disponíveis", acoes);

                for (String a : selected) {
                    MercadoManager.valores.keySet().stream().filter(a::contains).findFirst().ifPresent(name -> {
                        int valor = MercadoManager.valores.get(name);
                        if (valor > 0) {
                            long valorFinal = valor + (valor % MercadoManager.getJuros());
                            if (user.getSaldo() >= valorFinal) {
                                new Acoes(name, user.getToken());
                                TransferenceManager.removeTransference(user, "Mercado de Ações", valorFinal);
                                JOptionPane.showMessageDialog(null, "Você comprou a ação " + name + " por $" + valorFinal + "!");
                            } else {
                                JOptionPane.showMessageDialog(null, "Você não tem dinheiro para comprar a ação " + name + "!\nValor: " + valorFinal);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Tivemos que cancelar sua compra, pois a ação " + name + " não está valendo nada e acabou quebrando!");
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
                    acoes[i.getAndIncrement()] = a.getActionName() + " ($" + MercadoManager.valores.get(a.getActionName()) + ")";
                });

                List<String> selected = Main.getBooster().showMultipleSelection(
                        "Quais ações você deseja vender?\n\nImportante: Ao vender uma ação você recebe o valor da ação + o juros atual.",
                        "Ações Disponíveis", acoes);

                for (String a : selected) {
                    acoesUser.stream().filter(ac -> a.contains(ac.getActionName())).findFirst().ifPresent(ac -> {
                        ac.delete();
                        int valor = MercadoManager.valores.get(ac.getActionName());
                        if (valor > 0) {
                            long valorFinal = valor + (valor % MercadoManager.getJuros());
                            TransferenceManager.createTransference(user, "Mercado de Ações", valorFinal);
                            JOptionPane.showMessageDialog(null, "Você vendeu a ação " + ac.getActionName() + " por $" + valorFinal + "!");
                        } else {
                            JOptionPane.showMessageDialog(null, "Você vendeu a ação " + ac.getActionName() + " por $" + 0 + "!");
                        }
                    });
                }
            }
        });
        button.add(enviar);
        button.add(receber);
        button.add(comprar);
        button.add(vender);
        buttons.add(button);
        JButton apostar = new JButton("Apostar");
        apostar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random r = new Random();
                String selection = Main.getBooster().showSelectionDialog(
                        "Qual tipo de aposta você deseja fazer?",
                        "Escolha o tipo de aposta",
                        Arrays.asList("Chutar Número (1)", "Dominó sortido (2)"));
                if (selection.contains("1")) {
                    int x = NumberUtils.getRandomNumber(2, 10);
                    int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto deseja apostar nessa?\n\nSe você acertar irá ganhar " + x + "X em cima do valor apostado."));
                    int value = Main.getBooster().showSlider("Selecione o número que deseja chutar!", "Chute um número (Aposta)", 0, 10, 5);
                    int n = r.nextInt(10);
                    if (n == value) {
                        TransferenceManager.createTransference(user, "Cassino", valor * x);
                        JOptionPane.showMessageDialog(null, "Você acertou e recebeu " + valor * x + "!");
                    } else {
                        TransferenceManager.removeTransference(user, "Cassino", valor);
                        JOptionPane.showMessageDialog(null, "Você perdeu, era " + n + "!");
                    }
                } else if (selection.contains("2")) {
                    AtomicInteger x = new AtomicInteger(NumberUtils.getRandomNumber(1, 5));
                    Main.getBooster().showList(
                            "Escolha a peça que deseja apostar, se a peça que você escolher for a mesma do sistema, você ganha se não você perde.\n\nA aposta é feita diretamente com saldo da sua conta, tome cuidado!",
                            "Selecione uma peça",
                            element -> {
                                switch (element.getTitle()) {
                                    case "1X":
                                        if (x.get() == 1) {
                                            TransferenceManager.createTransference(user, "Cassino (Domino)", (user.getSaldo() % (NumberUtils.getRandomNumber(1, 10))));
                                            JOptionPane.showMessageDialog(null, "Parabéns, você acertou a mesma peça do sistema.\nSeu dinheiro já está na conta!");
                                        } else {
                                            TransferenceManager.removeTransference(user, "Cassino (Domino)", (user.getSaldo() % 5));
                                            JOptionPane.showMessageDialog(null, "Você perdeu e por isso removemos o valor estipulado da sua conta!");
                                        }
                                        break;
                                    case "2X":
                                        if (x.get() == 2) {
                                            TransferenceManager.createTransference(user, "Cassino (Domino)", (user.getSaldo() % (NumberUtils.getRandomNumber(1, 30))));
                                            JOptionPane.showMessageDialog(null, "Parabéns, você acertou a mesma peça do sistema.\nSeu dinheiro já está na conta!");
                                        } else {
                                            TransferenceManager.removeTransference(user, "Cassino (Domino)", (user.getSaldo() % 20));
                                            JOptionPane.showMessageDialog(null, "Você perdeu e por isso removemos o valor estipulado da sua conta!");
                                        }
                                        break;
                                    case "3X":
                                        if (x.get() == 3) {
                                            TransferenceManager.createTransference(user, "Cassino (Domino)", (user.getSaldo() % (NumberUtils.getRandomNumber(1, 60))));
                                            JOptionPane.showMessageDialog(null, "Parabéns, você acertou a mesma peça do sistema.\nSeu dinheiro já está na conta!");
                                        } else {
                                            TransferenceManager.removeTransference(user, "Cassino (Domino)", (user.getSaldo() % 40));
                                            JOptionPane.showMessageDialog(null, "Você perdeu e por isso removemos o valor estipulado da sua conta!");
                                        }
                                        break;
                                    case "4X":
                                        if (x.get() == 4) {
                                            TransferenceManager.createTransference(user, "Cassino (Domino)", (user.getSaldo() % (NumberUtils.getRandomNumber(1, 70))));
                                            JOptionPane.showMessageDialog(null, "Parabéns, você acertou a mesma peça do sistema.\nSeu dinheiro já está na conta!");
                                        } else {
                                            TransferenceManager.removeTransference(user, "Cassino (Domino)", (user.getSaldo() % 60));
                                            JOptionPane.showMessageDialog(null, "Você perdeu e por isso removemos o valor estipulado da sua conta!");
                                        }
                                        break;
                                    case "5X":
                                        if (x.get() == 5) {
                                            TransferenceManager.createTransference(user, "Cassino (Domino)", (user.getSaldo() * 2));
                                            JOptionPane.showMessageDialog(null, "Parabéns, você acertou a mesma peça do sistema.\nSeu dinheiro já está na conta!");
                                        } else {
                                            TransferenceManager.removeTransference(user, "Cassino (Domino)", (user.getSaldo() * 2));
                                            JOptionPane.showMessageDialog(null, "Você perdeu e por isso removemos o valor estipulado da sua conta!");
                                        }
                                        break;
                                }
                                x.set(NumberUtils.getRandomNumber(1, 5));
                            },
                            new ListElement("1X", "- Ganhe até 10% do seu saldo ou perca 5% do seu saldo", ImageUtils.getImage(this.getClass().getResourceAsStream("/images/domino/01.png"))),
                            new ListElement("2X", "- Ganhe até 30% do seu saldo ou perca 20% do seu saldo", ImageUtils.getImage(this.getClass().getResourceAsStream("/images/domino/02.png"))),
                            new ListElement("3X", "- Ganhe até 60% do seu saldo ou perca 40% do seu saldo", ImageUtils.getImage(this.getClass().getResourceAsStream("/images/domino/03.png"))),
                            new ListElement("4X", "- Ganhe até 70% do seu saldo ou perca 60% do seu saldo", ImageUtils.getImage(this.getClass().getResourceAsStream("/images/domino/04.png"))),
                            new ListElement("5X", "- Tudo ou nada\n- Ganhe 2X do seu saldo ou perca até 200% do seu saldo", ImageUtils.getImage(this.getClass().getResourceAsStream("/images/domino/05.png")))
                    );
                }
            }
        });
        JButton doar = new JButton("Doar");
        doar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> instituicoes = Main.getBooster().showMultipleSelection("Você é uma pessoa bondosa?\nDoe parte do seu dinheiro para instituições!",
                        "Doar",
                        "DoBem", "Instituto do Cancer", "MTE", "Moradores de Rua");
                for (String i : instituicoes) {
                    int valor = Integer.parseInt(Main.getBooster().showTextInputDialog("Quanto deseja doar para " + i + "?"));
                    TransferenceManager.removeTransference(user, "Doação (" + i + ")", valor);
                }
            }
        });
        apostaButtons.add(apostar);
        apostaButtons.add(doar);
        buttons.add(apostaButtons);
        home.add(buttons, BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        jurosLabel = new JLabel("O juros atual está em %" + MercadoManager.getJuros());
        jurosLabel.setFont(jurosLabel.getFont().deriveFont(16l));
        DefaultCategoryDataset ldata = new DefaultCategoryDataset();
        JFreeChart lucro = ChartFactory.createLineChart(
                "Histórico do seu saldo", "Data",
                "Valor",
                ldata, PlotOrientation.VERTICAL,
                true, true, false);
        lucrochart = new ChartPanel(lucro);
        lucrochart.setPreferredSize(new Dimension(480, 240));
        panel.add(lucrochart);
        JLabel resumoLabel = new JLabel("Resumo desta semana:");
        resumoLabel.setFont(resumoLabel.getFont().deriveFont(16f));
        panel.add(jurosLabel);
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        System.out.println("Tamanho da lista de juros " + MercadoManager.jurosHistory.size());
        JFreeChart jurosChart = ChartFactory.createLineChart(
                "Histórico dos Juros", "Data",
                "Valor",
                data, PlotOrientation.VERTICAL,
                true, true, false);
        chartpanel = new ChartPanel(jurosChart);
        chartpanel.setPreferredSize(new Dimension(480, 240));
        panel.add(chartpanel);
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

    public JPanel menuPanel() {
        JPanel menu = new JPanel();
        Color color = ColorUtils.hexToRgb("#171826");
        menu.setBackground(color);
        BoxLayout layout = new BoxLayout(menu, BoxLayout.Y_AXIS);
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
        EJSLButton emprestimo = new EJSLButton("Emprestimos");
        emprestimo.setPrimaryColor(color);
        emprestimo.setAnimation(true);
        menu.add(emprestimo);
        EJSLButton cofre = new EJSLButton("Cofre");
        cofre.setPrimaryColor(color);
        cofre.setAnimation(true);
        menu.add(cofre);
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
            emprestimo.setSelected(false);
            cofre.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });

        mercado.getClickListeners().add(e -> {
            e.setSelected(false);
            if (mercadoDeAcoesFrame != null && mercadoDeAcoesFrame.isActive()) {
                mercadoDeAcoesFrame.dispose();
            }
            mercadoDeAcoesFrame = new MercadoDeAcoesFrame();
        });

        emprestimo.addClickListener(e -> {
            this.menu.show(center, "EMP");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(false);
            emprestimo.setSelected(true);
            cofre.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });

        cofre.addClickListener(e -> {
            this.menu.show(center, "COFRE");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(false);
            cofre.setSelected(true);
            emprestimo.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });

        transferencias.getClickListeners().add(e -> {
            this.menu.show(center, "TRANS");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(true);
            emprestimo.setSelected(false);
            cofre.setSelected(false);
            card.setSelected(false);
            sobre.setSelected(false);
            revalidateAll();
        });
        card.getClickListeners().add(e -> {
            String password = Main.getBooster().showPasswordDialog("Importante: Se seu cartão não aparecer tente trocar o seu JDK pelo Amazon Corretto 20.\nPor favor, confirme sua senha antes de ver seu cartão:", "Confirme sua senha");
            if (password.equalsIgnoreCase(user.getPassword())) {
                this.menu.show(center, "CARD");
                home.setSelected(false);
                mercado.setSelected(false);
                transferencias.setSelected(false);
                emprestimo.setSelected(false);
                card.setSelected(true);
                cofre.setSelected(false);
                sobre.setSelected(false);
                revalidateAll();
            } else {
                JOptionPane.showMessageDialog(null, "Você errou sua senha, por isso iremos te desconectar do app.");
                this.dispose();
            }
        });
        sobre.getClickListeners().add(e -> {
            this.menu.show(center, "SOBRE");
            home.setSelected(false);
            mercado.setSelected(false);
            transferencias.setSelected(false);
            emprestimo.setSelected(false);
            card.setSelected(false);
            cofre.setSelected(false);
            sobre.setSelected(true);
            revalidateAll();
        });
        sair.getClickListeners().add(e -> {
            dispose();
            new LoginFrame();
        });
        return menu;
    }

    public void revalidateAll() {
        Arrays.stream(this.getComponents()).toList().forEach(c -> {
            revalidate(c);
        });
    }

    public void revalidate(Component c) {
        if (c instanceof JPanel p) {
            Arrays.stream(p.getComponents()).toList().forEach(cp -> revalidate(cp));
        }
        c.revalidate();
        c.repaint();
    }
}
