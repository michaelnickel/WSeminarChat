package de.michey.wsem.server;

import static de.michey.wsem.util.Utils.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection extends Thread {

    private Server server;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private String username;

    public ClientConnection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            evaluateClientData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void evaluateClientData() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        try {
            while((line = reader.readLine()) != null) {
                String[] args = line.split(" ");

                if(args.length > 0) {
                    String cmd = args[0].toLowerCase();
                    logServer("Eingehender Befehl von " + socket.toString() + ": " + line);

                    if(cmd.equals("/connect")) {
                        handleConnect(args[1]);
                    } else if(cmd.equals("/disconnect")) {
                        handleDisconnect();
                        break;
                    } else if(cmd.equals("/message")) {
                        handleMessage(args);
                    } else {
                        sendLineToClient("Unbekannter Befehl: " + line);
                    }
                }
            }
        } catch (IOException e) {
            String exception = e.getMessage();

            if(exception.contains("connection reset")) {
                handleDisconnect();
            }
        }

        socket.close();
    }

    private void handleConnect(String username) throws IOException {
        for(ClientConnection existing : server.getExistingConnections()) {
            if(existing.getUsername() != null && existing.getUsername().equals(username)) {
                sendLineToClient("Der Name " + username + " ist bereits vergeben, bitte wähle einen anderen.");
                return;
            }
        }

        this.username = username;
        sendLineToClient("login_success");
        logServer("Nutzer " + username + " hat sich eingeloggt.");

        ArrayList<ClientConnection> cons = server.getExistingConnections();
        for(int i = 0; i < cons.size(); i++) {
            if(cons.get(i).getUsername() != null) {
                if(cons.get(i).getUsername().equals(username)) continue;
                cons.get(i).sendLineToClient("/connect " + username);
            }
        }

        for(int i = 0; i < cons.size(); i++) {
            if(cons.get(i).getUsername() != null) {
                if(cons.get(i).getUsername().equals(username)) continue;
                sendLineToClient("/connect " + cons.get(i).getUsername() + " -silent");
            }
        }
    }

    private void handleDisconnect() throws IOException {
        server.removeExistingConnection(this);

        for(ClientConnection existing : server.getExistingConnections()) {
            if(existing.getUsername() != null) {
                existing.sendLineToClient("/disconnect " + username);
            }
        }

        socket.close();
        logServer("Nutzer " + username + " hat sich ausgeloggt.");
    }

    private void handleMessage(String[] args) throws IOException {
        String message = "";

        for(int i = 1; i < args.length; i++) {
            message += args[i] + " ";
        }

        for(ClientConnection existing : server.getExistingConnections()) {
            if(existing.getUsername() != null) {
                existing.sendLineToClient("/message " + username + " " + message);
            }
        }
    }

    public void sendToClient(String message) throws IOException {
        outputStream.write(message.getBytes());
    }

    public void sendLineToClient(String message) throws IOException {
        sendToClient(message + System.lineSeparator());
    }

    public String getUsername() {
        return username;
    }

}
