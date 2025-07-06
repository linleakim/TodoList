package ui;

import dal.IRepository;
import dal.TodoTask;
import dal.TaskStatus;

import javax.swing.*;
import java.awt.*;

public class CreateTaskDialog extends JDialog {
    private final IRepository repository;
    private final Runnable onTaskCreatedCallback;

    public CreateTaskDialog(JFrame parent, IRepository repository, Runnable onTaskCreatedCallback) {
        super(parent, "New Task", true);
        this.repository = repository;
        this.onTaskCreatedCallback = onTaskCreatedCallback;
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(new Color(247, 243, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextArea contentArea = new JTextArea(5, 20);
        JScrollPane scroll = new JScrollPane(contentArea);

        JTextField statusField = new JTextField(TaskStatus.NOT_STARTED.getDisplayName());
        statusField.setEditable(false);
        statusField.setBackground(new Color(230, 230, 230));

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Content:"));
        panel.add(scroll);
        panel.add(new JLabel("Status:"));
        panel.add(statusField);

        JButton createButton = createStyledButton("Create", new Color(255, 230, 120));
        JButton cancelButton = createStyledButton("Cancel", new Color(200, 180, 255));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(247, 243, 255));
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String content = contentArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                return;
            }

            TodoTask task = new TodoTask(name, desc, content);
            repository.add(task);

            if (onTaskCreatedCallback != null) onTaskCreatedCallback.run();

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 350);
        setLocationRelativeTo(getParent());
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
