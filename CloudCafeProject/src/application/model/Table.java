package application.model;

import javafx.beans.property.*;

public class Table {
    private final String id;
    private final int capacity;
    private final boolean isVip;
    // Properties allow the GUI to update automatically when these change
    private final ObjectProperty<TableStatus> status;
    private final SimpleStringProperty currentCustomer;

    public Table(String id, int capacity, boolean isVip) {
        this.id = id;
        this.capacity = capacity;
        this.isVip = isVip;
        this.status = new SimpleObjectProperty<>(TableStatus.AVAILABLE);
        this.currentCustomer = new SimpleStringProperty("None");
    }

    // --- Logic ---
    public void occupy(String customerName) {
        this.status.set(TableStatus.OCCUPIED);
        this.currentCustomer.set(customerName);
    }

    public void reserve(String customerName) {
        this.status.set(TableStatus.RESERVED);
        this.currentCustomer.set(customerName);
    }

    public void free() {
        this.status.set(TableStatus.AVAILABLE);
        this.currentCustomer.set("None");
    }

    // --- Persistence Helpers ---
    public String toCSV() {
        return id + "," + capacity + "," + isVip + "," + status.get() + "," + currentCustomer.get();
    }

    public static Table fromCSV(String line) {
        String[] p = line.split(",");
        if (p.length < 5) return null;
        Table t = new Table(p[0], Integer.parseInt(p[1]), Boolean.parseBoolean(p[2]));
        t.status.set(TableStatus.valueOf(p[3]));
        t.currentCustomer.set(p[4]);
        return t;
    }

    // --- Getters ---
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public boolean isVip() { return isVip; }
    public TableStatus getStatus() { return status.get(); }
    public String getCurrentCustomer() { return currentCustomer.get(); }
    
    public ObjectProperty<TableStatus> statusProperty() { return status; }
}