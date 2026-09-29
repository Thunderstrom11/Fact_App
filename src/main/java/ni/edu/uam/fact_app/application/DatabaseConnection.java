package ni.edu.uam.fact_app.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/tienda_javafx";

    private static final String USER =
            "postgres";

    private static final String PASSWORD =
            "diegoch1207";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
