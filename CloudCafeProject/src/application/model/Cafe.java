package application.model;

public class Cafe {
    private Inventory inventory;
    private Menu menu;
    private UserManager userManager;
    private TableManager tableManager; // <--- NEW

    public Cafe() {
        this.inventory = new Inventory();
        this.menu = new Menu();
        this.userManager = new UserManager();
        this.tableManager = new TableManager(); // <--- NEW
    }

    public Inventory getInventory() { return inventory; }
    public Menu getMenu() { return menu; }
    public UserManager getUserManager() { return userManager; }
    public TableManager getTableManager() { return tableManager; } // <--- NEW

    public void saveAll() {
        inventory.saveInventory();
        menu.saveMenu();
        userManager.saveUsers();
        tableManager.saveTables(); // <--- NEW
    }
}