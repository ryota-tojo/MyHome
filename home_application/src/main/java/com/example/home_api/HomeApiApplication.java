package com.example.home_api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeApiApplication {

    public static void main(String[] args) {
        loadLocalDatabaseSettings();
        SpringApplication.run(HomeApiApplication.class, args);
    }

    /**
     * DockerではComposeから環境変数を受け取り、IDE/bootRunではプロジェクト直下の.envを使う。
     * 秘密情報をapplication.propertiesへ固定しないための開発用ブートストラップ。
     */
    private static void loadLocalDatabaseSettings() {
        Dotenv dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
        String host = setting(dotenv, "DB_HOST", "localhost");
        boolean docker = "docker".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        if (!docker && (host.equals("postgres") || host.equals("database_container"))) host = "localhost";
        setIfAbsent("spring.datasource.url", "jdbc:postgresql://" + host + ":" + setting(dotenv,"DB_PORT","5432") + "/" + setting(dotenv,"DB_NAME","home_db"));
        setIfAbsent("spring.datasource.username", setting(dotenv,"DB_USER","postgres"));
        setIfAbsent("spring.datasource.password", setting(dotenv,"DB_PASSWORD","postgres"));
    }

    private static String setting(Dotenv dotenv, String key, String fallback) {
        String environment = System.getenv(key);
        if (environment != null && !environment.isBlank()) return environment;
        String local = dotenv.get(key);
        return local == null || local.isBlank() ? fallback : local;
    }

    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) System.setProperty(key, value);
    }

}
