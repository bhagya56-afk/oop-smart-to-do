package models;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import utils.FileHandler;

public class TaskManager {
    private List<Task> tasks;
    private FileHandler fileHandler;
    private static final String TASKS_FILE = "tasks.txt";
    private AtomicInteger nextId;

    public static final String[] CATEGORIES = {"All", "Lab", "Study", "Personal", "Assignment", "Project"};
    // New: Array of priority strings for the filter dropdown
    public static final String[] PRIORITY_FILTERS = {"All", "High", "Medium", "Low"};

    public TaskManager() {
        this.fileHandler = new FileHandler();
        this.tasks = new ArrayList<>();
        this.nextId = new AtomicInteger(1);
        loadTasks();
    }

    public void loadTasks() {
        try {
            List<String> lines = fileHandler.readFile(TASKS_FILE);
            this.tasks = lines.stream()
                    .map(Task::fromFileString)
                    .filter(t -> t != null)
                    .collect(Collectors.toList());

            this.tasks.stream()
                    .mapToInt(Task::getId)
                    .max()
                    .ifPresent(maxId -> nextId.set(maxId + 1));
        } catch (IOException e) {
            System.err.println("Warning: Could not load tasks from file. Error: " + e.getMessage());
            this.tasks = new ArrayList<>();
        }
    }

    private void saveTasks() {
        List<String> lines = tasks.stream()
                .map(Task::toFileString)
                .collect(Collectors.toList());
        try {
            fileHandler.writeFile(TASKS_FILE, lines);
        } catch (IOException e) {
            System.err.println("Error: Could not save tasks to file. Error: " + e.getMessage());
        }
    }

    public Task addTask(String title, String description, String category,
                        Task.Priority priority, LocalDateTime dueDate, String studentEmail) {

        int newId = nextId.getAndIncrement();
        Task newTask = new Task(newId, studentEmail, title, description, category, priority, dueDate);
        tasks.add(newTask);
        saveTasks();
        return newTask;
    }

    public boolean deleteTask(int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) {
            saveTasks();
        }
        return removed;
    }

    public boolean completeTask(int id) {
        Optional<Task> taskOpt = tasks.stream()
                .filter(t -> t.getId() == id && !t.isCompleted())
                .findFirst();

        if (taskOpt.isPresent()) {
            taskOpt.get().setCompleted(true);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Gets all tasks for a student, with optional filtering by category AND priority.
     */
    public List<Task> getTasksByStudent(String email, String categoryFilter, String priorityFilter) {

        // Convert priority filter string to the Task.Priority enum value for comparison
        Task.Priority requiredPriority = null;
        if (!priorityFilter.equalsIgnoreCase(PRIORITY_FILTERS[0])) { // If not "All"
            try {
                // Task.Priority.fromString handles case-insensitivity
                requiredPriority = Task.Priority.fromString(priorityFilter);
            } catch (IllegalArgumentException e) {
                // Should not happen if PRIORITY_FILTERS are consistent with Priority enum
            }
        }

        final Task.Priority finalRequiredPriority = requiredPriority;

        return tasks.stream()
                .filter(t -> t.getStudentEmail().equalsIgnoreCase(email))
                // Category Filter Logic
                .filter(t -> categoryFilter.equals(CATEGORIES[0]) || t.getCategory().equalsIgnoreCase(categoryFilter))
                // Priority Filter Logic
                .filter(t -> finalRequiredPriority == null || t.getPriority() == finalRequiredPriority)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    // Default call when only email is passed
    public List<Task> getTasksByStudent(String email) {
        return getTasksByStudent(email, CATEGORIES[0], PRIORITY_FILTERS[0]);
    }

    // Overloaded call for backward compatibility (used by TaskStats)
    public List<Task> getTasksByStudent(String email, String categoryFilter) {
        return getTasksByStudent(email, categoryFilter, PRIORITY_FILTERS[0]);
    }

    public TaskStats getTaskStats(String email) {
        // TaskStats still counts ALL tasks for the student, regardless of the current view filters
        List<Task> studentTasks = getTasksByStudent(email);

        int total = studentTasks.size();
        int completed = (int) studentTasks.stream().filter(Task::isCompleted).count();
        int pending = total - completed;
        int overdue = (int) studentTasks.stream().filter(Task::isOverdue).count();
        int dueToday = (int) studentTasks.stream().filter(Task::isDueToday).count();

        return new TaskStats(total, completed, pending, overdue, dueToday);
    }

    public class TaskStats {
        private final int total;
        private final int completed;
        private final int pending;
        private final int overdue;
        private final int dueToday;

        public TaskStats(int total, int completed, int pending, int overdue, int dueToday) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
            this.dueToday = dueToday;
        }

        public int getTotal() { return total; }
        public int getCompleted() { return completed; }
        public int getPending() { return pending; }
        public int getOverdue() { return overdue; }
        public int getDueToday() { return dueToday; }
    }
}