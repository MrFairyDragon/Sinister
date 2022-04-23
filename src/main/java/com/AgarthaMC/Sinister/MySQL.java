package com.AgarthaMC.Sinister;

import java.sql.*;
import java.util.Properties;

public class MySQL {

    private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://eu01-sql.pebblehost.com:3306/customer_259852_agartha";
    private static final String USERNAME = "customer_259852_agartha";
    private static final String PASSWORD = "6SYJec$D~90rC90kndBi";
    private static final String MAX_POOL = "250";

    private Connection connection;
    private Properties properties;

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Could not connect to MYSQL database is the information wrong?");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
