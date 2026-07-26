package com.cartonerp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Component
public class SchemaMaintenance implements CommandLineRunner {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (!isMySql()) return;
        jdbcTemplate.execute("alter table production_orders modify notes longtext");
        ensureColumn("production_records", "nailer", "varchar(60)");
        ensureColumn("production_records", "production_date", "date");
        ensureColumn("production_records", "delivery_qty", "int");
        ensureColumn("production_records", "remaining_stock", "int");
        ensureColumn("production_records", "delivery_date", "date");
        ensureColumn("production_records", "driver", "varchar(60)");
    }

    private void ensureColumn(String table, String column, String definition) {
        if (columnExists(table, column)) return;
        jdbcTemplate.execute("alter table " + table + " add column " + column + " " + definition);
    }

    private boolean columnExists(String table, String column) {
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMySql() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("mysql");
        } catch (Exception e) {
            return false;
        }
    }
}
