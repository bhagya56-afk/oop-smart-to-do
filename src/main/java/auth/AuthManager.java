package auth;

import models.Student;
import utils.FileHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AuthManager - Handles all user (Student) authentication logic,
 * including registration, login, and persistence of user data to a file.
 */
public class AuthManager {
    private List<Student> students;
    // Changed to 'final' to address the IDE warning
    private final FileHandler fileHandler;
    private static final String STUDENTS_FILE = "students.txt";

    public AuthManager() {
        // fileHandler must be initialized here since it's final
        this.fileHandler = new FileHandler();
        this.students = new ArrayList<>();
        loadStudents();
    }

    /**
     * Loads student data from the file system.
     */
    public void loadStudents() {
        try {
            // NOTE: This call now throws IOException (must be implemented in FileHandler)
            List<String> lines = fileHandler.readFile(STUDENTS_FILE);
            this.students = lines.stream()
                    .map(Student::fromFileString)
                    // Changed lambda to method reference to address IDE warning
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // The try-catch block is now correct because FileHandler throws IOException
            System.err.println("Error loading student data: " + e.getMessage());
            this.students = new ArrayList<>();
        }
    }

    /**
     * Saves all current student data to the file system.
     */
    private void saveStudents() {
        try {
            List<String> lines = students.stream()
                    .map(Student::toFileString)
                    .collect(Collectors.toList());
            // NOTE: This call now throws IOException (must be implemented in FileHandler)
            fileHandler.writeFile(STUDENTS_FILE, lines);
        } catch (IOException e) {
            System.err.println("Error saving student data: " + e.getMessage());
        }
    }

    // A placeholder for a simple password hash (in a real app, use BCrypt)
    private String hashPassword(String password) {
        // Simple shift/scramble for illustration in a Java-only app
        return new StringBuilder(password).reverse().toString();
    }

    /**
     * Registers a new student and saves data.
     * @param firstName Student's first name.
     * @param lastName Student's last name.
     * @param email Student's email (used as unique username).
     * @param studentId Student's ID number.
     * @param major Student's major.
     * @param password Student's chosen password (unhashed).
     * @return true if registration was successful, false if email already exists.
     */
    public boolean register(String firstName, String lastName, String email,
                            String studentId, String major, String password) {
        if (emailExists(email)) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        Student newStudent = new Student(email, firstName, lastName, studentId, major, hashedPassword);
        students.add(newStudent);
        saveStudents();
        return true;
    }

    /**
     * Logs in a user by checking credentials.
     * @param email The user's email.
     * @param password The user's password.
     * @return The Student object if login is successful, or null otherwise.
     */
    public Student login(String email, String password) {
        String hashedPasswordAttempt = hashPassword(password);

        for (Student student : students) {
            if (student.getEmail().equalsIgnoreCase(email) &&
                    student.getHashedPassword().equals(hashedPasswordAttempt)) {

                // This method must exist in the User/Student class (which we added in the previous step)
                student.updateLastLogin();
                saveStudents();
                return student;
            }
        }
        return null;
    }

    /**
     * Checks if an email is already in use.
     * @param email The email to check.
     * @return true if the email is found in the student list, false otherwise.
     */
    public boolean emailExists(String email) {
        return students.stream()
                .anyMatch(s -> s.getEmail().equalsIgnoreCase(email));
    }

    // --- Static Utility Methods ---

    /**
     * Validates an email format using a basic regular expression.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Validates password length.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
