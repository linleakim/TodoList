package ui;

import dal.IUserRepository;

import javax.swing.*;

public class RegisterDialog extends JDialog {

    public RegisterDialog(JFrame parent, IUserRepository userRepo) {
        super(parent, "Registration", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerBtn = new JButton("Registration");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("username:"));
        panel.add(usernameField);
        panel.add(new JLabel("password:"));
        panel.add(passwordField);
        panel.add(registerBtn);

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (userRepo.register(user, pass)) {
                JOptionPane.showMessageDialog(this, "Succsessful registration ");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "user already exists");
            }
        });

        add(panel);
    }
}

