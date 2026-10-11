package mg.itu.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;

public class Database {
    private static String driver;
    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    public static void init(ServletContext context) {
        System.out.println("[Database] === INITIALISATION ===");

        if (initialized) {
            System.out.println("[Database] Déjà initialisée");
            return;
        }

        driver = context.getInitParameter("db.driver");
        url = context.getInitParameter("db.url");
        user = context.getInitParameter("db.user");
        password = context.getInitParameter("db.password");

        System.out.println("[Database] Driver: " + driver);
        System.out.println("[Database] URL: " + url);
        System.out.println("[Database] User: " + user);

        if (driver != null && url != null) {
            try {
                Class.forName(driver);
                initialized = true;
                System.out.println("[Database] Base de données configurée avec succès");
            } catch (ClassNotFoundException e) {
                System.err.println("[Database] Driver JDBC non trouvé: " + driver);
                e.printStackTrace();
            }
        } else {
            System.err.println("[Database] Paramètres de base de données manquants !");
            System.err.println("[Database] driver=" + driver);
            System.err.println("[Database] url=" + url);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Base de données non initialisée. Vérifie les paramètres db dans web.xml");
        }
        return DriverManager.getConnection(url, user, password);
    }
}