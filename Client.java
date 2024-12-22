package com.socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Client {
       public static void main(String[] args) {
        try {
            Class.forName("com.jdbc.MyDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver non trouvé : " + e.getMessage());
            return;
        }

        String url = "jdbc:mydriver://localhost:3000/cooperative";
        Properties properties = new Properties();
        properties.setProperty("dbName", "my_database");

        try {
            Connection connection = DriverManager.getConnection(url, properties);
            Statement statement = connection.createStatement();
            // String query = "INSERT INTO VEHICLE (ID, MODEL, MARQUE, MONTANT) VALUES "+ 
            //     "(1, 'ModelX', 'Tesla', 79999.99), "+
            //     "(2, 'Civic', 'Honda', 22000.00), "+ 
            //     "(3, 'Corolla', 'Toyota', 25000.50), "+
            //     "(4, 'Mustang', 'Ford', 55000.00), "+
            //     "(5, '911 Carrera', 'Porsche', 120000.75), "+
            //     "(6, 'i3', 'BMW', 45000.00), "+
            //     "(7, 'Leaf', 'Nissan', 30000.99), "+
            //     "(8, 'Model 3', 'Tesla', 39999.99), "+
            //     "(9, 'A6', 'Audi', 60000.00), "+
            //     "(10, 'CX-5', 'Mazda', 35000.49)";
            
            
            String query = "create table vehicle (id int, model varchar,marque varchar,montant double)";
            int st = statement.executeUpdate(query);

            ResultSet set = statement.executeQuery("select * from vehicle");
            while(set.next()) {
                System.out.println(set.getString("ID")+"|"+set.getString("MODEL")+"|"+set.getString("MARQUE")+"|"+set.getString("MONTANT"));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion ou de l'exécution de la requête : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // connection.close();
        }
    }
}
