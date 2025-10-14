package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * FileHandler - Utility class for reading and writing data to files
 * in the 'data' directory.
 */
public class FileHandler {
    private static final String DATA_DIR = "data";
    private final Path dataDirPath;

    public FileHandler() {
        this.dataDirPath = Paths.get(DATA_DIR);
        // Ensure data directory exists on initialization
        if (!Files.exists(dataDirPath)) {
            try {
                Files.createDirectories(dataDirPath);
                System.out.println("Created data directory: " + DATA_DIR);
            } catch (IOException e) {
                System.err.println("Could not create data directory: " + e.getMessage());
            }
        }
    }

    /**
     * Reads all lines from a file in the data directory.
     * @param filename The name of the file (e.g., "students.txt")
     * @return A list of strings, each representing a line from the file.
     * @throws IOException If an I/O error occurs reading from the file.
     */
    public List<String> readFile(String filename) throws IOException {
        Path filePath = dataDirPath.resolve(filename);
        if (!Files.exists(filePath)) {
            // Return empty list if file doesn't exist yet
            return List.of();
        }
        return Files.readAllLines(filePath);
    }

    /**
     * Writes a list of strings to a file in the data directory.
     * Overwrites the file if it exists.
     * @param filename The name of the file.
     * @param lines The list of strings to write.
     * @throws IOException If an I/O error occurs writing to the file.
     */
    public void writeFile(String filename, List<String> lines) throws IOException {
        Path filePath = dataDirPath.resolve(filename);
        Files.write(filePath, lines);
    }
}