package com.vyapaar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the AI-written SQL with its own read-only MySQL account, over a plain JDBC connection:
 * read-only mode, a 10 second timeout and a 200 row cap.
 *
 * It deliberately does NOT use the application's main DataSource, for two reasons. The main
 * account can write, and this one must not. And declaring a second DataSource bean would switch
 * off Spring Boot's automatic configuration of the primary one and break the rest of the app.
 */
@Service
public class ChatSqlRunner {

    public static final int MAX_ROWS = 200;
    private static final int TIMEOUT_SECONDS = 10;

    private final String url;
    private final String username;
    private final String password;

    public ChatSqlRunner(@Value("${chat.db.url}") String url,
                         @Value("${chat.db.username:}") String username,
                         @Value("${chat.db.password:}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /** True when the read-only account is configured. */
    public boolean isConfigured() {
        return username != null && !username.isBlank();
    }

    public QueryResult run(String sql) throws SQLException {
        if (!isConfigured()) {
            throw new IllegalStateException("CHAT_DB_USERNAME is not set");
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            connection.setReadOnly(true);
            statement.setQueryTimeout(TIMEOUT_SECONDS);
            statement.setMaxRows(MAX_ROWS);

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                ResultSetMetaData meta = resultSet.getMetaData();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String label = meta.getColumnLabel(i);
                    String unique = label;
                    int copy = 2;
                    while (columns.contains(unique)) {
                        unique = label + "_" + copy++;
                    }
                    columns.add(unique);
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 0; i < columns.size(); i++) {
                        row.put(columns.get(i), simplify(resultSet.getObject(i + 1)));
                    }
                    rows.add(row);
                }
                return new QueryResult(columns, rows);
            }
        }
    }

    // Dates, decimals and the like become plain strings or numbers so the JSON stays simple.
    private Object simplify(Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value.toString();
    }

    public record QueryResult(List<String> columns, List<Map<String, Object>> rows) {
    }
}
