package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.TodoTask;
import services.MessageService;
import models.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TodoSplitApp {
    private final IRepository repository;
    private final MessageService messageService;
    private JList<TodoTask> taskList;
    private DefaultListModel<TodoTask> listModel;

    private JTextField nameField;
    private JTextField descField;
    private JTextArea contentArea;
    private String username;
    private IUserRepository userRepository;

    // Message components
    private JTextArea groupMessagesArea;
    private JTextArea myMessagesArea;
    private JButton sendButton;

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

        // left
        JPanel detailPanel = new JPanel(new BorderLayout());
        JPanel fields = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 fields - name description 

        nameField = new JTextField();
        descField = new JTextField();
        contentArea = new JTextArea(10, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);

        fields.add(new LabeledField("Name:", nameField));
        fields.add(new LabeledField("Description:", descField));

        detailPanel.add(fields, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // right 
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskCellRenderer());

        taskList.addListSelectionListener(e -> {
            TodoTask selected = taskList.getSelectedValue();
            if (selected != null) {
                nameField.setText(selected.getName());
                descField.setText(selected.getDescription());
                contentArea.setText(selected.getContent());
            }
        });

        JScrollPane listScroll = new JScrollPane(taskList);

        // buttoms 
        JButton addButton = new JButton("Make new task ");
        addButton.addActionListener(e -> {
            new CreateTaskDialog(frame, repository, this::loadTasks).setVisible(true);
        });

        JButton logoutButton = new JButton("Exit");
        logoutButton.addActionListener(e -> {
            frame.dispose(); // close main window 
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

        loadTasks();
        loadGroupMessages();

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

            for (Message message : messages) {
                displayMessage(message);
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
                value = task.getName();
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
            field.setEditable(false);
        }
    }
}
