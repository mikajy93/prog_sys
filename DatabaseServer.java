package com.socket;

import java.io.*;
import java.net.*;

public class DatabaseServer {

    private String host;
    private int port;
    private ServerSocket serverSocket;

    public DatabaseServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur en attente de connexions sur le port " + port + "...");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Connexion établie avec " + socket.getInetAddress());

                    new QueryClientHandler(this,socket,host,port).start();

                } catch (IOException e) {
                    System.err.println("Erreur lors de l'acceptation de la connexion : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du serveur : " + e.getMessage());
            }
        }
    }
}

