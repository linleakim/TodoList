package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.MongoRepository;

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
                IRepository todoRepo = new MongoRepository(user); // mane repository 
                new TodoSplitApp(todoRepo, userRepo); // open main window 
            } else {
                JOptionPane.showMessageDialog(this, "wrong username or password ");
            }
        });

        registerButton.addActionListener(e -> new RegisterDialog(this, userRepo).setVisible(true));

        add(panel);
    }
}
