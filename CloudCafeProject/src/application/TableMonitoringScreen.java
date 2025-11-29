package application;

import application.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TableMonitoringScreen {

    private Cafe cafe;

    public TableMonitoringScreen(Cafe cafe) {
        this.cafe = cafe;
    }

    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root-pane");
        root.setAlignment(Pos.TOP_CENTER);

        // --- Header ---
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("⬅ Back");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> stage.close());
        
        Label title = new Label("Table Monitoring");
        title.getStyleClass().add("title-label");
        
        headerBox.getChildren().addAll(backBtn, title);

        // --- Legend ---
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
            createLegendItem("Available", "#4CAF50"), // Green
            createLegendItem("Occupied", "#F44336"),  // Red
            createLegendItem("Reserved", "#FF9800")   // Orange
        );

        // --- MAP AREA ---
        HBox mapContainer = new HBox(40);
        mapContainer.setAlignment(Pos.CENTER);
        mapContainer.setPadding(new Insets(30));

        // 1. VIP AREA
        VBox vipArea = new VBox(15);
        vipArea.getStyleClass().add("area-box");
        vipArea.setAlignment(Pos.CENTER);
        vipArea.getChildren().add(new Label("VIP AREA"));
        vipArea.getChildren().addAll(
            createTableButton("VIP 1"), 
            createTableButton("VIP 2")
        );

        // 2. CENTER AREA
        VBox centerArea = new VBox(15);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.getChildren().add(new Label("Customer Area (4 Pax)"));
        centerArea.getChildren().addAll(
            createTableButton("Table 1"),
            createTableButton("Table 2"),
            createTableButton("Table 3")
        );

        // 3. SIDE AREA
        VBox sideArea = new VBox(15);
        sideArea.setAlignment(Pos.CENTER);
        sideArea.getChildren().add(new Label("Side (2 Pax)"));
        
        FlowPane sideFlow = new FlowPane(10, 10);
        sideFlow.setPrefWrapLength(150);
        sideFlow.setAlignment(Pos.CENTER);
        sideFlow.getChildren().addAll(
            createTableButton("T4"), createTableButton("T5"),
            createTableButton("T6"), createTableButton("T7"), createTableButton("T8")
        );
        sideArea.getChildren().add(sideFlow);

        mapContainer.getChildren().addAll(vipArea, centerArea, sideArea);

        root.getChildren().addAll(headerBox, legend, mapContainer);

        Scene scene = new Scene(root, 1000, 700);
        if(getClass().getResource("application.css") != null)
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Table Monitoring - Milestone 3");
        stage.show();
    }

    private HBox createLegendItem(String name, String colorHex) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER);
        Pane circle = new Pane();
        circle.setPrefSize(15, 15);
        circle.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 50%;");
        Label lbl = new Label(name);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        box.getChildren().addAll(circle, lbl);
        return box;
    }

    private Button createTableButton(String tableId) {
        Table table = cafe.getTableManager().getTable(tableId);
        Button btn = new Button(tableId);
        
        // Apply shape class based on ID
        if (tableId.startsWith("VIP")) btn.getStyleClass().add("table-vip");
        else if (tableId.startsWith("T") && tableId.length() == 2) btn.getStyleClass().add("table-reg-2");
        else btn.getStyleClass().add("table-reg-4");

        btn.getStyleClass().add("table-btn"); // Base style

        // Initial Color Set
        updateButtonColor(btn, table.getStatus());

        // Action: Show Action Dialog
        btn.setOnAction(e -> showActionDialog(table, btn));

        return btn;
    }

    private void updateButtonColor(Button btn, TableStatus status) {
        String color = "";
        switch (status) {
            case AVAILABLE: color = "#4CAF50"; break; // Green
            case OCCUPIED:  color = "#F44336"; break; // Red
            case RESERVED:  color = "#FF9800"; break; // Orange
        }
        // We use setStyle to override the CSS background color dynamically
        btn.setStyle("-fx-background-color: " + color + ";");
    }

    private void showActionDialog(Table table, Button btn) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Manage " + table.getId());
        alert.setHeaderText("Status: " + table.getStatus() + "\nCustomer: " + table.getCurrentCustomer());
        alert.setContentText("Choose an action:");

        ButtonType btnOccupy = new ButtonType("Mark Occupied");
        ButtonType btnFree = new ButtonType("Free Table");
        ButtonType btnReserve = new ButtonType("Reserve");
        ButtonType btnCancel = new ButtonType("Cancel");

        if (table.getStatus() == TableStatus.AVAILABLE) {
            alert.getButtonTypes().setAll(btnOccupy, btnReserve, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnFree, btnCancel);
        }

        alert.showAndWait().ifPresent(type -> {
            if (type == btnOccupy) {
                table.occupy("Walk-in");
            } else if (type == btnReserve) {
                // Simple mock reservation
                TextInputDialog td = new TextInputDialog();
                td.setHeaderText("Enter VIP Customer Name");
                td.showAndWait().ifPresent(name -> table.reserve(name));
            } else if (type == btnFree) {
                table.free();
            }
            // Refresh visual
            updateButtonColor(btn, table.getStatus());
            cafe.saveAll();
        });
    }
}