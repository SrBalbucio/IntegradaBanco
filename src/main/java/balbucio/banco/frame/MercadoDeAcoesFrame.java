package balbucio.banco.frame;

import balbucio.banco.Main;
import balbucio.banco.manager.MercadoManager;
import balbucio.responsivescheduler.RSTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MercadoDeAcoesFrame extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public MercadoDeAcoesFrame(){
        super("Mercado de Ações");
        this.setSize(320, 640);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        model = new DefaultTableModel();
        model.addColumn("Ação");
        model.addColumn("Valor");
        Main.getScheduler().repeatTask(new RSTask() {
            @Override
            public void run() {
                try {
                    model = new DefaultTableModel();
                    model.addColumn("Ação");
                    model.addColumn("Valor");

                    MercadoManager.valores.forEach((n, v) -> {
                        model.addRow(new Object[]{n, "$" + v});
                    });
                    table.setModel(model);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 1000, 5000);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane pane = new JScrollPane(table);
        pane.setVisible(true);
        this.add(pane);
        this.setVisible(true);
    }
}
