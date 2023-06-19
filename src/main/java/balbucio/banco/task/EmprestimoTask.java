package balbucio.banco.task;

import balbucio.banco.Main;
import balbucio.banco.manager.EmprestimoManager;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.responsivescheduler.RSTask;

import java.util.Calendar;
import java.util.Date;

public class EmprestimoTask extends RSTask {

    @Override
    public void run() {
        if(!Main.connected()) {
            EmprestimoManager.emprestimos.forEach(e -> {
                Date today = new Date();
                Date empTime = new Date(e.getMaxTime());
                if(today.after(empTime)){
                    long finalValue = e.getValor() * MercadoManager.getJuros();
                    TransferenceManager.removeTransference(UserManager.getInstance().getUserByToken(e.getToken()), "Banco (Emprestimo)", finalValue);
                }
            });
        }
    }
}
