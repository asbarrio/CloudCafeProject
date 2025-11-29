package application;

import application.model.Cafe;
import application.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WelcomeScreen { 
    
    private Cafe cafe;

    public WelcomeScreen() {
        this.cafe = new Cafe(); 
    }

    public void show(Stage primaryStage) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        VBox loginCard = new VBox(15);
        loginCard.setMaxWidth(400);
        loginCard.setPadding(new Insets(30));
        loginCard.setAlignment(Pos.CENTER);
        loginCard.getStyleClass().add("card");

        // Logo Logic
        ImageView logoView = new ImageView();
        try {
            // Ensure your image is in the right package or use a relative path
            String imagePath = getClass().getResource("/images/cloud.png") != null ? 
                               getClass().getResource("/images/cloud.png").toExternalForm() : 
                               getClass().getResource("/images/Logo.png").toExternalForm();
            
            logoView.setImage(new Image(imagePath));
            logoView.setFitWidth(150); // Slightly larger logo
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            // System.err.println("Logo not found"); // Silent fail is okay, just show text
        }
        
        Label titleLabel = new Label("Cloud Café");
        titleLabel.getStyleClass().add("title-label");
        
        Label subtitleLabel = new Label("System Access");
        subtitleLabel.getStyleClass().add("sub-label");

        // Inputs
        VBox inputContainer = new VBox(15);
        inputContainer.setPadding(new Insets(10, 0, 15, 0));
        
        TextField idField = new TextField();
        idField.setPromptText("User ID");
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        
        inputContainer.getChildren().addAll(idField, passField);

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("module-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPadding(new Insets(12));
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        // --- NEW: Make Account Link ---
        Hyperlink createAccLink = new Hyperlink("Make A New Account");
        createAccLink.getStyleClass().add("create-acc-link"); // We will add this style
        createAccLink.setOnAction(e -> showRegistrationDialog(primaryStage));

        loginButton.setOnAction(e -> {
            String id = idField.getText().trim();
            String pass = passField.getText().trim();
            User user = cafe.getUserManager().authenticate(id, pass);
            
            if (user != null) {
                routeUser(primaryStage, user);
            } else {
                statusLabel.setText("Invalid credentials.");
            }
        });

        if (logoView.getImage() != null) {
            loginCard.getChildren().add(logoView);
        } else {
            Label fallback = new Label("☁️");
            fallback.setStyle("-fx-font-size: 60px;");
            loginCard.getChildren().add(fallback);
        }

        loginCard.getChildren().addAll(titleLabel, subtitleLabel, inputContainer, loginButton, statusLabel, new Separator(), createAccLink);
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1000, 700); 
        if(getClass().getResource("application.css") != null)
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        primaryStage.setTitle("Cloud Café - Login");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // --- NEW: Registration Dialog ---
    private void showRegistrationDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Create Account");

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label header = new Label("Join the Cloud");
        header.getStyleClass().add("title-label");
        header.setStyle("-fx-font-size: 24px;");

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField newIdField = new TextField(); newIdField.setPromptText("Desired ID (e.g., U005)");
        PasswordField newPassField = new PasswordField(); newPassField.setPromptText("Password");
        
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Common", "VIP", "Cashier", "Manager");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("replenish-button");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText();
            String id = newIdField.getText();
            String pass = newPassField.getText();
            String role = roleBox.getValue();

            if (name.isEmpty() || id.isEmpty() || pass.isEmpty() || role == null) {
                new Alert(Alert.AlertType.ERROR, "Please fill all fields.").show();
                return;
            }
            
            if (cafe.getUserManager().getUser(id) != null) {
                new Alert(Alert.AlertType.ERROR, "ID already exists!").show();
                return;
            }

            // Create and Save using the NEW addUser method in UserManager
            User newUser = new User(id, name, role, 0, pass);
            // You need to ensure UserManager has an addUser method or access the map directly:
            cafe.getUserManager().getUsers().put(id, newUser);
            cafe.saveAll();
            
            new Alert(Alert.AlertType.INFORMATION, "Account created! Please log in.").showAndWait();
            dialog.close();
        });

        root.getChildren().addAll(header, nameField, newIdField, newPassField, roleBox, registerBtn);
        
        Scene scene = new Scene(root, 400, 550);
        if(getClass().getResource("application.css") != null)
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.show();
    }

    private void routeUser(Stage stage, User user) {
        stage.hide();
        String role = user.getRole();
        if ("Manager".equalsIgnoreCase(role)) {
            new ManagerDashboard(cafe, user).start(new Stage(), stage);
        } else if ("Cashier".equalsIgnoreCase(role)) {
            new CashierDashboard(cafe, user).start(new Stage(), stage);
        } else {
            new CustomerDashboard(cafe, user).start(new Stage(), stage);
        }
    }
}