package balbucio.banco.task;

import balbucio.banco.manager.UserManager;
import balbucio.banco.model.User;
import balbucio.responsivescheduler.RSTask;

public class ImpostoTask extends RSTask {

    @Override
    public void run() {
        UserManager.getUsers().forEach(u -> {
            long saldo = u.getSaldo();

        });
    }
}
