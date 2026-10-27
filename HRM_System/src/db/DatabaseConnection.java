package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager for MySQL.
 * Configure URL, USER, PASSWORD below to match your environment.
 */
public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/hrm_system?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASS = "123456";

    private static Connection connection;

    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-java.jar to lib/", e);
            }
        }
        return connection;
    }

    public static void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException ignored) { }
    }
}
