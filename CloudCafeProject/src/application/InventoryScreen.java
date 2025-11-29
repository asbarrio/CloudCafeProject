package application;

import application.model.Ingredient;
import application.model.Inventory;
import javafx.application.Platform; // Import Platform
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Handles the GUI and logic for Inventory Management (Stock In/Out, Add Item).
 * This was previously the Main.java class.
 */
public class InventoryScreen { 

    private Inventory inventory; 
    private ObservableList<Ingredient> inventoryData;
    private Label alertLabel;
    private TextField replenishAmountField;
    private TableView<Ingredient> inventoryTable;

    // Fields for Adding Inventory Items
    private TextField newNameField;
    private TextField newUnitField;
    private TextField newStockField;
    private TextField newReorderField;

    // Use a non-Application start method, accepting a Stage
    public void start(Stage primaryStage) { 
        // 1. Initialize Model and Data
        this.inventory = new Inventory(); 
        this.inventoryData = FXCollections.observableArrayList(inventory.getStock().values());

        // 2. Build the Main Layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        // Title Section
        Label titleLabel = new Label("📦 Cloud Café Inventory Management");
        titleLabel.getStyleClass().add("title-label");
        
        // Back Button
        Button backButton = new Button("⬅️ Back to Dashboard");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> primaryStage.close()); // Closing returns to the WelcomeScreen

        // Layout the Header
        HBox headerLayout = new HBox(10, titleLabel);
        headerLayout.setAlignment(Pos.CENTER);
        VBox.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        VBox.setMargin(backButton, new Insets(0, 0, 10, 0));

        // Inventory Table
        inventoryTable = createInventoryTable();
        inventoryTable.setItems(inventoryData);

        // Controls
        VBox stockControls = createStockControls();
        VBox addControls = createAddIngredientControls();

        // Group the controls horizontally for better spacing
        HBox controlGroup = new HBox(30, stockControls, addControls);
        controlGroup.setAlignment(Pos.CENTER_LEFT);
        controlGroup.setPadding(new Insets(10, 0, 10, 0));

        // Alert Status Display
        alertLabel = new Label();
        alertLabel.getStyleClass().add("alert-label");
        updateAlerts(); 

        root.getChildren().addAll(backButton, headerLayout, inventoryTable, controlGroup, alertLabel);
        
