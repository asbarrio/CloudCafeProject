package application.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Menu {
    private Map<String, MenuItem> items;
    private static final String MENU_FILE = "menu_data.csv"; 

    public Menu() {
        this.items = new HashMap<>();
        loadMenu(); 
    }
    
    public Map<String, MenuItem> getItems() { return items; }

    public void loadMenu() {
        File file = new File(MENU_FILE);
        if (!file.exists()) {
            initializeDefaultMenu();
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                MenuItem item = MenuItem.fromCSVString(line);
                if (item != null) this.items.put(item.getName(), item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (this.items.isEmpty()) initializeDefaultMenu();
    }

    public void saveMenu() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MENU_FILE))) {
            pw.println("Name,Price,Category,Description,Ingredients"); 
            for (MenuItem item : items.values()) {
                pw.println(item.toCSVString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeDefaultMenu() {
        // A. Cloudy Brews
        String catA = "Cloudy Brews";
        createItem("Creamy Cumulatte", 165.00, catA, "Velvety vanilla bean latte with cloud foam.", 
            Map.of("Espresso Beans", 18, "Vanilla Syrup", 20, "Milk", 200, "White Chocolate", 5));
        createItem("Stratospresso", 170.00, catA, "Layered Macchiato with dark chocolate hint.", 
            Map.of("Espresso Beans", 20, "Dark Chocolate", 15, "Milk", 150));
        createItem("Nimbus Cold Brew", 185.00, catA, "Nitrogen-infused creamy cold brew.", 
            Map.of("Espresso Beans", 25, "Nitrogen", 1));
        createItem("Cirrus Fog Americano", 145.00, catA, "Americano infused with cardamom mist.", 
            Map.of("Espresso Beans", 18, "Cardamom", 2));
        createItem("Altostratus Mocha", 175.00, catA, "Dense espresso and chocolate blend.", 
            Map.of("Espresso Beans", 18, "Dark Chocolate", 25, "Milk", 180));

        // B. Weather Refreshers
        String catB = "Weather Refreshers";
        createItem("Aurora Lemonada", 150.00, catB, "Color-changing sparkling lemonade.", 
            Map.of("Lemon", 2, "Butterfly Pea", 5));
        createItem("Morning Matcha", 160.00, catB, "Premium matcha latte with honey drizzle.", 
            Map.of("Matcha Powder", 15, "Milk", 200, "Honey", 10));
        createItem("Sunshower Smoothie", 195.00, catB, "Mango and passion fruit smoothie.", 
            Map.of("Mango", 1, "Passion Fruit", 1));
        createItem("Rainy Iced Tea", 140.00, catB, "Bold black tea with mint and citrus.", 
            Map.of("Tea Leaves", 10, "Mint", 5, "Lemon", 1));

        // C. Elevated Bites
        String catC = "Elevated Bites";
        createItem("Horizon Toast", 230.00, catC, "Sourdough toast with avocado and feta.", 
            Map.of("Sourdough Bread", 1, "Avocado", 1, "Feta Cheese", 30, "Cherry Tomatoes", 30));
        createItem("Zest Muffin", 150.00, catC, "Lemon-poppy seed muffin.", 
            Map.of("Flour", 100, "Lemon", 1, "Poppy Seeds", 5));
        createItem("Celestial Roll", 135.00, catC, "Giant butterscotch sticky roll.", 
            Map.of("Flour", 120, "Butterscotch", 30));

        saveMenu();
    }

    private void createItem(String name, double price, String category, String desc, Map<String, Integer> ingredients) {
        this.items.put(name, new MenuItem(name, price, category, desc, ingredients));
    }
}