package models;

import java.time.LocalDateTime;

/**
 * Student - Represents a student user in the system
 * Demonstrates OOP principles: Inheritance and Encapsulation
 */
public class Student extends User {
    // ONLY student-specific fields
    private String studentId;
    private String major;

    public Student() {
        super(); // Call parent constructor
    }

    public Student(String email, String firstName, String lastName,
                   String studentId, String major, String hashedPassword) {
        super(email, firstName, lastName, hashedPassword); // Call parent constructor
        this.studentId = studentId;
        this.major = major;
    }

    // Student-specific getters and setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    // =======================================================
    // FIX: ADD THE REQUIRED METHOD
    // This method is necessary to satisfy the call in AuthManager.java
    // It relies on the setLastLoginAt() method being available (likely in the parent User class)
    // =======================================================
    public void updateLastLogin() {
        this.setLastLoginAt(LocalDateTime.now());
    }

    // Override abstract methods from User
    @Override
    public String toJson() {
        return String.format(
                "{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"," +
                        "\"studentId\":\"%s\",\"major\":\"%s\",\"createdAt\":\"%s\"," +
                        "\"lastLoginAt\":%s,\"isActive\":%b}",
                email, firstName, lastName, studentId, major,
                createdAt.toString(),
                lastLoginAt != null ? "\"" + lastLoginAt.toString() + "\"" : "null",
                isActive
        );
    }

    @Override
    public String toFileString() {
        return String.join("|",
                email != null ? email : "",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                studentId != null ? studentId : "",
                major != null ? major : "",
                hashedPassword != null ? hashedPassword : "",
                createdAt.toString(),
                lastLoginAt != null ? lastLoginAt.toString() : "null",
                String.valueOf(isActive)
        );
    }

    // Parse from file
    public static Student fromFileString(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 9) {
                Student student = new Student();
                student.setEmail(parts[0]);
                student.setFirstName(parts[1]);
                student.setLastName(parts[2]);
                student.setStudentId(parts[3]);
                student.setMajor(parts[4]);
                student.setHashedPassword(parts[5]);
                student.setCreatedAt(LocalDateTime.parse(parts[6]));
                if (!parts[7].equals("null")) {
                    student.setLastLoginAt(LocalDateTime.parse(parts[7]));
                }
                student.setActive(Boolean.parseBoolean(parts[8]));
                return student;
            }
        } catch (Exception e) {
            System.err.println("Error parsing student: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Student{email='%s', name='%s %s', studentId='%s', major='%s', active=%b}",
                email, firstName, lastName, studentId, major, isActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return email != null && email.equals(student.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}