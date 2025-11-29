package application.model;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class MenuItem {
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty category; // <--- NEW FIELD
    private final SimpleStringProperty description;
    
    // Key: Ingredient Name, Value: Quantity
    private Map<String, Integer> ingredientsConsumed;

    public MenuItem(String name, double price, String category, String description, Map<String, Integer> ingredientsConsumed) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.category = new SimpleStringProperty(category);
        this.description = new SimpleStringProperty(description);
        this.ingredientsConsumed = ingredientsConsumed;
    }

    public boolean isAvailable(Inventory inventory) {
        for (Map.Entry<String, Integer> entry : ingredientsConsumed.entrySet()) {
            Ingredient ingredient = inventory.getStock().get(entry.getKey());
            if (ingredient == null || ingredient.getStockLevel() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    // --- Persistence ---
    private String ingredientsMapToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : ingredientsConsumed.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }
    
    public String toCSVString() {
        // Format: Name,Price,Category,Description,Ingredients
        return name.get() + "," + price.get() + "," + category.get() + "," + description.get().replace(",", ";") + "," + ingredientsMapToString();
    }

    public static MenuItem fromCSVString(String csvLine) {
        // Now splits into 5 parts
        String[] parts = csvLine.split(",", 5); 
        if (parts.length != 5) return null;
        
        try {
            String name = parts[0].trim();
            double price = Double.parseDouble(parts[1].trim());
            String category = parts[2].trim(); // <--- Parse Category
            String description = parts[3].trim().replace(";", ","); 
            String ingredientsString = parts[4].trim();
            
            Map<String, Integer> ingredients = new HashMap<>();
            if (!ingredientsString.isEmpty()) {
                String[] ingredientEntries = ingredientsString.split("\\|");
                for (String entry : ingredientEntries) {
                    String[] kv = entry.split(":");
                    if (kv.length == 2) {
                        ingredients.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                    }
                }
            }
            return new MenuItem(name, price, category, description, ingredients);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing menu item: " + csvLine);
            return null;
        }
    }

    // --- Getters ---
    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }
    
    public double getPrice() { return price.get(); }
    public SimpleDoubleProperty priceProperty() { return price; }

    public String getCategory() { return category.get(); } // <--- Getter
    public SimpleStringProperty categoryProperty() { return category; }

    public String getDescription() { return description.get(); }
    public SimpleStringProperty descriptionProperty() { return description; }

    public Map<String, Integer> getIngredientsConsumed() { return ingredientsConsumed; }
}