package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.TodoTask;
import dal.TaskStatus;
import services.MessageService;
import models.Message;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

// main window
public class TodoSplitApp {
    private final IRepository repository;
    private final MessageService messageService;
    private final IUserRepository userRepository;
    private final String username;

    // interface
    private JFrame frame;
    private DefaultListModel<TodoTask> listModel;
    private JList<TodoTask> taskList;
    private JTextField nameField, descField;
    private JTextArea contentArea;
    private JComboBox<TaskStatus> statusComboBox;
    private JButton saveButton, deleteButton;
    private TodoTask currentSelectedTask;

    // chat
    private JTextArea groupMessagesArea, myMessagesArea;
    private JButton sendButton;
    private Timer messageRefreshTimer;
    private LocalDateTime lastDisplayedMessageTime;

    public TodoSplitApp(IRepository repository, IUserRepository userRepository, MessageService messageService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.username = repository.getCurrentUser();

        initUI(); // start UI
    }

    // user window
    private void initUI() {
        frame = new JFrame("Todo App — user: " + username);
        frame.setSize(900, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color bgColor = new Color(0xF7F3FF); // lila
        Color textColor = new Color(0x3D3D3D); // black
        Color buttonYellow = new Color(0xFFD56B);
        Color buttonPurple = new Color(0xD2A8FF);

        frame.getContentPane().setBackground(bgColor);

        // oben mt buttons
        JButton addButton = createRoundedButton("＋ Make new task", buttonYellow);
        addButton.addActionListener(e -> new CreateTaskDialog(frame, repository, this::loadTasks).setVisible(true));

        JButton logoutButton = createRoundedButton("Exit", buttonPurple);
        logoutButton.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(userRepository).setVisible(true));
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(bgColor);
        topPanel.add(addButton);
        topPanel.add(logoutButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // text field
        nameField = createRoundedTextField();
        descField = createRoundedTextField();
        contentArea = new JTextArea(10, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBorder(BorderFactory.createLineBorder(buttonPurple, 2, true));
        contentArea.setBackground(bgColor);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        statusComboBox = new JComboBox<>(TaskStatus.values());
        statusComboBox.setRenderer(new StatusRenderer());
        statusComboBox.setEnabled(false);

        saveButton = createRoundedButton("Save Changes", buttonYellow);
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveCurrentTask());

        deleteButton = createRoundedButton("Delete Task", buttonPurple);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteCurrentTask());

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(bgColor);

        JPanel fields = new JPanel(new GridLayout(4, 1, 5, 5));
        fields.setBackground(bgColor);
        fields.add(createLabeledPanel("Name:", nameField, bgColor, textColor));
        fields.add(createLabeledPanel("Description:", descField, bgColor, textColor));
        fields.add(createLabeledPanel("Status:", statusComboBox, bgColor, textColor));
        fields.add(createLabeledPanel("Content:", new JScrollPane(contentArea), bgColor, textColor));

        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        savePanel.setBackground(bgColor);
        savePanel.add(saveButton);
        savePanel.add(deleteButton);

        detailPanel.add(fields, BorderLayout.CENTER);
        detailPanel.add(savePanel, BorderLayout.SOUTH);

        // tasks
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskRenderer());
        taskList.addListSelectionListener(e -> loadTaskDetails(taskList.getSelectedValue()));

