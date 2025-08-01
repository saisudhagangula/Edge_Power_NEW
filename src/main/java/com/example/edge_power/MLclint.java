package com.example.edge_power;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MLclint {


    @FXML
    private ChoiceBox<String> modelChoiceBox;

    @FXML
    private ChoiceBox<String> parametersChoiceBox;

    @FXML
    private Label welcomeText;
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private AnchorPane contentPane;


    private Map<String, String[]> modelParameters = new HashMap<>();




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
    protected void initialize() {
        // Populate the modelParameters map with available models and their parameters
        modelParameters.put("SVM", new String[]{"linear", "poly", "rbf", "sigmoid"});
        modelParameters.put("RandomForest", new String[]{"5", "10", "15", "20", "25", "30"});
        modelParameters.put("LinearRegression", new String[]{});
        modelParameters.put("LogisticRegression", new String[]{"linear", "poly", "rbf", "sigmoid"});
        modelParameters.put("DecisionTreeRegressor", new String[]{});
        modelParameters.put("DecisionTreeClassifier", new String[]{});
        // Add more models and their parameters as needed

        // Populate the modelChoiceBox with available models
        modelChoiceBox.getItems().addAll(modelParameters.keySet());

        // Listen for changes in model choice and update parametersChoiceBox accordingly
        modelChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateParametersChoiceBox(newValue);
        });
    }

    private void updateParametersChoiceBox(String selectedModel) {
        parametersChoiceBox.getItems().clear();
        if (selectedModel != null && modelParameters.containsKey(selectedModel)) {
            parametersChoiceBox.getItems().addAll(modelParameters.get(selectedModel));
        }
    }

    @FXML
    protected void onStartButtonClick() {

        String model = modelChoiceBox.getValue();
        String parameters = parametersChoiceBox.getValue();

        if (model == null || model.isEmpty()) {
            welcomeText.setText("Please enter all required fields.");
            return;
        }

        Socket socket = null;
        BufferedReader reader = null;

        try {
            socket = new Socket("localhost", 123);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("data" + " " + model + " " + parameters);
            writer.newLine();
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String result = reader.readLine(); // Read the result from the server
            welcomeText.setText(result); // Update the welcomeText label with the result
        } catch (UnknownHostException e) {
            welcomeText.setText("Unknown host error: " + e.getMessage());
        } catch (IOException e) {
            welcomeText.setText("IO error: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                welcomeText.setText("Error closing socket: " + e.getMessage());
            }
        }
    }


}
