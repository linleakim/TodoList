package ui;

import dal.IUserRepository;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame parent, IUserRepository userRepo) {
        super(parent, "Registration", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerBtn = new JButton("Register");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(247, 243, 255)); // color
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerBtn);

        registerBtn.setBackground(new Color(200, 180, 255));
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (userRepo.register(user, pass)) {
                JOptionPane.showMessageDialog(this, "Successful registration!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "User already exists.");
            }
        });

        add(panel);
    }
}
