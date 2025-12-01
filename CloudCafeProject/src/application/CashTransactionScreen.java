package application;

import application.model.*;
import application.model.MenuItem;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font; 
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CashTransactionScreen {

    private Cafe cafe;
    private ObservableList<CartItem> cartItems;
    private Label totalLabel, discountLabel, finalTotalLabel, customerNameLabel;
    private TextField customerIdField;
    private TableView<CartItem> cartTable;
    
    private String currentTableSelection = null;
    private Label selectedTableLabel; 

    // --- CartItem Class ---
    public static class CartItem {
        private MenuItem item;
        private SimpleStringProperty name;
        private SimpleIntegerProperty qty;
        private SimpleDoubleProperty price;

        public CartItem(MenuItem item) {
            this.item = item;
            this.name = new SimpleStringProperty(item.getName());
            this.qty = new SimpleIntegerProperty(1);
            this.price = new SimpleDoubleProperty(item.getPrice());
        }
        public String getName() { return name.get(); }
        public int getQty() { return qty.get(); }
        public double getPrice() { return price.get(); }
        public MenuItem getItem() { return item; }
        public void increment() { qty.set(qty.get() + 1); price.set(item.getPrice() * qty.get()); }
        public void decrement() { qty.set(qty.get() - 1); price.set(item.getPrice() * qty.get()); }
    }

    public CashTransactionScreen(Cafe cafe) {
        this.cafe = cafe;
        this.cartItems = FXCollections.observableArrayList();
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(0, 0, 20, 0));
        Button backBtn = new Button("⬅ Dashboard");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> stage.close());
        
        Label screenTitle = new Label("New Order");
        screenTitle.getStyleClass().add("header-label");
        screenTitle.setStyle("-fx-font-size: 24px;"); 
        
        topBar.getChildren().addAll(backBtn, screenTitle);
        root.setTop(topBar);

        // --- LEFT: MENU ---
        VBox menuCard = new VBox(15);
        menuCard.getStyleClass().add("card");
        menuCard.setPrefWidth(650);
        
        Label menuHeader = new Label("Select Items");
        menuHeader.getStyleClass().add("header-label");

        VBox menuContent = new VBox(25);
        List<String> categories = List.of("Cloudy Brews", "Weather Refreshers", "Elevated Bites");

        for (String category : categories) {
            Label catHeader = new Label(category);
            catHeader.setStyle("-fx-text-fill: #0077B6; -fx-font-weight: bold; -fx-font-size: 15px;");
            
            FlowPane catGrid = new FlowPane();
            catGrid.setHgap(12);
            catGrid.setVgap(12);
            
            List<MenuItem> items = cafe.getMenu().getItems().values().stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());

            for (MenuItem item : items) {
                Button btn = new Button(item.getName() + "\n₱" + item.getPrice());
                btn.setPrefSize(140, 90);
                btn.getStyleClass().add("coffee-button");
                btn.setStyle("-fx-wrap-text: true; -fx-text-alignment: center;");
                btn.setOnAction(e -> addToCart(item));
                catGrid.getChildren().add(btn);
            }
            if(!items.isEmpty()) menuContent.getChildren().addAll(catHeader, catGrid);
        }
        
        ScrollPane scrollPane = new ScrollPane(menuContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        
        menuCard.getChildren().addAll(menuHeader, scrollPane);

        // --- RIGHT: RECEIPT CARD ---
        VBox cartCard = new VBox(15);
        cartCard.getStyleClass().add("card");
        cartCard.setPrefWidth(400);

        Label cartHeader = new Label("Current Order");
        cartHeader.getStyleClass().add("header-label");

        // Table Selection
        HBox tableBox = new HBox(10);
        tableBox.setAlignment(Pos.CENTER_LEFT);
        
        selectedTableLabel = new Label("No Table Selected");
        selectedTableLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        
        Button tableSelectBtn = new Button("Choose Seat 🪑");
        tableSelectBtn.getStyleClass().add("button");
        tableSelectBtn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333;");
        tableSelectBtn.setOnAction(e -> showTableMapDialog(stage));
        
        tableBox.getChildren().addAll(tableSelectBtn, selectedTableLabel);

        // Cart Table
        cartTable = new TableView<>();
        cartTable.setItems(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<CartItem, String> colName = new TableColumn<>("Item");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        
        TableColumn<CartItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQty()).asObject());
        colQty.setMaxWidth(50);
        
        TableColumn<CartItem, Double> colPrice = new TableColumn<>("₱");
        colPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrice()).asObject());
        colPrice.setMaxWidth(70);
        
        cartTable.getColumns().addAll(colName, colQty, colPrice);
        cartTable.setPrefHeight(300);

        // Action Buttons
        HBox cartActions = new HBox(10);
        Button removeBtn = new Button("Remove Item");
        removeBtn.getStyleClass().add("deduct-button");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(removeBtn, Priority.ALWAYS);
        removeBtn.setOnAction(e -> handleRemoveSelected());

        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("button");
        clearBtn.setStyle("-fx-text-fill: #757575; -fx-background-color: #EEEEEE;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(clearBtn, Priority.ALWAYS);
        clearBtn.setOnAction(e -> { cartItems.clear(); updateTotals(); });
        
        cartActions.getChildren().addAll(removeBtn, clearBtn);

        // Customer Logic
        HBox custBox = new HBox(10);
        customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");
        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("button"); 
        applyBtn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333;");
        applyBtn.setOnAction(e -> updateTotals());
        custBox.getChildren().addAll(customerIdField, applyBtn);
        
        customerNameLabel = new Label("Guest");
        customerNameLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");

        // Totals
        VBox totalBox = new VBox(5);
        totalBox.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 10; -fx-background-radius: 5;");
        totalLabel = new Label("Subtotal: ₱0.00");
        discountLabel = new Label("Discount: -₱0.00");
        discountLabel.setStyle("-fx-text-fill: #4CAF50;");
        finalTotalLabel = new Label("Total: ₱0.00");
        finalTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #0077B6;");
        totalBox.getChildren().addAll(totalLabel, discountLabel, finalTotalLabel);

        Button checkoutBtn = new Button("Confirm & Pay");
        checkoutBtn.getStyleClass().add("replenish-button");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setPadding(new Insets(15));
        checkoutBtn.setOnAction(e -> handleCheckout());

        cartCard.getChildren().addAll(cartHeader, tableBox, cartTable, cartActions, new Separator(), new Label("Membership"), custBox, customerNameLabel, totalBox, checkoutBtn);

        root.setCenter(menuCard);
        root.setRight(cartCard);
        BorderPane.setMargin(cartCard, new Insets(0, 0, 0, 20));

        Scene scene = new Scene(root, 1100, 750);
        if(getClass().getResource("application.css") != null)
             scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("New Order");
        stage.show();
    }

    // --- VIP SEAT LOGIC ---
    private void showTableMapDialog(Stage owner) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Select Table");

        VBox dialogLayout = new VBox(20);
        dialogLayout.setPadding(new Insets(30));
        dialogLayout.getStyleClass().add("root-pane");
        dialogLayout.setAlignment(Pos.CENTER);

        Label title = new Label("Tap a Table to Select");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-font-size: 24px;");

        ToggleGroup tableGroup = new ToggleGroup();
        
        // Check if current customer is VIP
        String custID = customerIdField.getText().trim().toUpperCase();
        boolean isVIP = custID.startsWith("V"); 

        // 1. VIP AREA
        VBox vipArea = new VBox(10);
        vipArea.getStyleClass().add("area-box");
        vipArea.setAlignment(Pos.CENTER);
        Label vipLabel = new Label("VIP AREA");
        vipLabel.getStyleClass().add("header-label");
        
        ToggleButton vip1 = createTableBtn("VIP 1", "table-vip", tableGroup);
        ToggleButton vip2 = createTableBtn("VIP 2", "table-vip", tableGroup);
        
        // ALERT if non-VIP tries to click
        if (!isVIP) {
            vip1.setOnAction(e -> { vip1.setSelected(false); showVipAlert(); });
            vip2.setOnAction(e -> { vip2.setSelected(false); showVipAlert(); });
        }
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

        Button confirmBtn = new Button("Confirm Selection");
        confirmBtn.getStyleClass().add("module-button");
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> {
            ToggleButton selected = (ToggleButton) tableGroup.getSelectedToggle();
            if (selected != null) {
                currentTableSelection = selected.getText();
                selectedTableLabel.setText(currentTableSelection);
                selectedTableLabel.setStyle("-fx-text-fill: #0077B6; -fx-font-weight: bold; -fx-font-size: 14px;");
                dialog.close();
            }
        });

        tableGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> confirmBtn.setDisable(newVal == null));

        dialogLayout.getChildren().addAll(title, mapContainer, confirmBtn);
        Scene dialogScene = new Scene(dialogLayout, 900, 600);
        if(getClass().getResource("application.css") != null)
            dialogScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showVipAlert() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Restricted Access");
        a.setHeaderText("VIP Section Only");
        a.setContentText("This seat is reserved for VIP customers. Please select a Regular seat.");
        a.showAndWait();
    }

    private ToggleButton createTableBtn(String name, String cssClass, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(name);
        btn.getStyleClass().add("table-btn");
        btn.getStyleClass().add(cssClass);
        btn.setToggleGroup(group);
        return btn;
    }

    // --- STANDARD LOGIC ---
    private void handleRemoveSelected() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getQty() > 1) { selected.decrement(); cartTableRefresh(); } else { cartItems.remove(selected); }
        updateTotals();
    }
    private void addToCart(MenuItem item) {
        if (!item.isAvailable(cafe.getInventory())) { new Alert(Alert.AlertType.WARNING, "Insufficient stock").show(); return; }
        for (CartItem ci : cartItems) { if (ci.getItem().getName().equals(item.getName())) { ci.increment(); cartTableRefresh(); updateTotals(); return; } }
        cartItems.add(new CartItem(item)); updateTotals();
    }
    private void cartTableRefresh() { if (!cartItems.isEmpty()) cartItems.set(0, cartItems.get(0)); }
    
    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(CartItem::getPrice).sum();
        double discount = 0;
        String uid = customerIdField.getText().trim().toUpperCase();

        // CHECK FOR INVALID IDS (Manager/Cashier)
        if (uid.startsWith("M") || uid.startsWith("C")) {
            customerNameLabel.setText("Invalid Customer ID");
            customerNameLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
            customerIdField.clear();
            totalLabel.setText(String.format("Subtotal: ₱%.2f", subtotal));
            discountLabel.setText(String.format("Discount: -₱%.2f", 0.0));
            finalTotalLabel.setText(String.format("Total: ₱%.2f", subtotal));
            return;
        }

        User u = cafe.getUserManager().getUser(uid);
        if (u != null) { 
            customerNameLabel.setText(u.getName() + " (" + u.getRole() + ")");
            customerNameLabel.setStyle("-fx-text-fill: #0077B6; -fx-font-weight: bold;");
            if (u.isVIP()) discount = subtotal * 0.10; 
        } else if (!uid.isEmpty()) {
            customerNameLabel.setText("User not found");
            customerNameLabel.setStyle("-fx-text-fill: red;");
        } else {
            customerNameLabel.setText("Guest");
            customerNameLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
        }
        
        totalLabel.setText(String.format("Subtotal: ₱%.2f", subtotal));
        discountLabel.setText(String.format("Discount: -₱%.2f", discount));
        finalTotalLabel.setText(String.format("Total: ₱%.2f", subtotal - discount));
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) { new Alert(Alert.AlertType.ERROR, "Cart is empty!").show(); return; }
        if (currentTableSelection == null) { new Alert(Alert.AlertType.ERROR, "Please select a table first!").show(); return; }

        double totalPaid = Double.parseDouble(finalTotalLabel.getText().replace("Total: ₱", ""));
        
        for (CartItem ci : cartItems) {
            for (int i = 0; i < ci.getQty(); i++) {
                for (java.util.Map.Entry<String, Integer> entry : ci.getItem().getIngredientsConsumed().entrySet()) {
                    cafe.getInventory().getStock().get(entry.getKey()).deductStock(entry.getValue());
                }
            }
        }
        
        String uid = customerIdField.getText().trim();
        User u = cafe.getUserManager().getUser(uid);
        if (u != null) { u.addPoints((int)(totalPaid/10)); }
     // Update Table Status
        if (currentTableSelection != null) {
            Table t = cafe.getTableManager().getTable(currentTableSelection);
            if (t != null) {
                String custName = (u != null) ? u.getName() : "Guest";
                t.occupy(custName);
            }
        }

        
        cafe.saveAll();
        
        

        // --- SHOW RECEIPT ---
        showReceipt(u != null ? u.getName() : "Guest", currentTableSelection);

        cartItems.clear(); 
        updateTotals(); 
        customerIdField.clear();
        currentTableSelection = null;
        selectedTableLabel.setText("No Table Selected");
        selectedTableLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
    }

    // --- CUTE RECEIPT DIALOG ---
    private void showReceipt(String customerName, String tableName) {
        Stage receiptStage = new Stage();
        receiptStage.setTitle("Receipt");
        
        VBox receiptBox = new VBox(5); // Tight spacing
        receiptBox.getStyleClass().add("receipt-box");
        receiptBox.setAlignment(Pos.TOP_CENTER);
        receiptBox.setPrefWidth(350);

        Label title = new Label("CLOUD CAFÉ");
        title.getStyleClass().add("receipt-title");
        Label subtitle = new Label("Los Baños, Laguna");
        subtitle.getStyleClass().add("receipt-text");
        
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        VBox details = new VBox(2);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setPadding(new Insets(10, 0, 10, 0));
        
        addReceiptLine(details, "--------------------------------");
        addReceiptLine(details, "DATE: " + date);
        addReceiptLine(details, "CUST: " + customerName.toUpperCase());
        addReceiptLine(details, "SEAT: " + tableName.toUpperCase());
        addReceiptLine(details, "--------------------------------");

        VBox itemsBox = new VBox(2);
        for (CartItem item : cartItems) {
            String line = String.format("%-2d %-18s %6.2f", item.getQty(), 
                          item.getName().length() > 18 ? item.getName().substring(0,18) : item.getName(), 
                          item.getPrice());
            addReceiptLine(itemsBox, line);
        }

        VBox footer = new VBox(2);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        
        addReceiptLine(footer, "--------------------------------");
        addReceiptLine(footer, "TOTAL: " + finalTotalLabel.getText().replace("Total: ", ""));
        addReceiptLine(footer, "--------------------------------");
        
        Label thanks = new Label("THANK YOU!");
        thanks.getStyleClass().add("receipt-text");
        thanks.setAlignment(Pos.CENTER);
        
        Label barcode = new Label("||| || ||| | ||| ||");
        barcode.setFont(Font.font("Arial", 24));

        receiptBox.getChildren().addAll(title, subtitle, details, itemsBox, footer, thanks, barcode);

        Scene scene = new Scene(receiptBox);
        if(getClass().getResource("application.css") != null)
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        
        receiptStage.setScene(scene);
        receiptStage.show();
    }
    
    private void addReceiptLine(VBox container, String text) {
        Label l = new Label(text);
        l.getStyleClass().add("receipt-text");
        container.getChildren().add(l);
    }

}
