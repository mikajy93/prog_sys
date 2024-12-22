package com.socket;

import java.sql.*;
import java.util.Properties;

public class MyClient {

    public static void main(String[] args) {
        try {
            Class.forName("com.jdbc.MyDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver non trouvé : " + e.getMessage());
            return;
        }

        String url = "jdbc:mydriver://localhost:8080/market";
        Properties properties = new Properties();
        properties.setProperty("dbName", "my_database");

        try {
            Connection connection = DriverManager.getConnection(url, properties);
            Statement statement = connection.createStatement();
            String insertQuery1 = "INSERT INTO PRODUITS (ID, NOM, CATEGORIE, PRIX, STOCK, DISPONIBLE) VALUES " +
                      "(1, 'Ordinateur Portable', 'Électronique', 800.00, 50, TRUE), " +
                      "(2, 'Smartphone', 'Électronique', 600.00, 0, FALSE), " +
                      "(3, 'Casque Audio', 'Électronique', 100.00, 200, TRUE), " +
                      "(4, 'T-Shirt', 'Vêtements', 20.00, 150, TRUE), " +
                      "(5, 'Chaussures de Sport', 'Vêtements', 50.00, 30, TRUE), " +
                      "(6, 'Télévision', 'Électronique', 1200.00, 10, TRUE), " +
                      "(7, 'Canapé', 'Meubles', 500.00, 5, TRUE), " +
                      "(8, 'Table', 'Meubles', 150.00, 0, FALSE), " +
                      "(9, 'Livre', 'Divers', 15.00, 300, TRUE), " +
                      "(10, 'Vélo', 'Sport', 250.00, 20, TRUE)";

            // int st = statement.executeUpdate(insertQuery1);
            String query = "create database import";
            int st = statement.executeUpdate(query);
            // String q = "create table produits (id int,nom varchar,categorie varchar,prix double,stock double,disponible boolean)";
            // int sst = statement.executeUpdate(q);
            // int stt = statement.executeUpdate(insertQuery1);
            // ResultSet set = statement.executeQuery("select * from produits");
            // while(set.next()) {
            //     System.out.println(set.getString("ID")+"|"+set.getString("NOM")+"|"+set.getString("CATEGORIE")+"|"+set.getString("PRIX")+"|"+set.getString("DISPONIBLE"));
            // }
            connection.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion ou de l'exécution de la requête : " + e.getMessage());
            e.printStackTrace();
        } finally {
        }
    }
}
