package com.dbmanager;


import com.sgbd.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class JSONDatabaseManager {
    private final ObjectMapper mapper;
    String dbPath;

    public JSONDatabaseManager() {
        this.mapper = new ObjectMapper();
    }

    public String getDbPath() {
        return this.dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @SuppressWarnings("deprecation")
    public void createDatabase(String dbName) throws IOException {

        File dbFile = new File(this.dbPath+"/"+"databases.json");

        ObjectNode databases;
        if (dbFile.exists()) {
            databases = (ObjectNode) mapper.readTree(dbFile);
        } else {
            databases = mapper.createObjectNode();
        }

        if (databases.has(dbName)) {
            System.out.println("Database '" + dbName + "' already exists.");
            return;
        }

        String folderPath = this.dbPath+"/"+dbName.toLowerCase()+"/";

        Path path = Paths.get(folderPath);

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
        }

        databases.put(dbName, mapper.createArrayNode()); 
        mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, databases);
        System.out.println("Database '" + dbName + "' created successfully.");
    }

    public boolean checkIfDatabaseExists(String dbName) throws Exception {
        File dbFile = new File(this.dbPath + "/databases.json");
    
        if (!dbFile.exists()) {
            System.out.println("No database created");
            return false;
        }
    
        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        if (!databases.has(dbName)) {
            System.out.println("Database '" + dbName + "' does not exist.");
            return false;
        }

        return true;
    }

    public void createTable(String dbName, String tableName, Domaine[] domaines) throws IOException {
        File dbFile = new File(this.dbPath + "/databases.json");
    
        if (!dbFile.exists()) {
            System.out.println("No database created");
            return;
        }
    
        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        if (!databases.has(dbName)) {
            System.out.println("Database '" + dbName + "' does not exist.");
            return;
        }
    
        // Charger ou créer un fichier pour les tables
        File tablesFile = new File(this.dbPath + "/" + dbName.toLowerCase() + "/tables_" + dbName + ".json");
        ObjectNode tables;
        if (tablesFile.exists()) {
            tables = (ObjectNode) mapper.readTree(tablesFile);
        } else {
            tables = mapper.createObjectNode();
        }
    
        if (tables.has(tableName)) {
            System.out.println("Table '" + tableName + "' already exists in database '" + dbName + "'.");
            return;
        }
    
        ArrayNode columnsArray = mapper.createArrayNode();
        for (Domaine domaine : domaines) {
            ObjectNode domaineNode = mapper.createObjectNode();
            domaineNode.put("NOM", domaine.getNom().toUpperCase());
            domaineNode.put("TYPE", domaine.getType().toUpperCase());
            columnsArray.add(domaineNode);
        }
    
        // Créer la structure de la table
        ObjectNode table = mapper.createObjectNode();
        table.put("NOM", tableName);
        table.set("DOMAINES", columnsArray);
    
        // Ajouter la table dans l'ObjectNode des tables
        tables.set(tableName, table);
    
        // Écrire les tables dans le fichier
        mapper.writerWithDefaultPrettyPrinter().writeValue(tablesFile, tables);
    
        // Ajouter la table dans la liste des tables de la base
        ArrayNode tableList = (ArrayNode) databases.get(dbName);
        tableList.add(tableName);
        databases.set(dbName, tableList);
    
        // Écrire les bases de données dans le fichier
        mapper.writeValue(dbFile, databases);
    
        System.out.println("Table '" + tableName + "' created in database '" + dbName + "'.");
    }
    

    public void checkIfTableExists(String dbName,String tableName) throws IOException {

        dbName = dbName.toUpperCase();
        tableName = tableName.toUpperCase();

        File databaseFile = new File(this.dbPath+"/"+"databases.json");
        ObjectNode databaseNode = (ObjectNode) mapper.readTree(databaseFile);
        if (!databaseNode.has(dbName)) {
            throw new IOException("Database '" + dbName + "' does not exist.");
        }

        ArrayNode tablesArray = (ArrayNode) databaseNode.get(dbName);

        boolean tableExists = false;
        for (int i = 0; i < tablesArray.size(); i++) {
            if (tablesArray.get(i).asText().equalsIgnoreCase(tableName)) {
                tableExists = true;
                break;
            }
        } 

        if(!tableExists) {
            throw new IOException("Table "+tableName+" doesn't exist");
        }
    }

    public Domaine[] getDomaineFromJSON(String dbName,String tableName) throws IOException {

        this.checkIfTableExists(dbName,tableName);

        File tableFile = new File(this.dbPath+"/"+dbName.toLowerCase()+"/"+"tables_"+dbName+".json");

        ObjectNode tableList = (ObjectNode) mapper.readTree(tableFile);

        ObjectNode table = (ObjectNode) tableList.get(tableName);

        ArrayNode domainesNode = (ArrayNode) table.get("DOMAINES");

        Domaine domaines[] = new Domaine[domainesNode.size()];

        for(int i= 0; i < domainesNode.size() ; i++) {
            ObjectNode domaineNode = (ObjectNode) domainesNode.get(i);
            Domaine domaine = new Domaine(domaineNode.get("NOM").asText(),domaineNode.get("TYPE").asText());
            domaines[i] = domaine;
        }

        return domaines;
        
    }

    public Nuplet getNupletFromInput(String[] columns,Object[] values) throws Exception {
        Nuplet nuplet =  new Nuplet();
        if(columns.length != values.length) {
            throw new Exception("Length of column and values don't match");
        }
        for(int i = 0; i < columns.length; i++) {
            nuplet.add(columns[i],values[i]);
        }
        return nuplet;
    }

    public Nuplet[] getNupletsFromJSON(String dbName,String tableName) throws Exception {
        File dataFile = new File(this.dbPath+"/"+dbName.toLowerCase()+"/"+"data_"+tableName+".json");
        ArrayNode nupletsNode = (ArrayNode) mapper.readTree(dataFile);
        Nuplet nuplets[] = new Nuplet[nupletsNode.size()];
        HashMap<String,String> mappedDomaines = this.getDomaine(dbName,tableName);
        for(int i = 0; i < nupletsNode.size(); i++) {
            ObjectNode nupletNode = (ObjectNode) nupletsNode.get(i);
            Iterator<String> fieldNames = nupletNode.fieldNames();
            Nuplet nuplet = new Nuplet();
            while(fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                Object value = null;
                if(fieldName.startsWith("_")) {
                    value = JSONStringParser.parseValue(nupletNode.get(fieldName).asText(),"INT");
                } else {
                    value = JSONStringParser.parseValue(nupletNode.get(fieldName).asText(),mappedDomaines.get(fieldName));
                }
                
                nuplet.add(fieldName,value);
            }
            nuplets[i] = nuplet;
        }
        return nuplets;
    }

    public Relation getRelationFromJSON(String dbName,String tableName) throws Exception {
        ArrayList<Domaine> domaines = new ArrayList<>(Arrays.asList(this.getDomaineFromJSON(dbName,tableName)));
        ArrayList<Nuplet> nuplets = new ArrayList<>(Arrays.asList(this.getNupletsFromJSON(dbName,tableName)));
        Relation relation = new Relation(domaines,nuplets);
        return relation;
    }

    public void checkNupletIntegrity(String dbName,String tableName, String[] columns, Object[] values) throws Exception {
        Domaine[] domaines = this.getDomaineFromJSON(dbName,tableName);
        Nuplet nuplet = this.getNupletFromInput(columns,values);
        for(Domaine domaine:domaines) {
            Object value = nuplet.get(domaine.getNom());
            boolean validation = domaine.validate(value);
            if(!validation) {
                throw new Exception("Invalid input for column "+domaine.getNom());
            }
        }
    }

    public HashMap<String,String> getDomaineFromColumns(String dbName,String tableName,String[] columns) throws Exception {
        HashMap<String,String> mappedDomaines = new HashMap<>();
        Domaine[] domaines = this.getDomaineFromJSON(dbName,tableName);
        for(String column:columns) {
            for(Domaine domaine:domaines) {
                if(domaine.getNom().equals(column)) {
                    mappedDomaines.put(column,domaine.getType());
                }
            }
        }

        return mappedDomaines;
    }

    
    public HashMap<String,String> getDomaine(String dbName,String tableName) throws Exception {
        HashMap<String,String> mappedDomaines = new HashMap<>();
        Domaine[] domaines = this.getDomaineFromJSON(dbName,tableName);
        for(Domaine domaine:domaines) {
            mappedDomaines.put(domaine.getNom(),domaine.getType());
        }

        return mappedDomaines;
    }

    public void insertData(String dbName,String tableName, String[] columns, String[] values) throws Exception {

        this.checkIfTableExists(dbName,tableName);

        HashMap<String,String> mappedDomaines = this.getDomaineFromColumns(dbName,tableName,columns);

        Object[] parsedValues = new Object[values.length];

        for(int i = 0 ; i < values.length;i++) {
            parsedValues[i] = JSONStringParser.parseValue(values[i],mappedDomaines.get(columns[i]));
        }

        this.checkNupletIntegrity(dbName,tableName,columns,parsedValues);

        File dataFile = new File(this.dbPath+"/"+dbName.toLowerCase()+"/"+"data_" + tableName + ".json");
        ArrayNode data;

        if (dataFile.exists()) {
            data = (ArrayNode) mapper.readTree(dataFile);
        } else {
            data = mapper.createArrayNode();
        }

        ObjectNode row = mapper.createObjectNode();
        for (int i = 0; i < columns.length; i++) {
            row.put(columns[i], parsedValues[i].toString());
        }

        row.put("_index", data.size());
        data.add(row);
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, data);

    }

    public void deleteData(String dbName, String tableName, String targetColumn, Object value) throws Exception {
        tableName = tableName.toUpperCase();
        final String finalTargetColumn = targetColumn.toUpperCase(); // Copie finale

        final HashMap<String, String> mappedDomaines = this.getDomaine(dbName, tableName); // Copie finale

        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(this.dbPath + dbName.toLowerCase() + "/" + "data_" + tableName + ".json");

        List<Map<String, Object>> jsonList = mapper.readValue(jsonFile, new TypeReference<List<Map<String, Object>>>() {});

        jsonList.removeIf(obj -> obj.get("_index") != null && 
                JSONStringParser.parseValue(obj.get(finalTargetColumn).toString(), mappedDomaines.get(finalTargetColumn)).equals(
                        JSONStringParser.parseValue(value.toString(), mappedDomaines.get(finalTargetColumn))
                ));

        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonList);
    }

    public void deleteData(String dbName, String tableName, Relation relation) throws Exception {
        tableName = tableName.toUpperCase();
    
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(this.dbPath+"/"+ dbName.toLowerCase() + "/" + "data_" + tableName + ".json");
    
        // Charger les données JSON
        List<Map<String, Object>> jsonList = mapper.readValue(jsonFile, new TypeReference<List<Map<String, Object>>>() {});
    
        // Créer un ensemble des index à supprimer
        Set<Object> indexesToDelete = new HashSet<>();
        for (Nuplet nuplet : relation.getNuplets()) {
            indexesToDelete.add(nuplet.get("_index"));
        }
    
        // Supprimer les objets correspondants
        jsonList.removeIf(obj -> obj.get("_index") != null && indexesToDelete.contains(obj.get("_index")));
    
        // Écrire les données mises à jour
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonList);
    }

    public void manageDatabase(String operation, String dbName, String tableName, Map<String, Object> updateData, String conditionColumn, Object conditionValue) throws Exception {
        switch (operation.toLowerCase()) {
            case "drop_table":
                dropTable(dbName, tableName);
                break;
    
            case "drop_database":
                dropDatabase(dbName);
                break;
    
            case "update_table":
                updateTable(dbName, tableName, updateData, conditionColumn, conditionValue);
                break;
    
            default:
                throw new IllegalArgumentException("Invalid operation. Supported operations: drop_table, drop_database, update_table.");
        }
    }
    
    public void dropTable(String dbName, String tableName) throws IOException {
        checkIfTableExists(dbName, tableName);
    
        File tablesFile = new File(this.dbPath + "/" + dbName.toLowerCase() + "/tables_" + dbName + ".json");
        ObjectNode tables = (ObjectNode) mapper.readTree(tablesFile);
        tables.remove(tableName);
    
        mapper.writerWithDefaultPrettyPrinter().writeValue(tablesFile, tables);
    
        File dataFile = new File(this.dbPath + "/" + dbName.toLowerCase() + "/data_" + tableName + ".json");
        if (dataFile.exists()) {
            Files.delete(dataFile.toPath());
        }
    
        File dbFile = new File(this.dbPath + "/databases.json");
        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        ArrayNode tableList = (ArrayNode) databases.get(dbName);
        for (int i = 0; i < tableList.size(); i++) {
            if (tableList.get(i).asText().equalsIgnoreCase(tableName)) {
                tableList.remove(i);
                break;
            }
        }
        databases.set(dbName, tableList);
        mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, databases);
    
        System.out.println("Table '" + tableName + "' dropped successfully from database '" + dbName + "'.");
    }
    
    public void dropDatabase(String dbName) throws IOException {
        File dbFile = new File(this.dbPath + "/databases.json");
        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        if (!databases.has(dbName)) {
            throw new IOException("Database '" + dbName + "' does not exist.");
        }
    
        File dbFolder = new File(this.dbPath + "/" + dbName.toLowerCase());
        if (dbFolder.exists()) {
            for (File file : Objects.requireNonNull(dbFolder.listFiles())) {
                Files.delete(file.toPath());
            }
            Files.delete(dbFolder.toPath());
        }
    
        databases.remove(dbName);
        mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, databases);
    
        System.out.println("Database '" + dbName + "' dropped successfully.");
    }
    
    public void updateTable(String dbName, String tableName, Map<String, Object> updateData, String conditionColumn, Object conditionValue) throws Exception {
        checkIfTableExists(dbName, tableName);
    
        File dataFile = new File(this.dbPath + "/" + dbName.toLowerCase() + "/data_" + tableName + ".json");
        ArrayNode data = (ArrayNode) mapper.readTree(dataFile);
    
        boolean updated = false;
        for (int i = 0; i < data.size(); i++) {
            ObjectNode row = (ObjectNode) data.get(i);
            if (row.has(conditionColumn) && row.get(conditionColumn).asText().equalsIgnoreCase(conditionValue.toString())) {
                for (Map.Entry<String, Object> entry : updateData.entrySet()) {
                    row.put(entry.getKey(), entry.getValue().toString());
                }
                updated = true;
            }
        }
    
        if (!updated) {
            throw new Exception("No matching row found to update.");
        }
    
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, data);
        System.out.println("Table '" + tableName + "' updated successfully in database '" + dbName + "'.");
    }

    public String displayAllDatabases() throws IOException {
        StringBuilder result = new StringBuilder();
        File dbFile = new File(this.dbPath + "/databases.json");

        if (!dbFile.exists()) {
            result.append("No databases available.\n");
            return result.toString();
        }

        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        if (databases.size() == 0) {
            result.append("No databases found.\n");
            return result.toString();
        }

        result.append("Databases:\n");
        databases.fieldNames().forEachRemaining(databaseName -> {
            result.append("- ").append(databaseName).append("\n");
        });

        return result.toString();
    }

    public String displayAllTables(String dbName) throws IOException {
        StringBuilder result = new StringBuilder();
        File dbFile = new File(this.dbPath + "/databases.json");

        if (!dbFile.exists()) {
            result.append("No databases created.\n");
            return result.toString();
        }

        ObjectNode databases = (ObjectNode) mapper.readTree(dbFile);
        if (!databases.has(dbName)) {
            result.append("Database '").append(dbName).append("' does not exist.\n");
            return result.toString();
        }

        ArrayNode tableList = (ArrayNode) databases.get(dbName);
        if (tableList.size() == 0) {
            result.append("No tables found in database '").append(dbName).append("'.\n");
            return result.toString();
        }

        result.append("Tables in database '").append(dbName).append("':\n");
        for (int i = 0; i < tableList.size(); i++) {
            result.append("- ").append(tableList.get(i).asText()).append("\n");
        }

        return result.toString();
    }

}


