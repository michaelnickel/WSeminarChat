package de.michey.wsem.client;

import de.michey.wsem.client.gui.ChatWindow;

import static de.michey.wsem.util.Utils.*;

import java.io.*;
import java.net.Socket;

public class Client extends Thread {

    private ChatWindow window;

    private String host;
    private int port;
    private String user;

    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    public Client(String host, int port, String user, ChatWindow window) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.window = window;
    }

    @Override
    public void run() {
        try {
            if(connect()) {
                logClient("Verbindung zum Server war erfolgreich!");

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            sendCommand("disconnect " + user);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                if(login(user)) {
                    logClient("Erfolgreich beim Server mit dem Namen '" + user + "' eingeloggt!");
                    window.currentState = 2;

                    window.connectPanel.setVisible(false);
                    window.disconnectPanel.setVisible(true);

                    window.updateParticipantsList();
                } else {
                    logClient("Das Einloggen beim Server mit dem Namen '" + user + "' ist fehlgeschlagen!");
                    window.currentState = 0;
                }
            } else {
                logClient("Verbindung zum Server ist fehlgeschlagen!");
                window.status.setText("Der Server '" + host + ":" + port + "' ist nicht erreichbar.");
                window.currentState = 0;
                window.getKnownParticipants().remove(window.username);
            }
        } catch (IOException e) {
            e.printStackTrace();
            window.currentState = 0;
        }
    }

    public void sendCommand(String cmd) throws IOException {
        serverOut.write(("/" + cmd + System.lineSeparator()).getBytes());
    }

    private boolean login(String username) throws IOException {
        sendCommand("connect " + username);

        if(bufferedIn.readLine().equals("login_success")) {
            startMessageReader();
            return true;
        }

        return false;
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };

        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;

            while((line = bufferedIn.readLine()) != null) {
                String[] args = line.split(" ");

                if(args.length > 0) {
                    String cmd = args[0].toLowerCase();

                    if(cmd.equals("/connect")) {
                        boolean silent = args.length == 3 && args[2].equals("-silent");
                        handleUserConnect(args[1], silent);
                    } else if(cmd.equals("/disconnect")) {
                        handleUserDisconnect(args[1]);
                    } else if(cmd.equals("/message")) {
                        handleUserMessage(args);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleUserConnect(String username, boolean silent) {
        logClient("Der Nutzer " + username + " hat sich eingeloggt!");

        if(!silent) {
            window.newMessage = "[System] Der Nutzer " + username + " hat sich dem Chat angeschlossen.";
            window.updateChatlog();
        }

        window.getKnownParticipants().add(username);
        window.updateParticipantsList();
    }

    private void handleUserDisconnect(String username) {
        logClient("Der Nutzer " + username + " hat sich ausgeloggt!");

        window.newMessage = "[System] Der Nutzer " + username + " hat den Chat verlassen.";
        window.updateChatlog();

        window.getKnownParticipants().remove(username);
        window.updateParticipantsList();
    }

    private void handleUserMessage(String[] args) {
        String text = "";
        for(int i = 3; i < args.length; i++) {
            text += args[i] + " ";
        }
        text = text.substring(0, text.length() - 1);

        logClient(args[1] + " schreibt: " + text);
        window.newMessage = "[" + now() + "] " + args[1] + ": " + text;
        window.updateChatlog();
    }

    private boolean connect() {
        try {
            socket = new Socket(host, port);
            serverIn = socket.getInputStream();
            serverOut = socket.getOutputStream();
            bufferedIn = new BufferedReader(new InputStreamReader(serverIn));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
