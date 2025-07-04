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

    public TodoSplitApp(IRepository repository, IUserRepository userRepository) {
        this.repository = repository;
        this.username = repository.getCurrentUser();
        this.userRepository = userRepository;
        initUI();
    }

    private void initUI() {
        JFrame frame = new JFrame("Todo App — user: " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
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

        frame.add(splitPane, BorderLayout.CENTER);

        loadTasks();

        frame.setVisible(true);
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
