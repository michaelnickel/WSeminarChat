package de.michey.wsem.client.gui;

import de.michey.wsem.client.Client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;

import static de.michey.wsem.util.Utils.logClient;

public class ChatWindow implements KeyListener {

    private JFrame window;

    public JPanel connectPanel;
    public JPanel disconnectPanel;
    public JLabel status;

    public int currentState = 0;

    private JTextArea chatlog;
    public String newMessage;
    private JLabel participantList;
    private JTextField inputChat;
    public JTextField inputUsername;
    public JTextField inputHost;
    public JTextField inputPort;

    public String username;
    private ArrayList<String> knownParticipants;

    public Client client;

    public ChatWindow() {
        knownParticipants = new ArrayList<>();

        window = new JFrame("Chatprogramm");
        window.setSize(600, 400);
        window.setResizable(false);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setLayout(null);

        int componentsInLine = 3;
        int startX = 16;
        int startY = 16;
        int spacingX = 8;
        int spacingY = 8;
        int singleComponentWidth = (window.getWidth() - (startX * 2) - ((componentsInLine - 1) * spacingX)) / componentsInLine;
        int totalHeight = (window.getHeight() - (startY * 2) - (spacingY * 2) - 24 - 32 - 24) / 2;

        {
            connectPanel = new JPanel();
            connectPanel.setSize(window.getWidth(), window.getHeight());
            connectPanel.setLayout(null);

            JLabel txtHost = new JLabel("Zieladresse:");
            txtHost.setBounds(startX, totalHeight, singleComponentWidth, 24);
            connectPanel.add(txtHost);

            inputHost = new JTextField("localhost");
            inputHost.setBounds(startX, txtHost.getY() + txtHost.getHeight() + spacingY, singleComponentWidth, 24);
            connectPanel.add(inputHost);

            inputPort = new JTextField("51220");
            inputPort.setBounds(inputHost.getX() + inputHost.getWidth() + spacingX, inputHost.getY(), singleComponentWidth, 24);
            connectPanel.add(inputPort);

            JLabel txtPort = new JLabel("Port:");
            txtPort.setBounds(inputPort.getX(), totalHeight, singleComponentWidth, 24);
            connectPanel.add(txtPort);

            inputUsername = new JTextField("Username");
            inputUsername.setBounds(inputPort.getX() + inputPort.getWidth() + spacingX, inputHost.getY(), singleComponentWidth, 24);
            connectPanel.add(inputUsername);

            JLabel txtUsername = new JLabel("Name:");
            txtUsername.setBounds(inputUsername.getX(), totalHeight, singleComponentWidth, 24);
            connectPanel.add(txtUsername);

            ConnectButton button = new ConnectButton(this);
            button.setBounds(startX, inputUsername.getY() + inputUsername.getHeight() + spacingY, ((inputUsername.getX() + inputUsername.getWidth()) - startX), 32);
            button.setFocusPainted(false);
            connectPanel.add(button);


            status = new JLabel("...");
            status.setHorizontalAlignment(SwingConstants.CENTER);
            status.setBounds(startX, window.getHeight() / 3 * 2, window.getWidth() - (startX * 2), 32);
            connectPanel.add(status);
        }

        {
            disconnectPanel = new JPanel();
            disconnectPanel.setSize(window.getWidth(), window.getHeight());
            disconnectPanel.setLayout(null);

            chatlog = new JTextArea();
            chatlog.setBackground(Color.lightGray);
            chatlog.setBounds(startX, startY, window.getWidth() - (startX * 2) - spacingX - 128, 256);
            chatlog.setEditable(false);
            chatlog.setFont(new Font("Arial", Font.PLAIN, 12));
            disconnectPanel.add(chatlog);

            participantList = new JLabel("<init>");
            participantList.setVerticalAlignment(SwingConstants.TOP);
            participantList.setBackground(Color.lightGray);
            participantList.setOpaque(true);
            participantList.setBounds(chatlog.getX() + chatlog.getWidth() + spacingX, chatlog.getY(), window.getWidth() - (startX * 2) - spacingX - chatlog.getWidth(), chatlog.getHeight());
            disconnectPanel.add(participantList);

            inputChat = new JTextField("Text");
            inputChat.setBounds(chatlog.getX(), chatlog.getY() + chatlog.getHeight() + spacingY, chatlog.getWidth(), 32);
            disconnectPanel.add(inputChat);
            inputChat.addKeyListener(this);

            JButton sendMessage = new JButton("Senden");
            sendMessage.setBounds(inputChat.getX() + inputChat.getWidth() + spacingX, inputChat.getY(), participantList.getWidth(), 32);
            sendMessage.setFocusPainted(false);
            disconnectPanel.add(sendMessage);
            sendMessage.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleSendMessage();
                }
            });

            JButton disconnect = new JButton("Verbindung trennen");
            disconnect.setBounds(inputChat.getX(), inputChat.getY() + inputChat.getHeight() + spacingY, window.getWidth() - (startX * 2), 32);
            disconnectPanel.add(disconnect);
            disconnect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentState = 0;
                    knownParticipants.clear();
                    chatlog.setText("");

                    disconnectPanel.setVisible(false);
                    connectPanel.setVisible(true);

                    try {
                        client.sendCommand("disconnect " + username);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        window.add(connectPanel);
        disconnectPanel.setVisible(false);
        window.add(disconnectPanel);

        window.setVisible(true);
    }

    public void updateChatlog() {
        chatlog.append(newMessage + System.lineSeparator());
    }

    public ArrayList<String> getKnownParticipants() {
        return knownParticipants;
    }

    public void updateParticipantsList() {
        String fullText = "<html><body>";

        for(String participant : knownParticipants) {
            fullText += participant + "<br>";
        }

        participantList.setText(fullText + "</body></html>");
    }

    public Client initClient(ChatWindow window, String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String user = args[2];

        logClient("Client wird gestartet, verbinde zu '" + host + ":" + port + "' mit dem Namen '" + user + "'.");

        Client client = new Client(host, port, user, window);
        client.start();

        return client;
    }

    public JFrame getWindow() {
        return window;
    }

    private void handleSendMessage() {
        if(inputChat.getText().length() > 0) {
            try {
                client.sendCommand("message " + username + " " + inputChat.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            inputChat.setText("");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleSendMessage();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}
