package com.example.edge_power;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class home implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            handleHomeButton(); // Call handleHomeButton when initializing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Stage stage;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private void handleMLButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ML.fxml"));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }
    @FXML
    private void handleDepButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Dep.fxml"));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }
    @FXML
    private void handlehello_viewButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }

    @FXML
    private void handleHomeButton() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GettingStarted.fxml"));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }
}
