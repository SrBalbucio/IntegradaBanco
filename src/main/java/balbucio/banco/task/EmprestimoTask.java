package balbucio.banco.task;

import balbucio.banco.Main;
import balbucio.banco.manager.EmprestimoManager;
import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.responsivescheduler.RSTask;

import java.security.spec.ECField;
import java.util.Calendar;
import java.util.Date;

public class EmprestimoTask extends RSTask {

    @Override
    public void run() {
        try {
            if (!Main.connected()) {
                EmprestimoManager.emprestimos.forEach(e -> {
                    Date today = new Date();
                    Date empTime = new Date(e.getMaxTime());
                    if (today.after(empTime)) {
                        long finalValue = e.getValor() * MercadoManager.getJuros();
                        TransferenceManager.removeTransference(UserManager.getInstance().getUserByToken(e.getDevedor()), "Banco (Emprestimo)", finalValue);
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            setProblemID(4);
            setProblem(true);
        }
    }
}
