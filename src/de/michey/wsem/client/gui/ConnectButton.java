package de.michey.wsem.client.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectButton extends JButton {

    public ConnectButton(final ChatWindow cw) {
        setText("Verbinden");

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cw.currentState == 0) {
                    cw.currentState = 1;
                    cw.username = cw.inputUsername.getText().replace(" ", "");
                    cw.getKnownParticipants().add(cw.username);

                    cw.client = cw.initClient(cw, new String[] {cw.inputHost.getText(), cw.inputPort.getText(), cw.inputUsername.getText().replace(" ", "")});
                }
            }
        });
    }

}
