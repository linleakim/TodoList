package ui;

import dal.IRepository;
import dal.IUserRepository;
import dal.TodoTask;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TodoSplitApp {
    private final IRepository repository;
    private JList<TodoTask> taskList;
    private DefaultListModel<TodoTask> listModel;

    private JTextField nameField;
    private JTextField descField;
    private JTextArea contentArea;
    private String username;
    private IUserRepository userRepository;
    
    // Message components**
    private JTextArea groupMessagesArea;
    private JTextArea myMessagesArea;
    private JButton sendButton;
    private DefaultListModel<String> groupMessagesModel;

    public TodoSplitApp(IRepository repository, IUserRepository userRepository) {
        this.repository = repository;
        this.username = repository.getCurrentUser();
        this.userRepository = userRepository;
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

        // Create messaging section**
        JPanel messagingSection = createMessagingSection();
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, messagingSection);
        verticalSplitPane.setDividerLocation(400); // Adjust as needed
        verticalSplitPane.setResizeWeight(0.7); // Give more space to main content

        frame.add(verticalSplitPane, BorderLayout.CENTER); 

        loadTasks();

        frame.setVisible(true);
    }

    // Create the messaging section with Group Messages and My Messages panels
    private JPanel createMessagingSection() {
        JPanel messagingPanel = new JPanel(new BorderLayout());
        messagingPanel.setBorder(BorderFactory.createTitledBorder("Messaging"));
        
        // Create Group Messages panel (upper panel)
        JPanel groupMessagesPanel = new JPanel(new BorderLayout());
        groupMessagesPanel.setBorder(BorderFactory.createTitledBorder("Group"));
        
        groupMessagesArea = new JTextArea(10, 30);
        groupMessagesArea.setEditable(false);
        groupMessagesArea.setLineWrap(true);
        groupMessagesArea.setWrapStyleWord(true);
        groupMessagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane groupScrollPane = new JScrollPane(groupMessagesArea);
        groupScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        groupScrollPane.setPreferredSize(new Dimension(0, 200)); // 10 visible rows approximately
        
        groupMessagesPanel.add(groupScrollPane, BorderLayout.CENTER);
        
        // Create My Messages panel (lower panel)
        JPanel myMessagesPanel = new JPanel(new BorderLayout());
        myMessagesPanel.setBorder(BorderFactory.createTitledBorder("Me"));
        
        myMessagesArea = new JTextArea(2, 30); // 2 rows height
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
        messagesSplitPane.setResizeWeight(0.8); // Give more space to group messages
        
        messagingPanel.add(messagesSplitPane, BorderLayout.CENTER);
        
        // Load initial group messages (placeholder)
        loadGroupMessages();
        
        return messagingPanel;
    }
    
    // Handle sending messages
    private void sendMessage() {
        String message = myMessagesArea.getText().trim();
        if (!message.isEmpty()) {
            // TODO: Send message to database/server
            System.out.println("Sending message: " + message);
            
            // Add message to group messages (for demonstration)
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String formattedMessage = "[" + timestamp + "] " + username + ": " + message + "\n";
            groupMessagesArea.append(formattedMessage);
            
            // Clear input field
            myMessagesArea.setText("");
            
            // Scroll to bottom of group messages
            groupMessagesArea.setCaretPosition(groupMessagesArea.getDocument().getLength());
        }
    }
    
    // Load group messages from database (placeholder)
    private void loadGroupMessages() {
        // TODO: Load messages from database
        // For now, add some sample messages
        groupMessagesArea.append("[10:30] System: Welcome to the group chat!\n");
        groupMessagesArea.append("[10:31] Alice: Hello everyone!\n");
        groupMessagesArea.append("[10:32] Bob: Good morning!\n");
        
        // Limit to 100 rows (approximate)
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
