package de.michey.wsem;

import de.michey.wsem.client.ClientMain;
import de.michey.wsem.server.ServerMain;

public class Main {

    public static void main(String[] args) {
        if(args[0].equals("server")) {
            ServerMain.main(args);
        } else {
            ClientMain.main(args);
        }
    }

}
