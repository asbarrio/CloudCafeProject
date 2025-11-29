package application;

import application.model.Cafe;
import application.model.Table;
import application.model.TableStatus;
import application.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomerDashboard {
    private Cafe cafe;
    private User currentUser;
    
    // Track selection
    private String currentTableSelection = null;
    private Label selectedTableLabel; 

    public CustomerDashboard(Cafe cafe, User user) {
        this.cafe = cafe;
        this.currentUser = user;
    }

    public void start(Stage stage, Stage loginStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label welcome = new Label("Welcome, " + currentUser.getName() + "!");
        welcome.getStyleClass().add("title-label");

        // Membership Card
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #48CAE4; -fx-padding: 20; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        Label roleLbl = new Label(currentUser.getRole().toUpperCase() + " MEMBER");
        roleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16pt;");
        
        Label pointsLbl = new Label("Cloud Points: " + currentUser.getPoints());
        pointsLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14pt;");

        card.getChildren().addAll(roleLbl, pointsLbl);

        // VIP Reservation Button
        Button reserveBtn = new Button("📅 VIP Table Reservation");
        reserveBtn.getStyleClass().add("module-button");
        
        if (!currentUser.isVIP()) {
            reserveBtn.setDisable(true);
            reserveBtn.setText("📅 VIP Table Reservation (VIP Only)");
            reserveBtn.getStyleClass().add("module-button-disabled");
        } else {
            // --- ACTION ADDED HERE ---
            reserveBtn.setOnAction(e -> showReservationDialog(stage));
        }

        Button logoutBtn = new Button("Log out");
        logoutBtn.getStyleClass().add("exit-button");
        logoutBtn.setOnAction(e -> {
            stage.close();
            loginStage.show();
        });

        root.getChildren().addAll(welcome, card, reserveBtn, logoutBtn);
        
        Scene scene = new Scene(root, 1000, 700); // Standardized Size
        if(getClass().getResource("application.css") != null)
             scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Customer Dashboard");
        stage.show();
    }

    // --- RESERVATION DIALOG (Similar to Cashier Map but for Booking) ---
    private void showReservationDialog(Stage owner) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Book a Table");

        VBox dialogLayout = new VBox(20);
        dialogLayout.setPadding(new Insets(30));
        dialogLayout.getStyleClass().add("root-pane");
        dialogLayout.setAlignment(Pos.CENTER);

        Label title = new Label("Select a Table to Reserve");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-font-size: 24px;");
        
        // Feedback Label
        selectedTableLabel = new Label("No Table Selected");
        selectedTableLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");

        ToggleGroup tableGroup = new ToggleGroup();

        // 1. VIP AREA
        VBox vipArea = new VBox(10);
        vipArea.getStyleClass().add("area-box");
        vipArea.setAlignment(Pos.CENTER);
        Label vipLabel = new Label("VIP AREA");
        vipLabel.getStyleClass().add("header-label");
        
        ToggleButton vip1 = createTableBtn("VIP 1", "table-vip", tableGroup);
        ToggleButton vip2 = createTableBtn("VIP 2", "table-vip", tableGroup);
        vipArea.getChildren().addAll(vipLabel, vip1, vip2);

        // 2. CUSTOMER AREA (4 Pax)
        VBox centerArea = new VBox(15);
        centerArea.setAlignment(Pos.CENTER);
        Label centerLabel = new Label("Customer Area (4 Pax)");
        centerLabel.getStyleClass().add("header-label");
        ToggleButton t1 = createTableBtn("Table 1", "table-reg-4", tableGroup);
        ToggleButton t2 = createTableBtn("Table 2", "table-reg-4", tableGroup);
        ToggleButton t3 = createTableBtn("Table 3", "table-reg-4", tableGroup);
        centerArea.getChildren().addAll(centerLabel, t1, t2, t3);

        // 3. SIDE AREA (2 Pax)
        VBox sideArea = new VBox(10);
        sideArea.setAlignment(Pos.CENTER);
        Label sideLabel = new Label("Side (2 Pax)");
        sideLabel.getStyleClass().add("header-label");
        FlowPane sideFlow = new FlowPane(10, 10);
        sideFlow.setPrefWrapLength(150);
        sideFlow.setAlignment(Pos.CENTER);
        sideFlow.getChildren().addAll(
            createTableBtn("T4", "table-reg-2", tableGroup), createTableBtn("T5", "table-reg-2", tableGroup),
            createTableBtn("T6", "table-reg-2", tableGroup), createTableBtn("T7", "table-reg-2", tableGroup),
            createTableBtn("T8", "table-reg-2", tableGroup)
        );
        sideArea.getChildren().addAll(sideLabel, sideFlow);

        HBox mapContainer = new HBox(40);
        mapContainer.setAlignment(Pos.CENTER);
        mapContainer.getChildren().addAll(vipArea, centerArea, sideArea);

        // CONFIRM BUTTON
        Button confirmBtn = new Button("Confirm Reservation");
        confirmBtn.getStyleClass().add("module-button");
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> {
            if (currentTableSelection != null) {
                handleReservation(currentTableSelection, dialog);
            }
        });

        // Handle Selection Changes
        tableGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ToggleButton btn = (ToggleButton) newVal;
                currentTableSelection = btn.getText();
                selectedTableLabel.setText("Selected: " + currentTableSelection);
                selectedTableLabel.setStyle("-fx-text-fill: #0077B6; -fx-font-weight: bold;");
                confirmBtn.setDisable(false);
            } else {
                confirmBtn.setDisable(true);
                selectedTableLabel.setText("No Table Selected");
                selectedTableLabel.setStyle("-fx-text-fill: #D32F2F;");
            }
        });

        dialogLayout.getChildren().addAll(title, mapContainer, selectedTableLabel, confirmBtn);
        Scene dialogScene = new Scene(dialogLayout, 950, 650);
        if(getClass().getResource("application.css") != null)
            dialogScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void handleReservation(String tableId, Stage dialog) {
        Table t = cafe.getTableManager().getTable(tableId);
        
        if (t == null) {
            new Alert(Alert.AlertType.ERROR, "Error finding table.").show();
            return;
        }

        if (t.getStatus() != TableStatus.AVAILABLE) {
            new Alert(Alert.AlertType.ERROR, "This table is currently " + t.getStatus() + ". Please choose another.").show();
            return;
        }

        // Perform Reservation
        t.reserve(currentUser.getName());
        cafe.saveAll(); // Save to CSV

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText("Reservation Confirmed!");
        success.setContentText("You have successfully reserved " + tableId + ".");
        success.showAndWait();
        
        dialog.close();
    }

    private ToggleButton createTableBtn(String name, String cssClass, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(name);
        btn.getStyleClass().add("table-btn");
        btn.getStyleClass().add(cssClass);
        btn.setToggleGroup(group);
        
        // Check status to visually disable occupied tables
        Table t = cafe.getTableManager().getTable(name);
        if (t != null && t.getStatus() != TableStatus.AVAILABLE) {
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #999; -fx-border-color: #AAA;"); // Greyed out
        }
        
        return btn;
    }
}