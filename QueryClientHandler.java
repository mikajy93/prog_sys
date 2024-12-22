package com.socket;

import com.compiler.SQLCompiler;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryClientHandler extends Thread {

    private DatabaseServer server;
    private final Socket socket;
    private final String host;
    private final int port;
    private ConfigLoader loader;
    
        public ConfigLoader getLoader() {
            return loader;
        }
    
    
        public Socket getSocket() {
            return socket;
        }
    
    
        public String getHost() {
            return host;
        }
    
    
        public int getPort() {
            return port;
        }

        public DatabaseServer getServer() {
            return server;
        }
    
    
        public void setServer(DatabaseServer server) {
            this.server = server;
        }
    
    
        public QueryClientHandler(DatabaseServer server,Socket socket, String host, int port) {
            this.server = server;
            this.socket = socket;
            this.host = host;
            this.port = port;
            this.loader = null;
            try {
                this.loader = new ConfigLoader();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            SQLCompiler compiler = new SQLCompiler("rootdb");

            String dbPath = this.loader.getDbPath(this.port);

            compiler.setDbPath(dbPath);

            while (true) {
                try {
                    String sql = (String) in.readObject();
                    System.out.println("[INFO] Received query: \"" + sql + "\"");

                    if(sql.startsWith("jdbc")) {
                        sql = sql.split(":")[1];
                        compiler.setDbName(sql);
                        continue;
                    }

                    if(this.server instanceof DatabaseServerSlave && (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("CREATE"))) {
                        System.out.println("[INFO] Database on read-only, refused query");
                        out.writeObject("Not permitted on read-only");
                        continue;
                    } 
                    Object queryResult = compiler.compile(sql);
                    System.out.println("[SUCCESS] Query compiled successfully!");

                    if(this.server instanceof DatabaseServerMaster) {
                        List<Integer> replicaPorts = this.loader.getReplicaPorts();
                        for(int replicaPort:replicaPorts) {
                            SQLCompiler replicaCompiler = new SQLCompiler(compiler.getDbName(),this.loader.getDbPath(replicaPort));
                            replicaCompiler.compile(sql);
                        }
                    }

                    
                    out.writeObject(queryResult);
                    out.flush();
                    if(this.server instanceof DatabaseServerMaster) {
                        writeLog(sql);
                    }
                    System.out.println("[INFO] Query result sent to client.");
                } catch (EOFException e) {
                    System.out.println("[INFO] Client disconnected.");
                    break;
                } catch (Exception e) {
                    System.err.println("[ERROR] Error while processing query: " + e.getMessage());
                    e.printStackTrace();
                    out.writeObject("[ERROR] An error occurred: " + e.getMessage());
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Issue with client connection: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Écrit une requête réussie dans un fichier log.
     *
     * @param sql La requête SQL à écrire dans le fichier log.
     */
    private void writeLog(String sql) {
        String logFilePath = "./lightsql_db/log/log.txt";
        try (FileWriter writer = new FileWriter(logFilePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String logEntry = String.format("[%s] Query executed: %s%n", timestamp, sql);

            bufferedWriter.write(logEntry);
            System.out.println("[INFO] Query logged successfully: " + sql);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write to log file: " + e.getMessage());
        }
    }
}
