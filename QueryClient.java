package com.socket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import com.sgbd.Relation;

public class QueryClient {
    public static void main(String[] args) throws IOException {

        ConfigLoader loader = new ConfigLoader();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the host (default: localhost): ");
        String host = scanner.nextLine();
        if (host.isEmpty()) {
            host = "localhost"; 
        }

        System.out.print("Enter the port (default: " + loader.getOriginalPort() + "): ");
        int port = loader.getOriginalPort();
        try {
            String portInput = scanner.nextLine();
            if (!portInput.isEmpty()) {
                port = Integer.parseInt(portInput);
            }
        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid port number. Using default port " + loader.getOriginalPort() + ".");
        }

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            System.out.println("Connected to the server at " + host + ":" + port);

            while (true) {
                System.out.print("LightSql>: ");
                StringBuilder sqlQuery = new StringBuilder();
                String line;

                while (!(line = scanner.nextLine()).endsWith(";")) {
                    sqlQuery.append(line).append(" ");
                    System.out.print(">>"); 
                }

                sqlQuery.append(line.substring(0,line.length()-1));

                out.writeObject(sqlQuery.toString());
                out.flush();
    
                Object response = in.readObject();
                if (response instanceof String) {
                    System.out.println(response);
                } else {
                    Relation relation = (Relation) response;
                    relation.afficher();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERROR] An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
