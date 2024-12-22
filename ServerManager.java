package com.socket;

import java.io.IOException;

public class ServerManager {
    public static void main(String[] args) throws IOException {
        ConfigLoader loader = new ConfigLoader();
        String host = "localhost";
        int port = loader.getOriginalPort();
        DatabaseServerMaster server = new DatabaseServerMaster(host, port);
        server.start();
        // DatabaseServerSlave server = new DatabaseServerSlave(host, port);
        // server.start();
    }
}
