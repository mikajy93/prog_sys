package com.socket;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ConfigLoader {
    private static final String CONFIG_FILE = "./lightsql_db/conf/config.txt";
    private Map<Integer, String> dbPaths;
    private Map<Integer, String> dbTypes;

    public ConfigLoader() throws IOException {
        dbPaths = new HashMap<>();
        dbTypes = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() throws IOException {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            throw new FileNotFoundException("Fichier de configuration non trouvé : " + CONFIG_FILE);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Ligne de configuration invalide : " + line);
                }

                int port = Integer.parseInt(parts[0].trim());
                String type = parts[1].trim();
                String dbPath = parts[2].trim();

                dbPaths.put(port, dbPath);
                dbTypes.put(port, type);
            }
        }
    }

    public String getDbPath(int port) {
        return dbPaths.get(port);
    }

    public List<Integer> getReplicaPorts() {
        List<Integer> replicaPorts = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : dbTypes.entrySet()) {
            if ("replica".equalsIgnoreCase(entry.getValue())) {
                replicaPorts.add(entry.getKey());
            }
        }
        return replicaPorts;
    }

    public int getOriginalPort() {
        int port = -1; 
        for (Map.Entry<Integer, String> entry : dbTypes.entrySet()) {
            if ("original".equalsIgnoreCase(entry.getValue())) {
                port = entry.getKey();
            }
        }
        return port;
    }

    public static void main(String[] args) {
        try {
            ConfigLoader loader = new ConfigLoader();
            
            int port = 8081;
            String dbPath = loader.getDbPath(port);
            if (dbPath != null) {
                System.out.println("Chemin de la base pour le port " + port + " : " + dbPath);
            } else {
                System.out.println("Aucune base trouvée pour le port " + port);
            }

            List<Integer> replicaPorts = loader.getReplicaPorts();
            System.out.println("Ports des bases répliquées : " + replicaPorts);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
        }
    }
}
