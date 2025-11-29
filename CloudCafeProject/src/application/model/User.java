package application.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty role; // "Manager", "Cashier", "Common", "VIP"
    private final SimpleIntegerProperty points;
    private final SimpleStringProperty password; // NEW FIELD

    public User(String id, String name, String role, int points, String password) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.role = new SimpleStringProperty(role);
        this.points = new SimpleIntegerProperty(points);
        this.password = new SimpleStringProperty(password);
    }

    // --- Logic ---
    public void addPoints(int amount) {
        this.points.set(this.points.get() + amount);
    }

    public boolean isVIP() {
        return "VIP".equalsIgnoreCase(role.get());
    }
    
    // --- Persistence ---
    public String toCSV() {
        return id.get() + "," + name.get() + "," + role.get() + "," + points.get() + "," + password.get();
    }

    public static User fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) return null; // Updated to expect 5 parts
        try {
            return new User(
                parts[0].trim(), 
                parts[1].trim(), 
                parts[2].trim(), 
                Integer.parseInt(parts[3].trim()), 
                parts[4].trim()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- Getters ---
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getRole() { return role.get(); }
    public int getPoints() { return points.get(); }
    public String getPassword() { return password.get(); }
    
    // Properties for TableView
    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty roleProperty() { return role; }
    public SimpleIntegerProperty pointsProperty() { return points; }
}