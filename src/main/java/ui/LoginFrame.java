package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.MongoRepository;
import dal.IMessageRepository;
import dal.MongoMessageRepository;
import services.MessageService;

import javax.swing.*;

public class LoginFrame extends JFrame {

    private final IUserRepository userRepo;

    public LoginFrame(IUserRepository userRepo) {
        this.userRepo = userRepo;
        initUI();
    }

    private void initUI() {
        setTitle("Login ");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login ");
        JButton registerButton = new JButton("Registation");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("username:"));
        panel.add(usernameField);
        panel.add(new JLabel("password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (userRepo.login(user, pass)) {
                dispose();

                // Create repositories
                IRepository todoRepo = new MongoRepository(user); // main todo repository
                IMessageRepository messageRepo = new MongoMessageRepository(); // NEW: message repository

                // Create message service
                MessageService messageService = new MessageService(messageRepo); // NEW: message service

                // Open main window with messaging support
                new TodoSplitApp(todoRepo, userRepo, messageService); // UPDATED: added messageService parameter
            } else {
                JOptionPane.showMessageDialog(this, "wrong username or password ");
            }
        });

        registerButton.addActionListener(e -> new RegisterDialog(this, userRepo).setVisible(true));

        add(panel);
    }
}