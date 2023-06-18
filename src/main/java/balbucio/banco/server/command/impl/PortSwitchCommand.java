package balbucio.banco.server.command.impl;

import balbucio.banco.server.BancoServer;
import balbucio.banco.server.command.Command;

public class PortSwitchCommand implements Command {


    @Override
    public void run(String[] args) {
        if(args.length > 0){
            int port = Integer.parseInt(args[1]);
            BancoServer.switchPort(port);
        } else{
            System.out.println("Comando incorreto, use switchport <port>");
        }
    }
}
