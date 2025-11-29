package application.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TableManager {
    private Map<String, Table> tables;
    private static final String TABLE_FILE = "tables.csv";

    public TableManager() {
        tables = new HashMap<>();
        loadTables();
    }

    public Map<String, Table> getTables() { return tables; }
    public Table getTable(String id) { return tables.get(id); }

    public void loadTables() {
        File file = new File(TABLE_FILE);
        if (!file.exists()) {
            initializeDefaultTables();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                Table t = Table.fromCSV(line);
                if (t != null) tables.put(t.getId(), t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTables() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TABLE_FILE))) {
            pw.println("ID,Capacity,IsVIP,Status,Customer");
            for (Table t : tables.values()) {
                pw.println(t.toCSV());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDefaultTables() {
        // 2 VIP Tables (4 Seater)
        tables.put("VIP 1", new Table("VIP 1", 4, true));
        tables.put("VIP 2", new Table("VIP 2", 4, true));

        // 3 Regular Tables (4 Seater) - Center Area
        tables.put("Table 1", new Table("Table 1", 4, false));
        tables.put("Table 2", new Table("Table 2", 4, false));
        tables.put("Table 3", new Table("Table 3", 4, false));

        // 5 Regular Tables (2 Seater) - Side Area
        tables.put("T4", new Table("T4", 2, false));
        tables.put("T5", new Table("T5", 2, false));
        tables.put("T6", new Table("T6", 2, false));
        tables.put("T7", new Table("T7", 2, false));
        tables.put("T8", new Table("T8", 2, false));
        
        saveTables();
    }
}