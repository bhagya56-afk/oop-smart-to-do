package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class Task {
    private int id;
    private String studentEmail;
    private String title;
    private String description;
    private String category;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private boolean isCompleted;

    public enum Priority {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low");

        private final String value;

        Priority(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Priority fromString(String text) {
            for (Priority p : Priority.values()) {
                if (p.value.equalsIgnoreCase(text)) {
                    return p;
                }
            }
            return MEDIUM; // Default to medium if invalid input
        }
    }

    // Constructor for creating a NEW task
    public Task(int id, String studentEmail, String title, String description,
                String category, Priority priority, LocalDateTime dueDate) {
        this.id = id;
        this.studentEmail = studentEmail;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.dueDate = dueDate;
        this.isCompleted = false;
    }

    // Constructor used by fromFileString
    public Task() {}

    /**
     * Checks if the task is past its due date and not completed.
     */
    public boolean isOverdue() {
        return !isCompleted && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Checks if the task is due today.
     */
    public boolean isDueToday() {
        return !isCompleted &&
                dueDate.toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
    }

    /**
     * Converts the Task object to a pipe-separated string for file storage.
     * Format: ID|Email|Title|Description|Category|Priority|CreatedAt|DueDate|IsCompleted
     */
    public String toFileString() {
        return String.join("|",
                String.valueOf(id),
                studentEmail,
                title,
                description.replace("|", ""), // Remove pipes from description
                category,
                priority.getValue(),
                createdAt.toString(),
                dueDate.toString(),
                String.valueOf(isCompleted)
        );
    }

    /**
     * Creates a Task object from a file string line.
     */
    public static Task fromFileString(String line) {
        try {
            String[] parts = line.split("\\|", -1); // Split with limit to handle empty trailing values
            if (parts.length >= 9) {
                Task task = new Task();
                task.setId(Integer.parseInt(parts[0]));
                task.setStudentEmail(parts[1]);
                task.setTitle(parts[2]);
                task.setDescription(parts[3]);
                task.setCategory(parts[4]);
                task.setPriority(Priority.fromString(parts[5]));
                task.setCreatedAt(LocalDateTime.parse(parts[6]));
                task.setDueDate(LocalDateTime.parse(parts[7]));
                task.setCompleted(Boolean.parseBoolean(parts[8]));
                return task;
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Error parsing task line: " + e.getMessage() + " -> " + line);
        }
        return null;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}