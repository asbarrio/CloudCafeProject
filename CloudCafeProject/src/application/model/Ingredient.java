package application.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Represents a single item in the inventory.
public class Ingredient {
    // JavaFX Properties for automatic GUI updates (e.g., when stockLevel changes)
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty stockLevel;
    private final SimpleStringProperty unit;
    private final SimpleIntegerProperty reorderPoint;

    // Constructor
    public Ingredient(String name, int stockLevel, String unit, int reorderPoint) {
        this.name = new SimpleStringProperty(name);
        this.stockLevel = new SimpleIntegerProperty(stockLevel);
        this.unit = new SimpleStringProperty(unit);
        this.reorderPoint = new SimpleIntegerProperty(reorderPoint);
    }

    // --- Core Inventory Logic ---

    /** Checks if the current stock level is at or below the reorder point. */
    public boolean needsReorder() {
        return this.stockLevel.get() <= this.reorderPoint.get();
    }

    /** Reduces stock by a specified amount (used during transactions). 
     * @return true if deduction was successful, false if stock was insufficient.
     */
    public boolean deductStock(int amount) {
        if (amount > 0 && this.stockLevel.get() >= amount) {
            // set() updates the value and notifies the JavaFX TableView automatically
            this.stockLevel.set(this.stockLevel.get() - amount); 
            return true; // Deduction successful
        } else if (this.stockLevel.get() < amount) {
            System.err.println("INSUFFICIENT STOCK: Cannot consume " + amount + " " + this.unit.get());
            return false; // Deduction failed due to low stock
        }
        return false; // Deduction failed (e.g., amount <= 0)
    }

    /** Increases stock by a specified amount (used during replenishment). */
    public void addStock(int amount) {
        if (amount > 0) {
            this.stockLevel.set(this.stockLevel.get() + amount);
        }
    }
    
    // --- Persistence Helper Methods ---
//... (toCSVString and fromCSVString remain the same)

    /** Converts the Ingredient object to a CSV format string for file saving. */
    public String toCSVString() {
        return name.get() + "," + stockLevel.get() + "," + unit.get() + "," + reorderPoint.get();
    }

    /** Creates an Ingredient object from a CSV line read from the file. */
    public static Ingredient fromCSVString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 4) return null;
        
        try {
            String name = parts[0].trim();
            int stock = Integer.parseInt(parts[1].trim());
            String unit = parts[2].trim();
            int reorder = Integer.parseInt(parts[3].trim());
            
            return new Ingredient(name, stock, unit, reorder);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in line: " + csvLine);
            return null;
        }
    }
    
    // --- Getters and Property Methods ---
    
    public String getName() { return name.get(); } 
    public int getStockLevel() { return stockLevel.get(); }
    public String getUnit() { return unit.get(); }
    public int getReorderPoint() { return reorderPoint.get(); }

    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty unitProperty() { return unit; }
    public SimpleIntegerProperty stockLevelProperty() { return stockLevel; }
    public SimpleIntegerProperty reorderPointProperty() { return reorderPoint; }
}