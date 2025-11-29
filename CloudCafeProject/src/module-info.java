module CloudCafe {
	// Require necessary JavaFX modules
	requires javafx.controls;
	requires javafx.base; // <-- ADDED: Crucial for SimpleStringProperty/SimpleIntegerProperty

	// Open packages containing GUI classes (Main.java) and resources (application.css)
	opens application to javafx.graphics, javafx.fxml;
    
    // Open the model package so JavaFX can access Ingredient properties (e.g., via PropertyValueFactory)
    opens application.model to javafx.base; // <-- ADDED: Ensure model properties are accessible
}