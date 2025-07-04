package ui;

import dal.IRepository;
import dal.TodoTask;

import javax.swing.*;
import java.awt.*;

public class CreateTaskDialog extends JDialog {

    private final IRepository repository;
    private final Runnable onTaskCreatedCallback;

    public CreateTaskDialog(JFrame parent, IRepository repository, Runnable onTaskCreatedCallback) {
        super(parent, "New task", true);
        this.repository = repository;
        this.onTaskCreatedCallback = onTaskCreatedCallback;

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextArea contentArea = new JTextArea(5, 20);

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Descriprion:"));
        panel.add(descField);
        panel.add(new JLabel("Content:"));

        JScrollPane scroll = new JScrollPane(contentArea);
        panel.add(scroll);

        JButton createButton = new JButton("New");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String content = contentArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Can not be empty.");
                return;
            }

            TodoTask task = new TodoTask(name, desc, content);
            repository.add(task);

            if (onTaskCreatedCallback != null)
                onTaskCreatedCallback.run();

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(getParent());
    }
}

