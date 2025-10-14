package app;

import auth.AuthManager;
import models.Student;
import models.TaskManager;

import javax.swing.*;
import java.awt.*;

public class SmartTaskGUI {

    private final AuthManager authManager;
    private final TaskManager taskManager;
    private Student currentStudent;

    private JFrame mainFrame;

    public SmartTaskGUI() {
        // Initialize backend (managers handle their own file loading)
        this.authManager = new AuthManager();
        this.taskManager = new TaskManager();

        // 1. Setup the Main Window (JFrame)
        mainFrame = new JFrame("SmartTask To-Do App");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null); // Center the window

        // 2. Start by showing the Login/Registration view
        showLoginView();

        mainFrame.setVisible(true);
    }

    public void showLoginView() {
        mainFrame.getContentPane().removeAll();
        LoginPanel loginPanel = new LoginPanel(this, authManager);

        mainFrame.setTitle("SmartTask - Login/Registration");
        mainFrame.add(loginPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    public void showDashboardView(Student student) {
        this.currentStudent = student;
        mainFrame.getContentPane().removeAll();

        DashboardPanel dashboardPanel = new DashboardPanel(this, taskManager, currentStudent);

        mainFrame.setTitle("SmartTask - Dashboard | Welcome, " + student.getFirstName());
        mainFrame.add(dashboardPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    public void logout() {
        this.currentStudent = null;
        showLoginView();
    }

    // --- Main Method to start the Swing Application ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartTaskGUI::new);
    }
}