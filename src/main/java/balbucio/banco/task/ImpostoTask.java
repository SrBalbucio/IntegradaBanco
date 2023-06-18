package balbucio.banco.task;

import balbucio.banco.manager.MercadoManager;
import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.manager.UserManager;
import balbucio.banco.model.User;
import balbucio.responsivescheduler.RSTask;

public class ImpostoTask extends RSTask {

    @Override
    public void run() {
        UserManager.getUsers().forEach(u -> {
            User clonedUser = u.clone();
            TransferenceManager.getTransferences(u).forEach(t -> clonedUser.transference(t));
            long saldo = clonedUser.getSaldo();
            long descontar = 0;
            if(saldo < 500){
                descontar = saldo % 10;
            } else if(saldo > 1000 && saldo < 5000){
                descontar = saldo % 15;
            } else if(saldo > 5000 && saldo < 10000){
                descontar = saldo % 30;
            } else if(saldo > 10000 && saldo < 100000){
                descontar = saldo % 40;
            } else if(saldo > 100000){
                descontar = saldo % 50;
            }
            descontar += (descontar % 15) * MercadoManager.juros;
            System.out.println("IMPOSTO A SER COBRADO DE "+u.getToken()+": $"+descontar);
            TransferenceManager.removeTransference(u, "Receita Federal", descontar);
        });
    }
}
