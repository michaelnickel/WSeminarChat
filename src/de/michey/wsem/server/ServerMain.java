package de.michey.wsem.server;

import static de.michey.wsem.util.Utils.*;

public class ServerMain {

    public static void main(String[] args) {
        String ip = "localhost";
        int port = 51220;

        if(args.length == 3) { // java -jar file.jar server <ip> <port>
            ip = args[1];
            port = Integer.parseInt(args[2]);
        }

        logServer("Server wird mit der Adresse '" + ip + "' und dem Port '" + port + "' gestartet.");

        Server server = new Server(ip, port);
        server.start();
    }

}