        JScrollPane listScroll = new JScrollPane(taskList);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailPanel, listScroll);
        splitPane.setDividerLocation(600);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, createMessagePanel(bgColor, textColor, buttonYellow));
        verticalSplit.setDividerLocation(450);
        verticalSplit.setResizeWeight(0.7);

        frame.add(verticalSplit, BorderLayout.CENTER);
        frame.setVisible(true);

        loadTasks();
        loadGroupMessages();
        startMessageRefreshTimer();
    }

    private void loadTasks() {
        listModel.clear();
        for (TodoTask task : repository.findAll()) listModel.addElement(task);
    }

    private void loadTaskDetails(TodoTask task) {
        if (task == null) return;

        currentSelectedTask = task;
        nameField.setText(task.getName());
        descField.setText(task.getDescription());
        contentArea.setText(task.getContent());
        statusComboBox.setSelectedItem(task.getStatus());

        nameField.setEditable(true);
        descField.setEditable(true);
        contentArea.setEditable(true);
        statusComboBox.setEnabled(true);
        saveButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void saveCurrentTask() {
        if (currentSelectedTask == null) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Name can't be empty");
            return;
        }

        currentSelectedTask.setName(name);
        currentSelectedTask.setDescription(descField.getText().trim());
        currentSelectedTask.setContent(contentArea.getText().trim());
        currentSelectedTask.setStatus((TaskStatus) statusComboBox.getSelectedItem());

        repository.update(currentSelectedTask);
        loadTasks();
    }

    private void deleteCurrentTask() {
        if (currentSelectedTask == null) return;
        int confirm = JOptionPane.showConfirmDialog(frame, "Delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            repository.remove(currentSelectedTask.getId());
            currentSelectedTask = null;
            loadTasks();
        }
    }

    private JPanel createMessagePanel(Color bgColor, Color textColor, Color buttonColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createTitledBorder("Messages"));

        groupMessagesArea = new JTextArea();
        groupMessagesArea.setEditable(false);
        groupMessagesArea.setBackground(bgColor);

        myMessagesArea = new JTextArea(3, 30);
        JScrollPane scroll = new JScrollPane(groupMessagesArea);

        sendButton = createRoundedButton("Send", buttonColor);
        sendButton.addActionListener(e -> sendMessage());

        JPanel input = new JPanel(new BorderLayout());
        input.add(new JScrollPane(myMessagesArea), BorderLayout.CENTER);
        input.add(sendButton, BorderLayout.EAST);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(input, BorderLayout.SOUTH);
        return panel;
    }

    private void sendMessage() {
        String text = myMessagesArea.getText().trim();
        if (text.isEmpty()) return;

        Message msg = messageService.sendMessage(username, text);
        displayMessage(msg);
        myMessagesArea.setText("");
    }

    private void loadGroupMessages() {
        groupMessagesArea.setText("");
        List<Message> messages = messageService.getAllMessages();
        for (Message msg : messages) displayMessage(msg);
    }

    private void displayMessage(Message msg) {
        groupMessagesArea.append(String.format("[%s] %s: %s\n",
                msg.getFormattedTimestamp(), msg.getUsername(), msg.getContent()));
    }

    private void startMessageRefreshTimer() {
        messageRefreshTimer = new Timer(2000, e -> checkForNewMessages());
        messageRefreshTimer.start();
    }

    private void checkForNewMessages() {
        Message latest = messageService.getLatestMessage();
        if (latest != null && (lastDisplayedMessageTime == null || latest.getTimestamp().isAfter(lastDisplayedMessageTime))) {
            loadGroupMessages();
            lastDisplayedMessageTime = latest.getTimestamp();
        }
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD2A8FF), 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        field.setBackground(new Color(0xF7F3FF));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2, true));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private JPanel createLabeledPanel(String labelText, Component comp, Color bg, Color fg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        JLabel label = new JLabel(labelText);
        label.setForeground(fg);
        panel.add(label, BorderLayout.WEST);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }

    // Рендер для статусів у комбобоксі
    private static class StatusRenderer extends JLabel implements ListCellRenderer<TaskStatus> {
        public StatusRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TaskStatus> list, TaskStatus value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.getDisplayName());
            switch (value) {
                case NOT_STARTED -> setBackground(new Color(0xF1948A));
                case IN_PROGRESS -> setBackground(new Color(0x85C1E9));
                case FINISHED -> setBackground(new Color(0x82E0AA));
            }
            return this;
        }
    }

    // colors with status
    private static class TaskRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TodoTask task) {
                label.setText(task.getName() + " [" + task.getStatus().getDisplayName() + "]");
                switch (task.getStatus()) {
                    case NOT_STARTED -> label.setForeground(new Color(255, 100, 100));
                    case IN_PROGRESS -> label.setForeground(new Color(100, 150, 255));
                    case FINISHED -> label.setForeground(new Color(100, 220, 100));
                }
            }
            return label;
        }
    }
}
