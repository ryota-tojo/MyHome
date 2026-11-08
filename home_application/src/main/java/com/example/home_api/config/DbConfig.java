package com.example.home_api.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    private final String url;
    private final String username;
    private final String password;

    public DbConfig(String host, String port, String database, String username, String password) {
        this.url = String.format(
                "jdbc:postgresql://%s:%s/%s",
                host,
                port,
                database
        );
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static DbConfig from() {
        Dotenv dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();

        DbConfig dbConfig = new DbConfig(
                required(dotenv, "DB_HOST"),
                required(dotenv, "DB_PORT"),
                required(dotenv, "DB_NAME"),
                required(dotenv, "DB_USER"),
                required(dotenv, "DB_PASSWORD")
        );

        try {
                DriverManager.getConnection(
                        dbConfig.getUrl(),
                        dbConfig.getUsername(),
                        dbConfig.getPassword()
                );

        } catch (SQLException e) {
            return new DbConfig(
                    "localhost",
                    required(dotenv, "DB_PORT"),
                    required(dotenv, "DB_NAME"),
                    required(dotenv, "DB_USER"),
                    required(dotenv, "DB_PASSWORD")
            );
        }
         return dbConfig;
    };

    private static String required(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing env: " + key);
        }
        return value;
    }
}
