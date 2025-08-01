package com.example.edge_power;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    private Process dataCollectionProcess;
    private Process mlModelProcess;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showMainApplication();
    }

    public static void showMainApplication() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1230, 680);

        try {
            primaryStage.getIcons().add(
                    new Image(
                            HelloApplication.class.getResourceAsStream("icon.jpg")
                    )
            );
        } catch (Exception e) {
            System.out.println("Error loading icon: " + e.getMessage());
        }

        primaryStage.setTitle("Edge Power");
        primaryStage.setScene(scene);

        // Use relative paths for the Python scripts
        String dataCollectionScriptPath = Paths.get("src", "main", "Servers", "DataCollectio.py").toAbsolutePath().toString();
        String mlModelScriptPath = Paths.get("src", "main", "Servers", "ML_Model.py").toAbsolutePath().toString();

        HelloApplication app = new HelloApplication();
        app.dataCollectionProcess = app.startPythonScript(dataCollectionScriptPath);
        app.mlModelProcess = app.startPythonScript(mlModelScriptPath);
        primaryStage.show();

        // Add shutdown hook to ensure processes are destroyed on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (app.dataCollectionProcess != null) {
                app.dataCollectionProcess.destroy();
            }
            if (app.mlModelProcess != null) {
                app.mlModelProcess.destroy();
            }
        }));
    }

    // Helper method to start Python scripts
    private Process startPythonScript(String scriptPath) {
        try {
            // Specify the full path to the Python executable if necessary
            String pythonPath = "python"; // or "python3" depending on your system
            ProcessBuilder builder = new ProcessBuilder(pythonPath, scriptPath);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Capture and print the script output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}