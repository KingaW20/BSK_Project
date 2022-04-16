package bsk.project;

import javax.swing.*;
import java.awt.event.*;

public class LogInPanel {
    private JTextArea userNameTextArea;
    public JPanel logInPanel;
    private JButton okButton;
    private JLabel userLabel;
    private String userName;

    public LogInPanel(JFrame frame) {
        frame.setVisible(true);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = userNameTextArea.getText();
            }
        });
    }

    public String getUserName() {
        return userName;
    }
}
