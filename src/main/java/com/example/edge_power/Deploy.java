package com.example.edge_power;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.stage.FileChooser;


public class Deploy {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button downloadButton;
    @FXML
    private Pane contentPane;

    @FXML
    private void handleMLButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ML.fxml"));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }



    @FXML
    private void handleDownloadButton(ActionEvent event) {
        try {
            File sourceFile = new File("model.h");

            // Create a FileChooser for selecting the destination to save the ZIP file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save ZIP File");
            fileChooser.setInitialFileName("model.zip");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
            File destinationFile = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

            if (destinationFile != null) {
                FileOutputStream fos = new FileOutputStream(destinationFile);
                ZipOutputStream zos = new ZipOutputStream(fos);

                // Create a ZipEntry for the source file
                ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
                zos.putNextEntry(zipEntry);

                // Write the content of the source file to the ZIP output stream
                FileInputStream fis = new FileInputStream(sourceFile);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                fis.close();

                zos.close();
                fos.close();

                // Optionally, you can provide feedback to the user
                System.out.println("File successfully zipped and saved to: " + destinationFile.getAbsolutePath());
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
