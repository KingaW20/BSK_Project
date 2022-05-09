package bsk.project;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

public class LogInPanel {
    public JPanel logInPanel;
    private JButton okButton;
    private JLabel userLabel;
    private JTextField loginText;
    private String userName;

    public LogInPanel(JFrame frame) {
        loginText.setDocument(new LimitJTextField(25));
        frame.setVisible(true);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = loginText.getText();
            }
        });
    }

    public String getUserName() {
        return userName;
    }
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
