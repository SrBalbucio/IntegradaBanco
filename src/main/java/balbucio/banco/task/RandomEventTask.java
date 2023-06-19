package balbucio.banco.task;

import balbucio.banco.manager.TransferenceManager;
import balbucio.banco.model.Transference;
import balbucio.banco.model.User;
import balbucio.banco.utils.NumberUtils;
import balbucio.responsivescheduler.RSTask;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RandomEventTask extends RSTask {

    private
    List<String> events = new ArrayList<>();
    private User user;
    public RandomEventTask(User user){
        this.user = user;
        events.add("Sua Tia morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Sua Avó morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Seu Avô morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Seu Tio morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Seu filho morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Sua filha morreu e você teve que pagar o enterro você perdeu ${valor}!");
        events.add("Você teve que fazer um exame caro e perdeu ${valor}!");
        events.add("Você teve que ir ao hospital e teve que pagar a consulta ${valor}!");
        events.add("Sua filha fez aniversário e você teve que comprar um presente, você perdeu ${valor}");
    }

    @Override
    public void run() {
        Random r = new Random();
        int event = r.nextInt(events.size());
        int valor = NumberUtils.getRandomNumber(500, 3500);
        String string = events.get(event).replace("{valor}", String.valueOf(valor));
        TransferenceManager.removeTransference(user, "Fatura do Cartão", valor);
        JOptionPane.showMessageDialog(null, string);
    }
}
