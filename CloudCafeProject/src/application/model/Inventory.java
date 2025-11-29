package application.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Inventory {
    private Map<String, Ingredient> stock;
    private static final String INVENTORY_FILE = "inventory_data.csv"; 

    public Inventory() {
        this.stock = new HashMap<>();
        loadInventory(); 
    }
    
    public Map<String, Ingredient> getStock() {
        return stock;
    }

    public void loadInventory() {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) {
            initializeDefaultIngredients();
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                Ingredient item = Ingredient.fromCSVString(line);
                if (item != null) this.stock.put(item.getName(), item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (this.stock.isEmpty()) initializeDefaultIngredients();
    }

    public void saveInventory() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(INVENTORY_FILE))) {
            pw.println("Name,StockLevel,Unit,ReorderPoint"); 
            for (Ingredient item : stock.values()) {
                pw.println(item.toCSVString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // --- UPDATED: Full Ingredient List based on PDF Specs ---
    private void initializeDefaultIngredients() {
        // Base Coffee & Dairy
        addIngredient(new Ingredient("Espresso Beans", 5000, "grams", 1000));
        addIngredient(new Ingredient("Milk", 8000, "mL", 2000));
        addIngredient(new Ingredient("Vanilla Syrup", 1500, "mL", 300));
        addIngredient(new Ingredient("Dark Chocolate", 2000, "grams", 500)); 
        addIngredient(new Ingredient("White Chocolate", 1000, "grams", 200)); // For Cumulatte
        
        // Specialty Add-ins
        addIngredient(new Ingredient("Nitrogen", 50, "canisters", 5)); 
        addIngredient(new Ingredient("Cardamom", 200, "grams", 50)); 
        addIngredient(new Ingredient("Matcha Powder", 2000, "grams", 500));
        addIngredient(new Ingredient("Honey", 1000, "mL", 200)); // For Morning Matcha
        addIngredient(new Ingredient("Butterfly Pea", 500, "grams", 100)); 
        
        // Fruits & Tea
        addIngredient(new Ingredient("Lemon", 100, "pieces", 20)); 
        addIngredient(new Ingredient("Mango", 50, "pieces", 10)); 
        addIngredient(new Ingredient("Passion Fruit", 50, "pieces", 10)); 
        addIngredient(new Ingredient("Tea Leaves", 2000, "grams", 500)); 
        addIngredient(new Ingredient("Mint", 200, "grams", 50)); // For Rainy Iced Tea
        
        // Food Ingredients
        addIngredient(new Ingredient("Sourdough Bread", 20, "loaves", 5)); 
        addIngredient(new Ingredient("Avocado", 50, "pieces", 10)); 
        addIngredient(new Ingredient("Cherry Tomatoes", 500, "grams", 100)); 
        addIngredient(new Ingredient("Feta Cheese", 1000, "grams", 200)); 
        addIngredient(new Ingredient("Flour", 5000, "grams", 1000)); // Muffins/Rolls
        addIngredient(new Ingredient("Poppy Seeds", 500, "grams", 100)); // Zest Muffin
        addIngredient(new Ingredient("Butterscotch", 1000, "mL", 200)); // Celestial Roll

        saveInventory();
    }

    public void addIngredient(Ingredient ingredient) {
        this.stock.put(ingredient.getName(), ingredient);
    }
    
    public boolean checkIfItemExists(String name) {
        return this.stock.containsKey(name);
    }

    public List<Ingredient> getLowStockAlerts() {
        List<Ingredient> alerts = new ArrayList<>();
        for (Ingredient item : stock.values()) {
            if (item.needsReorder()) alerts.add(item);
        }
        return alerts;
    }
}