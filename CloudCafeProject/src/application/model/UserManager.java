package application.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Map<String, User> users;
    private static final String USER_FILE = "users.csv";

    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public User authenticate(String id, String password) {
        User u = users.get(id);
        if (u != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }
    
    // --- NEW METHOD ---
    public void addUser(User newUser) {
        users.put(newUser.getId(), newUser);
        saveUsers(); // Auto-save when adding
    }
    
    public Map<String, User> getUsers() { return users; }
    
    public User getUser(String id) {
        return users.get(id);
    }

    public void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            initializeDefaults();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                User u = User.fromCSV(line);
                if (u != null) users.put(u.getId(), u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            pw.println("ID,Name,Role,Points,Password");
            for (User u : users.values()) {
                pw.println(u.toCSV());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDefaults() {
        users.put("M001", new User("M001", "KC Carr", "Manager", 0, "1234"));
        users.put("C001", new User("C001", "Nigel Cashier", "Cashier", 0, "1234"));
        users.put("U001", new User("U001", "Sophia Common", "Common", 50, "1234"));
        users.put("V001", new User("V001", "Antoni VIP", "VIP", 120, "1234"));
        saveUsers();
    }
}