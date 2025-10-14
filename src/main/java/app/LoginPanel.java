package app;

import auth.AuthManager;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel implements ActionListener {

    private SmartTaskGUI controller;
    private AuthManager authManager;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;

    public LoginPanel(SmartTaskGUI controller, AuthManager authManager) {
        this.controller = controller;
        this.authManager = authManager;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Components ---

        messageLabel = new JLabel("Enter credentials or Register");
        messageLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(messageLabel, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Buttons
        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(loginButton, gbc);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(registerButton, gbc);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        Student student = authManager.login(email, password);

        if (student != null) {
            messageLabel.setText("Login Successful!");
            messageLabel.setForeground(Color.BLUE);
            controller.showDashboardView(student); // Switch to dashboard
        } else {
            messageLabel.setText("Invalid credentials. Please try again.");
            messageLabel.setForeground(Color.RED);
        }
    }

    private void showRegistrationDialog() {
        // Simple registration dialog (requires user input for all fields)
        JTextField firstName = new JTextField();
        JTextField lastName = new JTextField();
        JTextField email = new JTextField();
        JTextField studentId = new JTextField();
        JTextField major = new JTextField();
        JPasswordField password = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("First Name:")); panel.add(firstName);
        panel.add(new JLabel("Last Name:")); panel.add(lastName);
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Student ID:")); panel.add(studentId);
        panel.add(new JLabel("Major:")); panel.add(major);
        panel.add(new JLabel("Password (min 6):")); panel.add(password);

        int result = JOptionPane.showConfirmDialog(this, panel, "Student Registration",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pass = new String(password.getPassword());
            if (!AuthManager.isValidEmail(email.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Invalid Email Format.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!AuthManager.isValidPassword(pass)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (authManager.register(firstName.getText().trim(), lastName.getText().trim(), email.getText().trim(),
                    studentId.getText().trim(), major.getText().trim(), pass)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Email might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerButton) {
            showRegistrationDialog();
        }
    }
}