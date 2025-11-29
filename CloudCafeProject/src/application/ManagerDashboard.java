package application;

import application.model.Cafe;
import application.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerDashboard {
    private Cafe cafe;
    private User currentUser;

    public ManagerDashboard(Cafe cafe, User user) {
        this.cafe = cafe;
        this.currentUser = user;
    }

    public void start(Stage stage, Stage loginStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label welcome = new Label("Manager Dashboard\nWelcome, " + currentUser.getName());
        welcome.getStyleClass().add("title-label");
        welcome.setStyle("-fx-font-size: 24px; -fx-text-alignment: center;");

        // 1. Inventory
        Button inventoryBtn = new Button("📦 Inventory Management");
        inventoryBtn.getStyleClass().add("module-button");
        inventoryBtn.setOnAction(e -> {
            stage.hide();
            InventoryScreen is = new InventoryScreen(); 
            Stage s = new Stage();
            is.start(s);
            s.setOnHidden(ev -> stage.show());
        });

        // 2. Cash Transactions
        Button cashBtn = new Button("💰 Cash Transactions");
        cashBtn.getStyleClass().add("module-button");
        cashBtn.setOnAction(e -> {
            stage.hide();
            CashTransactionScreen cts = new CashTransactionScreen(cafe);
            Stage s = new Stage();
            cts.start(s);
            s.setOnHidden(ev -> stage.show());
        });

        // 3. Community Rewards
        Button communityBtn = new Button("👥 Community Rewards");
        communityBtn.getStyleClass().add("module-button");
        communityBtn.setOnAction(e -> {
            stage.hide();
            CommunityScreen cs = new CommunityScreen(cafe);
            Stage s = new Stage();
            cs.start(s);
            s.setOnHidden(ev -> stage.show());
        });
        
        // 4. Table Monitoring (ENABLED for Milestone 3)
        Button tablesBtn = new Button("🪑 Table Monitoring");
        tablesBtn.getStyleClass().add("module-button");
        tablesBtn.setOnAction(e -> {
            stage.hide();
            TableMonitoringScreen tms = new TableMonitoringScreen(cafe);
            Stage s = new Stage();
            tms.start(s);
            s.setOnHidden(ev -> stage.show());
        });

        // Logout
        Button logoutBtn = new Button("🔒 Logout");
        logoutBtn.getStyleClass().add("exit-button");
        logoutBtn.setOnAction(e -> {
            cafe.saveAll();
            stage.close();
            loginStage.show();
        });

        root.getChildren().addAll(welcome, inventoryBtn, cashBtn, communityBtn, tablesBtn, logoutBtn);
        
        Scene scene = new Scene(root, 1000, 700);
        if(getClass().getResource("application.css") != null)
             scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Manager Dashboard");
        stage.show();
    }
}