package com.example.edge_power;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.stage.FileChooser;



public class HelloController {
    @FXML
    private TextField portArea;

    @FXML
    private ListView<String> fileListView;

    @FXML
    private TextField baudArea;

    @FXML
    private Button downloadButton;


    @FXML
    private TextField labelArea;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button uploadButton;

    @FXML
    private Label fileCountLabel;

    @FXML
    private Button deleteButton;

    private Path dataFolderPath = Paths.get("src/main/Servers/data");
    private WatchService watchService;

    private Socket socket;
    private DataOutputStream out;
    private Timer timer;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public Pane contentPane;

    private final String host = "localhost"; // Change this to the host where the Python server is running
    private final int port = 5555; // Change this to the port the Python server is listening on

    @FXML
    private void initialize() {
        try {
            // Initialize the WatchService and register the directory for changes
            watchService = FileSystems.getDefault().newWatchService();
            dataFolderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            // Start a background thread to continuously watch for changes
            Thread watchThread = new Thread(this::watchDataFolder);
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display initial files and count
        displayFiles();
    }

    @FXML
    private void handleStartButton(ActionEvent event) {
        try {
            // Create socket connection
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());

            // Disable start button and enable stop button
            startButton.setDisable(true);
            stopButton.setDisable(false);

            // Start sending data periodically
            timer = new Timer();
            timer.schedule(new SendDataTask(), 0, 1000); // Send data every 1 second
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStopButton(ActionEvent event) {
        if (timer != null) {
            timer.cancel();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enable start button and disable stop button
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }


    @FXML
    private void handleMLButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ML.fxml"));
        Parent root = loader.load();

        contentPane.getChildren().setAll(root);
    }

    private class SendDataTask extends TimerTask {
        @Override
        public void run() {
            // Send parameters to the server
            String parameters = portArea.getText() + " " +
                                baudArea.getText() + " " +
                                "data" + " " +
                                 labelArea.getText();
            try {
                out.writeUTF(parameters);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle any errors here
            }
        }
    }

    private void displayFiles() {
        File dataFolder = new File("src/main/Servers/data");
        File[] files = dataFolder.listFiles();
        if (files != null) {
            ObservableList<String> fileNames = FXCollections.observableArrayList();
            for (File file : files) {
                fileNames.add(file.getName());
            }
            fileListView.setItems(fileNames);

            // Update the file count label
            int totalFiles = files.length;
            Platform.runLater(() -> fileCountLabel.setText("Total files in data folder: " + totalFiles));
        }
    }



    private void watchDataFolder() {
        try {
            while (true) {
                WatchKey key = watchService.take(); // Wait for events
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        // A new file was created
                        displayFiles();
                    }
                }
                key.reset(); // Reset the key
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        File dataFolder = new File("src/main/Servers/data");
        File[] files = dataFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
            displayFiles(); // Update the file list after deletion
            fileCountLabel.setText("Total files in data folder: 0");
        }
    }

    @FXML
    private void handleUploadButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Upload");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(uploadButton.getScene().getWindow());

        if (selectedFiles != null) {
            try {
                File dataFolder = new File("src/main/Servers/data");
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }

                for (File file : selectedFiles) {
                    Path destination = Paths.get(dataFolder.getAbsolutePath(), file.getName());
                    Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                }

                displayFiles(); // Update the file list after upload
            } catch (IOException e) {
                e.printStackTrace();
                // Handle IO exceptions
            }
        }
    }

    @FXML
    private void handleDownloadButton(ActionEvent event) {
        try {
            File sourceFolder = new File("src/main/Servers/data");

            // Create a FileChooser for selecting the destination to save the ZIP file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save ZIP File");
            fileChooser.setInitialFileName("data.zip");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
            File destinationFile = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

            if (destinationFile != null) {
                FileOutputStream fos = new FileOutputStream(destinationFile);
                ZipOutputStream zos = new ZipOutputStream(fos);

                zipFolder(sourceFolder, sourceFolder.getName(), zos);

                zos.close();
                fos.close();

                // Optionally, you can provide feedback to the user
                System.out.println("Folder successfully zipped and saved to: " + destinationFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO exceptions
        }
    }

    private void zipFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipFolder(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }

            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            fis.close();
        }
    }

}