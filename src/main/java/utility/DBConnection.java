package utility; // пул соед с бд

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import config.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.getProperty("db.url"));
        config.setUsername(Config.getProperty("db.user"));
        config.setPassword(Config.getProperty("db.pass"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(Config.getIntProperty("db.pool.size"));
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException { // возвр соед всем потокам
        return dataSource.getConnection(); // не нужно новое подкл на каждый запрос
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}