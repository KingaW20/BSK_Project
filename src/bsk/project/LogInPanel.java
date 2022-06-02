package bsk.project;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

public class LogInPanel {
    public JPanel logInPanel;
    private JButton okButton;
    private JLabel userLabel;
    private JTextField loginText;
    private JLabel passwordLabel;
    private JPasswordField passwordText;
    private String userName;
    private String userPassword;

    public LogInPanel(JFrame frame) {
        loginText.setDocument(new LimitJTextField(25));
        passwordText.setDocument(new LimitJTextField(15));
        frame.setVisible(true);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = loginText.getText();
                userPassword = String.valueOf(passwordText.getPassword());
                System.out.println("Password: " + userPassword);
            }
        });
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() { return userPassword; }
}

class LimitJTextField extends PlainDocument {
    private int max;

    LimitJTextField(int max) {
        super();
        this.max = max;
    }

    public void insertString(int offset, String text, AttributeSet attr) throws BadLocationException {
        if (text == null) {
            return;
        }
        if (getLength() + text.length() <= max) {
            super.insertString(offset, text, attr);
        }
    }
}
