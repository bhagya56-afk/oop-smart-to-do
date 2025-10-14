package models;

import java.time.LocalDateTime;

public abstract class User {
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String hashedPassword;
    protected LocalDateTime createdAt;

    protected LocalDateTime lastLoginAt;
    protected boolean isActive;

    public User(String email, String firstName, String lastName, String hashedPassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = null;
        this.isActive = true;
    }

    public User() {} // Default constructor

    // --- ESSENTIAL PUBLIC GETTERS FOR INHERITED FIELDS ---
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getHashedPassword() { return hashedPassword; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- GETTERS/SETTERS FOR LAST LOGIN AND ACTIVE STATUS ---
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    // --- SETTERS for required fields (e.g., for parsing from file) ---
    // These are often needed by the fromFileString static methods in subclasses
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    // --- ABSTRACT METHODS (MUST BE IMPLEMENTED BY SUBCLASSES) ---
    /** Must be implemented by subclasses for JSON serialization. */
    public abstract String toJson();

    /** Must be implemented by subclasses for file storage. */
    public abstract String toFileString();

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
