package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.TodoTask;
import dal.TaskStatus;
import services.MessageService;
import models.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class TodoSplitApp {
    private final IRepository repository;
    private final MessageService messageService;
    private JList<TodoTask> taskList;
    private DefaultListModel<TodoTask> listModel;

    private JTextField nameField;
    private JTextField descField;
    private JTextArea contentArea;
    private JComboBox<TaskStatus> statusComboBox; // NEW: Status dropdown
    private JButton saveButton; // NEW: Save button for editing
    private TodoTask currentSelectedTask; // NEW: Track selected task
    private String username;
    private IUserRepository userRepository;

    // Message components
    private JTextArea groupMessagesArea;
    private JTextArea myMessagesArea;
    private JButton sendButton;

    // Timer for auto-refresh
    private Timer messageRefreshTimer;
    private LocalDateTime lastDisplayedMessageTime;

    public TodoSplitApp(IRepository repository, IUserRepository userRepository, MessageService messageService) {
        this.repository = repository;
        this.username = repository.getCurrentUser();
        this.userRepository = userRepository;
        this.messageService = messageService;
        initUI();
    }

    private void initUI() {

        JFrame frame = new JFrame("Todo App — user: " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 650);
        frame.setLocationRelativeTo(null);

        // note
        JLabel userLabel = new JLabel("You are logged in as: " + username);
        frame.add(userLabel, BorderLayout.SOUTH);

        // left panel components
        nameField = new JTextField();
        descField = new JTextField();
        nameField.setEditable(false);
        descField.setEditable(false);

        statusComboBox = new JComboBox<>(TaskStatus.values());
        contentArea = new JTextArea(10, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);

        // Create save button
        saveButton = new JButton("Save Changes");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveCurrentTask());

        // right panel components
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskCellRenderer());

        // left panel layout
        JPanel detailPanel = new JPanel(new BorderLayout());
        JPanel fields = new JPanel(new GridLayout(3, 1, 5, 5));

        // Create individual labeled panels
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.add(new JLabel("Name:"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);

        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.add(new JLabel("Description:"), BorderLayout.WEST);
        descPanel.add(descField, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.add(new JLabel("Status:"), BorderLayout.WEST);
        statusPanel.add(statusComboBox, BorderLayout.CENTER);

        fields.add(namePanel);
        fields.add(descPanel);
        fields.add(statusPanel);

        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        savePanel.add(saveButton);

        detailPanel.add(fields, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        detailPanel.add(savePanel, BorderLayout.SOUTH);

        JScrollPane listScroll = new JScrollPane(taskList);

        // buttons
        JButton addButton = new JButton("Make new task ");
        addButton.addActionListener(e -> {
            new CreateTaskDialog(frame, repository, this::loadTasks).setVisible(true);
        });

        JButton logoutButton = new JButton("Exit");
        logoutButton.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(userRepository).setVisible(true));
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(addButton);
        topPanel.add(logoutButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // between
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailPanel, listScroll);
        splitPane.setDividerLocation(500);

        // Create messaging section
        JPanel messagingSection = createMessagingSection();

        // Create vertical split pane to separate main content from messaging
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, messagingSection);
        verticalSplitPane.setDividerLocation(400);
        verticalSplitPane.setResizeWeight(0.7);

        frame.add(verticalSplitPane, BorderLayout.CENTER);

        taskList.addListSelectionListener(e -> {

            TodoTask selected = taskList.getSelectedValue();
            if (selected != null) {
                currentSelectedTask = selected;
                nameField.setText(selected.getName());
                descField.setText(selected.getDescription());
                contentArea.setText(selected.getContent());

                if (statusComboBox != null) {
                    statusComboBox.setSelectedItem(selected.getStatus());
                }

                // Enable editing
                nameField.setEditable(true);
                descField.setEditable(true);
                contentArea.setEditable(true);
                saveButton.setEnabled(true);
            } else {
                currentSelectedTask = null;
                clearDetailFields();
                if (saveButton != null) {
                    saveButton.setEnabled(false);
                }
            }
        });

        loadTasks();
        loadGroupMessages();
        startMessageRefreshTimer();

        frame.setVisible(true);
    }

    // Create the messaging section with Group Messages and My Messages panels
    private JPanel createMessagingSection() {
        JPanel messagingPanel = new JPanel(new BorderLayout());
        messagingPanel.setBorder(BorderFactory.createTitledBorder("Interactive Messages"));

        // Create Group Messages panel (upper panel)
        JPanel groupMessagesPanel = new JPanel(new BorderLayout());
        groupMessagesPanel.setBorder(BorderFactory.createTitledBorder("Group Messages"));

        groupMessagesArea = new JTextArea(10, 30);
        groupMessagesArea.setEditable(false);
        groupMessagesArea.setLineWrap(true);
        groupMessagesArea.setWrapStyleWord(true);
        groupMessagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane groupScrollPane = new JScrollPane(groupMessagesArea);
        groupScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        groupScrollPane.setPreferredSize(new Dimension(0, 200));

        groupMessagesPanel.add(groupScrollPane, BorderLayout.CENTER);

        // Create My Messages panel (lower panel)
        JPanel myMessagesPanel = new JPanel(new BorderLayout());
        myMessagesPanel.setBorder(BorderFactory.createTitledBorder("My Messages"));

        myMessagesArea = new JTextArea(2, 30);
        myMessagesArea.setLineWrap(true);
        myMessagesArea.setWrapStyleWord(true);
        myMessagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane myScrollPane = new JScrollPane(myMessagesArea);
        myScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        // Create panel for text area and send button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(myScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        myMessagesPanel.add(inputPanel, BorderLayout.CENTER);

        // Combine both panels vertically
        JSplitPane messagesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, groupMessagesPanel, myMessagesPanel);
        messagesSplitPane.setDividerLocation(200);
        messagesSplitPane.setResizeWeight(0.8);

        messagingPanel.add(messagesSplitPane, BorderLayout.CENTER);

        return messagingPanel;
    }

    // Handle sending messages - delegates to MessageService
    private void sendMessage() {
        String messageText = myMessagesArea.getText().trim();
        if (!messageText.isEmpty()) {
            try {
                Message message = messageService.sendMessage(username, messageText);

                // Update UI with the sent message
                displayMessage(message);

                // Clear input field
                myMessagesArea.setText("");

                // Scroll to bottom of group messages
                groupMessagesArea.setCaretPosition(groupMessagesArea.getDocument().getLength());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to send message: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Load group messages from MessageService
    private void loadGroupMessages() {
        try {
            List<Message> messages = messageService.getAllMessages();
            groupMessagesArea.setText(""); // Clear existing messages

            lastDisplayedMessageTime = null; // Reset timestamp tracker

            for (Message message : messages) {
                displayMessage(message);
                // Track the latest message timestamp
                if (lastDisplayedMessageTime == null ||
                        message.getTimestamp().isAfter(lastDisplayedMessageTime)) {
                    lastDisplayedMessageTime = message.getTimestamp();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load messages: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Display a single message in the group messages area
    private void displayMessage(Message message) {
        String formattedMessage = String.format("[%s] %s: %s%n",
                message.getFormattedTimestamp(),
                message.getUsername(),
                message.getContent());
        groupMessagesArea.append(formattedMessage);

        // Update the last displayed message time
        if (lastDisplayedMessageTime == null ||
                message.getTimestamp().isAfter(lastDisplayedMessageTime)) {
            lastDisplayedMessageTime = message.getTimestamp();
        }

        // Limit to 100 rows
        limitGroupMessagesRows();
    }

    // Limit group messages to maximum 100 rows
    private void limitGroupMessagesRows() {
        String[] lines = groupMessagesArea.getText().split("\n");
        if (lines.length > 100) {
            StringBuilder limitedText = new StringBuilder();
            for (int i = lines.length - 100; i < lines.length; i++) {
                limitedText.append(lines[i]).append("\n");
            }
            groupMessagesArea.setText(limitedText.toString());
        }
    }

    // NEW: Start the message refresh timer
    private void startMessageRefreshTimer() {
        messageRefreshTimer = new Timer(2000, e -> checkForNewMessages()); // 2 seconds interval
        messageRefreshTimer.start();
    }

    // NEW: Check for new messages and update if needed
    private void checkForNewMessages() {
        try {
            // Get the latest message from the database
            Message latestMessage = messageService.getLatestMessage();

            // If there's no latest message or no displayed messages yet, skip
            if (latestMessage == null) {
                return;
            }

            // Compare timestamps - if DB has newer messages, refresh
            if (lastDisplayedMessageTime == null ||
                    latestMessage.getTimestamp().isAfter(lastDisplayedMessageTime)) {

                // Refresh the messages
                loadGroupMessages();

                // Auto-scroll to bottom to show new messages
                SwingUtilities.invokeLater(() -> {
                    groupMessagesArea.setCaretPosition(groupMessagesArea.getDocument().getLength());
                });
            }

        } catch (Exception ex) {
            // Silently handle errors to avoid disrupting the user experience
            // You could log this error if you have a logging system
            System.err.println("Error checking for new messages: " + ex.getMessage());
        }
    }

    // NEW: Save current task changes
    private void saveCurrentTask() {
        if (currentSelectedTask == null) return;

        try {
            // Get values from fields
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            String content = contentArea.getText().trim();
            TaskStatus status = (TaskStatus) statusComboBox.getSelectedItem();

            // Validate
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Task name cannot be empty.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Update the task
            currentSelectedTask.setName(name);
            currentSelectedTask.setDescription(description);
            currentSelectedTask.setContent(content);
            currentSelectedTask.setStatus(status);

            // Save to database
            repository.update(currentSelectedTask);

            // Refresh the task list to show updated status
            loadTasks();

            // Show success message
            JOptionPane.showMessageDialog(null, "Task updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to save task: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // NEW: Clear detail fields
    private void clearDetailFields() {
        nameField.setText("");
        descField.setText("");
        contentArea.setText("");
        if (statusComboBox != null) { // ADD null check
            statusComboBox.setSelectedItem(TaskStatus.NOT_STARTED);
        }

        // Disable editing
        nameField.setEditable(false);
        descField.setEditable(false);
        contentArea.setEditable(false);
    }

    // NEW: Stop the timer when the window is closed (good practice)
    public void dispose() {
        if (messageRefreshTimer != null) {
            messageRefreshTimer.stop();
        }
    }

    private void loadTasks() {
        List<TodoTask> tasks = repository.findAll();
        listModel.clear();
        tasks.forEach(listModel::addElement);
    }

    // Render for tasks
    private static class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof TodoTask task) {
                // NEW: Use the toString() method which includes status
                value = task.toString(); // This will show "Task Name [Status]"
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    //
    private static class LabeledField extends JPanel {
        public LabeledField(String label, JTextField field) {
            super(new BorderLayout(5, 5));
            add(new JLabel(label), BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
            field.setEditable(false); // Start as non-editable
        }
    }

    // NEW: Separate class for combo box labeled field
    private static class LabeledFieldCombo extends JPanel {
        public LabeledFieldCombo(String label, JComboBox<?> comboBox) {
            super(new BorderLayout(5, 5));
            add(new JLabel(label), BorderLayout.WEST);
            add(comboBox, BorderLayout.CENTER);
        }
    }
}