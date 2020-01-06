package de.michey.wsem.client;

import de.michey.wsem.client.gui.ChatWindow;

import javax.swing.*;

public class ClientMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        new ChatWindow();
    }

}
