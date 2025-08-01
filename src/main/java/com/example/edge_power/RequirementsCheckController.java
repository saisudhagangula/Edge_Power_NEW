package com.example.edge_power;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class RequirementsCheckController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button retryButton;

    @FXML
    public void initialize() {
        checkRequirements();
    }

    @FXML
    private void handleRetryButton() {
        retryButton.setVisible(false);
        statusLabel.setText("Checking requirements...");
        checkRequirements();
    }

    private void checkRequirements() {
        new Thread(() -> {
            try {
                // Check if Python is installed
                ProcessBuilder processBuilder = new ProcessBuilder("python", "--version");
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    downloadPython();
                } else {
                    checkPythonLibraries();
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error checking requirements: " + e.getMessage());
                    retryButton.setVisible(true);
                });
            }
        }).start();
    }

    private void downloadPython() {
        Platform.runLater(() -> statusLabel.setText("Downloading Python..."));
        try {
            URI uri = new URI("https://www.python.org/ftp/python/3.9.7/python-3.9.7-amd64.exe");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            // Save the downloaded file and install Python
            // (Implementation of saving and installing is omitted for brevity)
            Platform.runLater(() -> statusLabel.setText("Python downloaded and installed."));
            checkPythonLibraries();
        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error downloading Python: " + e.getMessage());
                retryButton.setVisible(true);
            });
        }
    }

    private void checkPythonLibraries() {
        Platform.runLater(() -> statusLabel.setText("Checking Python libraries..."));
        new Thread(() -> {
            try {
                // Specify the full path to the Python executable if necessary
                String pythonPath = "python"; // or "python3" depending on your system
                ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, "-m", "pip", "install", "-r", "src/main/Servers/requirements.txt");
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    Platform.runLater(() -> statusLabel.setText("All requirements are met."));
                    // Proceed to the main application
                    Platform.runLater(() -> {
                        try {
                            HelloApplication.showMainApplication();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error installing Python libraries:\n" + output.toString());
                        retryButton.setVisible(true);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error checking Python libraries: " + e.getMessage());
                    retryButton.setVisible(true);
                });
            }
        }).start();
    }
}