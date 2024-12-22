package com.jdbc;

import java.sql.*;
import java.util.Properties;

public class MyDriver implements Driver {

    static {
        try {
            DriverManager.registerDriver(new MyDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        String host = url.split(":")[2];
        host = host.substring(host.lastIndexOf("//")+2);
        int port = Integer.parseInt(url.split(":")[3].split("/")[0]);
        String dbName = url.split(":")[3].split("/")[1];   

        return new MyConnection(host, port, dbName);
    }

    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith("jdbc:mydriver:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        return null;
    }
}