        // 3. Setup Scene and Stage
        Scene scene = new Scene(root, 1200, 750); 
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        primaryStage.setTitle("CMSC 22 - Inventory Milestone 1");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Ensure data is saved when the window is closed
        primaryStage.setOnCloseRequest(e -> inventory.saveInventory());
    }
    
    // --- GUI Component Building Methods (Same as before) ---

    @SuppressWarnings("unchecked")
    private TableView<Ingredient> createInventoryTable() {
        TableView<Ingredient> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ingredient, String> nameCol = new TableColumn<>("Ingredient Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(200);

        TableColumn<Ingredient, Integer> stockCol = new TableColumn<>("Current Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockLevel"));

        TableColumn<Ingredient, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<Ingredient, Integer> reorderCol = new TableColumn<>("Reorder Point");
        reorderCol.setCellValueFactory(new PropertyValueFactory<>("reorderPoint"));

        TableColumn<Ingredient, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().needsReorder() ? "⚠️ LOW STOCK" : "✅ OK")
        );
        statusCol.setPrefWidth(150);

        table.getColumns().addAll(nameCol, stockCol, unitCol, reorderCol, statusCol);
        return table;
    }

    private VBox createStockControls() {
        Label header = new Label("Stock Operations (Selected Item)");
        header.getStyleClass().add("header-label");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label prompt = new Label("Amount:");
        
        replenishAmountField = new TextField();
        replenishAmountField.setPromptText("Enter amount");
        replenishAmountField.setMaxWidth(150);

        Button replenishButton = new Button("➕ Stock IN");
        replenishButton.getStyleClass().add("replenish-button");
        replenishButton.setOnAction(e -> handleReplenish());

        Button deductButton = new Button("➖ Stock OUT");
        deductButton.getStyleClass().add("deduct-button");
        deductButton.setOnAction(e -> handleDeduct());
        
        controls.getChildren().addAll(prompt, replenishAmountField, replenishButton, deductButton);
        return new VBox(5, header, controls);
    }
    
    private VBox createAddIngredientControls() {
        Label header = new Label("Add New Ingredient");
        header.getStyleClass().add("header-label");

        // Input Fields
        newNameField = new TextField(); newNameField.setPromptText("Ingredient Name");
        newUnitField = new TextField(); newUnitField.setPromptText("Unit (e.g., grams)");
        newStockField = new TextField(); newStockField.setPromptText("Initial Stock");
        newReorderField = new TextField(); newReorderField.setPromptText("Reorder Point");

        // Layout for inputs
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(5);
        inputGrid.setPadding(new Insets(10, 0, 10, 0));
        
        inputGrid.addRow(0, new Label("Name:"), newNameField, new Label("Unit:"), newUnitField);
        inputGrid.addRow(1, new Label("Stock:"), newStockField, new Label("Reorder:"), newReorderField);
        
        // Ensure the Grid is not excessively wide
        newNameField.setMaxWidth(150);
        newUnitField.setMaxWidth(150);
        newStockField.setMaxWidth(150);
        newReorderField.setMaxWidth(150);

        Button addButton = new Button("➕ Add Ingredient");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> handleAddIngredient());
        
        return new VBox(5, header, inputGrid, addButton);
    }

    // --- Event Handlers and Logic (Same as before) ---
    private void handleReplenish() {
        Ingredient selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Item Selected", "Please select an ingredient from the table to replenish.");
            return;
        }
        
        try {
            int amount = Integer.parseInt(replenishAmountField.getText().trim());
            if (amount <= 0) {
                 showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Invalid Value", "Please enter a positive amount greater than 0.");
                 return;
            }
            
            selectedItem.addStock(amount); 
            inventory.saveInventory();
            inventoryTable.refresh(); 
            updateAlerts();
            replenishAmountField.clear();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Stock Updated", 
                      selectedItem.getName() + " replenished by " + amount + " " + selectedItem.getUnit() + ".");

        } catch (NumberFormatException e) {
             showAlert(Alert.AlertType.ERROR, "Invalid Input", "Input Format Error", "Please enter a valid whole number for the amount.");
        }
    }
    
    private void handleDeduct() {
        Ingredient selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Item Selected", "Please select an ingredient from the table to deduct stock from.");
            return;
        }
        
        try {
            int amount = Integer.parseInt(replenishAmountField.getText().trim());
            if (amount <= 0) {
                 showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Invalid Value", "Please enter a positive amount greater than 0.");
                 return;
            }
            
            boolean success = selectedItem.deductStock(amount); 
            
            if (success) {
                inventory.saveInventory();
                inventoryTable.refresh(); 
                updateAlerts();
                replenishAmountField.clear();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Stock Deducted", 
                          selectedItem.getName() + " stock reduced by " + amount + " " + selectedItem.getUnit() + ".");
            } else {
                 showAlert(Alert.AlertType.WARNING, "Low Stock Warning", "Insufficient Stock", 
                           "Cannot deduct " + amount + " " + selectedItem.getUnit() + ". Only " + selectedItem.getStockLevel() + " remaining.");
            }

        } catch (NumberFormatException e) {
             showAlert(Alert.AlertType.ERROR, "Invalid Input", "Input Format Error", "Please enter a valid whole number for the amount.");
        }
    }
    
    private void handleAddIngredient() {
        String name = newNameField.getText().trim();
        String unit = newUnitField.getText().trim();
        
        if (name.isEmpty() || unit.isEmpty() || newStockField.getText().trim().isEmpty() || newReorderField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Incomplete Information", "Please fill in all fields for the new ingredient.");
            return;
        }
        
        if (inventory.checkIfItemExists(name)) {
             showAlert(Alert.AlertType.ERROR, "Duplicate Item", "Ingredient Already Exists", name + " is already in the inventory.");
             return;
        }

        try {
            int stock = Integer.parseInt(newStockField.getText().trim());
            int reorder = Integer.parseInt(newReorderField.getText().trim());
            
            if (stock < 0 || reorder < 0) {
                 showAlert(Alert.AlertType.ERROR, "Invalid Values", "Negative Numbers Not Allowed", "Stock level and reorder point must be non-negative.");
                 return;
            }

            Ingredient newIngredient = new Ingredient(name, stock, unit, reorder);
            
            inventory.addIngredient(newIngredient);
            inventory.saveInventory();

            inventoryData.add(newIngredient);
            updateAlerts();
            newNameField.clear();
            newUnitField.clear();
            newStockField.clear();
            newReorderField.clear();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Ingredient Added", 
                      name + " has been successfully added to the inventory.");
            
        } catch (NumberFormatException e) {
             showAlert(Alert.AlertType.ERROR, "Invalid Input", "Input Format Error", "Please enter valid whole numbers for Stock and Reorder Point.");
        }
    }

    private void updateAlerts() {
        List<Ingredient> lowStockItems = inventory.getLowStockAlerts();
        
        alertLabel.getStyleClass().removeAll("alert-low", "alert-ok");
        
        if (!lowStockItems.isEmpty()) {
            alertLabel.setText("⚠️ LOW STOCK: " + lowStockItems.size() + " items are below reorder point and need attention!");
            alertLabel.getStyleClass().add("alert-low");
        } else {
            alertLabel.setText("✅ All stock levels are satisfactory. (" + inventoryData.size() + " ingredients currently tracked)");
            alertLabel.getStyleClass().add("alert-ok");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}