module com.example.edge_power {
        requires javafx.controls;
        requires javafx.fxml;

        requires com.dlsc.formsfx;


    opens com.example.edge_power to javafx.fxml;
        exports com.example.edge_power;
}
