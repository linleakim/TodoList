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
        super(parent, "New task", true);
        this.repository = repository;
        this.onTaskCreatedCallback = onTaskCreatedCallback;

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10)); // NEW: Changed from 4 to 5 rows
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextArea contentArea = new JTextArea(5, 20);

        // NEW: Status field (read-only for new tasks)
        JTextField statusField = new JTextField(TaskStatus.NOT_STARTED.getDisplayName());
        statusField.setEditable(false);
        statusField.setBackground(Color.LIGHT_GRAY);

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:")); // Fixed typo
        panel.add(descField);
        panel.add(new JLabel("Content:"));

        JScrollPane scroll = new JScrollPane(contentArea);
        panel.add(scroll);

        // NEW: Add status field
        panel.add(new JLabel("Status:"));
        panel.add(statusField);

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
                JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                return;
            }

            TodoTask task = new TodoTask(name, desc, content); // Status defaults to NOT_STARTED
            repository.add(task);

            if (onTaskCreatedCallback != null)
                onTaskCreatedCallback.run();

            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 350); // NEW: Increased height for status field
        setLocationRelativeTo(getParent());
    }
}
