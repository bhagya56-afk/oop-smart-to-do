package app;

import models.Student;
import models.Task;
import models.Task.Priority;
import models.TaskManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DashboardPanel extends JPanel implements ActionListener {

    private final SmartTaskGUI controller;
    private final TaskManager taskManager;
    private final Student currentStudent;

    private String currentCategoryFilter = TaskManager.CATEGORIES[0];
    private String currentPriorityFilter = TaskManager.PRIORITY_FILTERS[0];

    private JButton addTaskButton;
    private JButton deleteTaskButton;
    private JButton editTaskButton;
    private JButton completeTaskButton;
    private JComboBox<String> categoryFilterBox;
    private JComboBox<String> priorityFilterBox;
    private JButton logoutButton;

    private JList<Task> taskList;
    private DefaultListModel<Task> listModel;
    private JLabel statsLabel;

    // --- UX/UI: Beautiful Colors ---
    private final Color PRIMARY_ACCENT = new Color(79, 170, 185); // Soft Teal/Blue
    private final Color LIGHT_BG = new Color(245, 245, 245); // Light Gray Background
    private final Color DARK_TEXT = new Color(50, 50, 50);

    public DashboardPanel(SmartTaskGUI controller, TaskManager taskManager, Student currentStudent) {
        this.controller = controller;
        this.taskManager = taskManager;
        this.currentStudent = currentStudent;

        setBackground(LIGHT_BG);
        setLayout(new BorderLayout(15, 15)); // Increased general gap
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Increased overall padding

        // --- 1. Top Area (Header, Filters, Buttons) ---

        // Welcome Header
        JLabel welcomeLabel = new JLabel("SmartTask: Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(DARK_TEXT);

        JLabel studentNameLabel = new JLabel("Welcome, " + currentStudent.getFirstName() + "!");
        studentNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        studentNameLabel.setForeground(DARK_TEXT);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(welcomeLabel);
        titlePanel.add(studentNameLabel);


        // Action Buttons Row (Right side)
        addTaskButton = createStyledButton("Add Task 📝");
        deleteTaskButton = createStyledButton("Delete 🗑️");
        editTaskButton = createStyledButton("Edit Task ✏️"); // NEW BUTTON
        editTaskButton.addActionListener(this);
        completeTaskButton = createStyledButton("Complete ✔️");
        logoutButton = createStyledButton("Logout 🚪");

        addTaskButton.addActionListener(this);
        deleteTaskButton.addActionListener(this);
        completeTaskButton.addActionListener(this);
        logoutButton.addActionListener(this);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.add(addTaskButton);
        actionButtonPanel.add(completeTaskButton);
        actionButtonPanel.add(deleteTaskButton);
        actionButtonPanel.add(editTaskButton);
        actionButtonPanel.add(logoutButton);

        // Filter Controls Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10)); // Increased horizontal gap
        filterPanel.setOpaque(false);
        filterPanel.add(createFilterLabel("Category:"));
        categoryFilterBox = createStyledComboBox(TaskManager.CATEGORIES);
        categoryFilterBox.addActionListener(this);
        filterPanel.add(categoryFilterBox);

        filterPanel.add(createFilterLabel("Priority:"));
        priorityFilterBox = createStyledComboBox(TaskManager.PRIORITY_FILTERS);
        priorityFilterBox.addActionListener(this);
        filterPanel.add(priorityFilterBox);

        // Combine Top Elements
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(titlePanel, BorderLayout.WEST);
        headerRow.add(actionButtonPanel, BorderLayout.EAST);

        // Separator for the filters
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        topContainer.add(headerRow, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);


        // --- 2. Center Area (Task List) ---
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setFixedCellHeight(70); // Slightly taller for more visual appeal

        JScrollPane scrollPane = new JScrollPane(taskList);
        // UX Improvement: Add a raised border/shadow effect to the list panel
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Bottom Area (Stats) ---
        statsLabel = new JLabel("Loading stats...");
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statsLabel.setForeground(DARK_TEXT);
        statsLabel.setBorder(new EmptyBorder(10, 5, 0, 0));
        add(statsLabel, BorderLayout.SOUTH);

        loadTasks();
    }

    // --- UX/UI Helper Methods ---

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        // Add a padding border to make buttons look bigger
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_ACCENT.darker(), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Arial", Font.PLAIN, 12));
        box.setBackground(Color.WHITE);
        return box;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(DARK_TEXT);
        return label;
    }


    // --- Custom Renderer for Enhanced Design ---
    private class TaskCellRenderer extends JPanel implements ListCellRenderer<Task> {

        // Softer, more pleasing color tones for backgrounds
        private final Color HIGH_COLOR = new Color(255, 235, 235); // Very soft red
        private final Color MEDIUM_COLOR = new Color(255, 255, 215); // Very soft yellow
        private final Color LOW_COLOR = new Color(215, 255, 215); // Very soft green
        private final Color COMPLETED_COLOR = new Color(230, 230, 230); // Lighter gray

        private JLabel titleLabel;
        private JLabel detailsLabel;
        private JPanel statusPanel;

        public TaskCellRenderer() {
            setLayout(new BorderLayout(10, 0)); // Increased horizontal gap
            setBorder(new EmptyBorder(10, 15, 10, 15)); // Increased padding

            // Title (North)
            titleLabel = new JLabel();
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Slightly larger title
            add(titleLabel, BorderLayout.NORTH);

            // Details (Center)
            detailsLabel = new JLabel();
            detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            detailsLabel.setForeground(new Color(100, 100, 100)); // Lighter gray for secondary text
            add(detailsLabel, BorderLayout.CENTER);

            // Status Panel (East)
            statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            statusPanel.setOpaque(false);
            add(statusPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            // 1. Set Background Color
            Color background;
            if (task.isCompleted()) {
                background = COMPLETED_COLOR;
            } else if (task.getPriority() == Priority.HIGH) {
                background = HIGH_COLOR;
            } else if (task.getPriority() == Priority.MEDIUM) {
                background = MEDIUM_COLOR;
            } else {
                background = LOW_COLOR;
            }

            if (isSelected) {
                // Use primary accent color for selection, but lighter
                background = PRIMARY_ACCENT.brighter();
            }

            setBackground(background);

            // 2. Set Text and Status
            String titleText = task.getTitle();
            String dueDateStr = task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));

            statusPanel.removeAll();

            Color priorityDotColor;
            if (task.getPriority() == Priority.HIGH) priorityDotColor = new Color(220, 0, 0);
            else if (task.getPriority() == Priority.MEDIUM) priorityDotColor = new Color(200, 100, 0);
            else priorityDotColor = new Color(0, 150, 0);

            if (!task.isCompleted()) {
                statusPanel.add(createColoredDot(priorityDotColor));
            }

            JLabel statusLabel = new JLabel();
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));

            if (task.isCompleted()) {
                titleLabel.setText("<html><strike>" + titleText + "</strike></html>");
                statusLabel.setForeground(PRIMARY_ACCENT.darker());
                statusLabel.setText("DONE");
            } else {
                titleLabel.setText(titleText);

                // Priority/Due Status
                if (task.isOverdue()) {
                    statusLabel.setForeground(new Color(180, 0, 0));
                    statusLabel.setText("OVERDUE");
                } else if (task.isDueToday()) {
                    statusLabel.setForeground(new Color(0, 100, 0));
                    statusLabel.setText("TODAY"); // Shorter text
                } else {
                    statusLabel.setForeground(DARK_TEXT);
                    statusLabel.setText(task.getPriority().getValue().toUpperCase());
                }
            }

            statusPanel.add(statusLabel);

            detailsLabel.setText(String.format("Category: %s | Due: %s", task.getCategory(), dueDateStr));

            // Set final foreground colors
            Color titleForeground = (task.isCompleted() && !isSelected) ? Color.GRAY : (isSelected ? Color.WHITE : DARK_TEXT);
            Color detailForeground = (isSelected) ? Color.WHITE : new Color(100, 100, 100);

            titleLabel.setForeground(titleForeground);
            detailsLabel.setForeground(detailForeground);

            // Set borders/padding
            if (!isSelected) {
                Border lineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220));
                setBorder(BorderFactory.createCompoundBorder(lineBorder, new EmptyBorder(10, 15, 10, 15)));
            } else {
                setBorder(new EmptyBorder(10, 15, 10, 15));
            }

            statusPanel.setBackground(background);

            return this;
        }

        private JPanel createColoredDot(Color color) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Use anti-aliasing for smooth circles
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(color);
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                }
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(10, 10); // Slightly larger dot
                }
            };
            dot.setOpaque(false);
            return dot;
        }
    }

    // --- Action and Logic Methods (Unchanged Logic, but cleaner code structure) ---

    private void loadTasks() {
        listModel.clear();
        List<Task> studentTasks = taskManager.getTasksByStudent(currentStudent.getEmail(),
                currentCategoryFilter,
                currentPriorityFilter);
        for (Task task : studentTasks) {
            listModel.addElement(task);
        }
        updateStats();
    }

    private void updateStats() {
        TaskManager.TaskStats stats = taskManager.getTaskStats(currentStudent.getEmail());
        statsLabel.setText(String.format(
                "Total Tasks: %d | Pending: %d | Completed: %d | Due Today: %d | Overdue: %d",
                stats.getTotal(), stats.getPending(), stats.getCompleted(), stats.getDueToday(), stats.getOverdue()
        ));
    }

    private void showAddTaskDialog() {
        JTextField titleField = new JTextField(20);
        JTextField descField = new JTextField(20);

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Lab", "Study", "Personal", "Assignment", "Project"});
        categoryBox.setSelectedItem(currentCategoryFilter.equals("All") ? "Personal" : currentCategoryFilter);

        JComboBox<Task.Priority> priorityBox = new JComboBox<>(Task.Priority.values());
        if (!currentPriorityFilter.equals("All")) {
            try {
                priorityBox.setSelectedItem(Task.Priority.fromString(currentPriorityFilter));
            } catch (Exception ignored) { }
        }

        JTextField dueDateField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 10)); // Increased vertical gap
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Category:")); panel.add(categoryBox);
        panel.add(new JLabel("Priority:")); panel.add(priorityBox);
        panel.add(new JLabel("Due Date (YYYY-MM-DD HH:MM):")); panel.add(dueDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                String desc = descField.getText().trim();
                String category = (String) categoryBox.getSelectedItem();
                Task.Priority priority = (Task.Priority) priorityBox.getSelectedItem();
                LocalDateTime dueDate = LocalDateTime.parse(dueDateField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                taskManager.addTask(title, desc, category, priority, dueDate, currentStudent.getEmail());
                loadTasks();

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid Date/Time format. Use YYYY-MM-DD HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Add this new method inside app/DashboardPanel.java

    private void showEditTaskDialog() {
        Task selectedTask = taskList.getSelectedValue();

        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- 1. Set up dialog fields, pre-filled with existing data ---
        JTextField titleField = new JTextField(selectedTask.getTitle(), 20);
        JTextField descField = new JTextField(selectedTask.getDescription(), 20);

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Lab", "Study", "Personal", "Assignment", "Project"});
        categoryBox.setSelectedItem(selectedTask.getCategory());

        JComboBox<Task.Priority> priorityBox = new JComboBox<>(Task.Priority.values());
        priorityBox.setSelectedItem(selectedTask.getPriority()); // Pre-select current priority

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JTextField dueDateField = new JTextField(selectedTask.getDueDate().format(formatter), 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 10));
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Category:")); panel.add(categoryBox);
        panel.add(new JLabel("Priority:")); panel.add(priorityBox);
        panel.add(new JLabel("Due Date (YYYY-MM-DD HH:MM):")); panel.add(dueDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Task: " + selectedTask.getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newTitle = titleField.getText().trim();
                String newDesc = descField.getText().trim();
                String newCategory = (String) categoryBox.getSelectedItem();
                Task.Priority newPriority = (Task.Priority) priorityBox.getSelectedItem();
                LocalDateTime newDueDate = LocalDateTime.parse(dueDateField.getText().trim(), formatter);

                if (newTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // --- 2. Call the updateTask logic you added ---
                boolean success = taskManager.updateTask(
                        selectedTask.getId(),
                        newTitle,
                        newDesc,
                        newCategory,
                        newPriority,
                        newDueDate
                );

                if (success) {
                    loadTasks(); // Reload the list to show updated details
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update task.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid Date/Time format. Use YYYY-MM-DD HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void handleDeleteTask() {
        Task selectedTask = taskList.getSelectedValue();

        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (taskManager.deleteTask(selectedTask.getId())) {
            loadTasks();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete task.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCompleteTask() {
        Task selectedTask = taskList.getSelectedValue();

        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Please select a task to mark complete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedTask.isCompleted()) {
            JOptionPane.showMessageDialog(this, "This task is already marked complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (taskManager.completeTask(selectedTask.getId())) {
            loadTasks();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to mark task complete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    // Inside DashboardPanel.java

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addTaskButton) {
            showAddTaskDialog();
        } else if (e.getSource() == deleteTaskButton) {
            handleDeleteTask();
        } else if (e.getSource() == completeTaskButton) {
            handleCompleteTask();
        } else if (e.getSource() == editTaskButton) { // ADDED
            showEditTaskDialog();
        } else if (e.getSource() == logoutButton) {
            controller.logout();
        } else if (e.getSource() == categoryFilterBox) {
            currentCategoryFilter = (String) categoryFilterBox.getSelectedItem();
            loadTasks();
        } else if (e.getSource() == priorityFilterBox) {
            currentPriorityFilter = (String) priorityFilterBox.getSelectedItem();
            loadTasks();
        }
    }
}