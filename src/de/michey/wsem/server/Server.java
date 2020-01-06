package de.michey.wsem.server;

import static de.michey.wsem.util.Utils.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {

    private String ip;
    private int port;
    private ArrayList<ClientConnection> existingConnections;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        existingConnections = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));

            while(true) {
                logServer("Warte auf eingehende Verbindungen...");

                Socket clientSocket = serverSocket.accept();
                logServer("Neue eingehende Verbindung: " + clientSocket.toString());

                ClientConnection cc = new ClientConnection(this, clientSocket);
                existingConnections.add(cc);
                cc.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeExistingConnection(ClientConnection cc) {
        existingConnections.remove(cc);
    }

    public ArrayList<ClientConnection> getExistingConnections() {
        return existingConnections;
    }

}
