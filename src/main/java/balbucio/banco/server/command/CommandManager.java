package balbucio.banco.server.command;

import balbucio.banco.server.command.impl.PortSwitchCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private Map<String, Command> commands = new HashMap<>();

    public CommandManager(){
        commands.put("switchport", new PortSwitchCommand());
    }

    public void execute(String cmd){
        String[] args = cmd.split(" ");
        if(commands.containsKey(args[0])){
            commands.get(args[0]).run(args);
        } else{
            System.out.println("Comando não reconhecido!");
        }
    }
}
