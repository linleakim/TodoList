package ui;

import dal.*;
import services.MessageService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final IUserRepository userRepo;

    public LoginFrame(IUserRepository userRepo) {
        this.userRepo = userRepo;
        initUI();
    }

    private void initUI() {
        setTitle("Login");
        setSize(320, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // creating elements
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = createStyledButton("Login", new Color(255, 230, 120));
        JButton registerButton = createStyledButton("Register", new Color(200, 180, 255));

        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(247, 243, 255)); // лавандовий фон
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(5));
        panel.add(registerButton);

        // Button Login
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (userRepo.login(username, password)) {
                dispose();
                IRepository todoRepo = new MongoRepository(username);
                IMessageRepository messageRepo = new MongoMessageRepository();
                MessageService messageService = new MessageService(messageRepo);

                SwingUtilities.invokeLater(() -> new TodoSplitApp(todoRepo, userRepo, messageService));
            } else {
                JOptionPane.showMessageDialog(this, "Wrong username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Button Register
        registerButton.addActionListener(e -> new RegisterDialog(this, userRepo).setVisible(true));

        add(panel);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }
}
