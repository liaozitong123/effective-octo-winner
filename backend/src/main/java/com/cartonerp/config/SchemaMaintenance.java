package com.cartonerp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

@Component
public class SchemaMaintenance implements CommandLineRunner {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String productName = databaseProductName();
        maintainDeliveryNoteNo(productName);
        if (!isMySql(productName)) return;
        jdbcTemplate.execute("alter table production_orders modify notes longtext");
        ensureColumn("production_records", "nailer", "varchar(60)");
        ensureColumn("production_records", "production_date", "date");
        ensureColumn("production_records", "delivery_qty", "int");
        ensureColumn("production_records", "remaining_stock", "int");
        ensureColumn("production_records", "delivery_date", "date");
        ensureColumn("production_records", "driver", "varchar(60)");
    }

    private void maintainDeliveryNoteNo(String productName) {
        if (productName == null) return;
        String normalized = productName.toLowerCase();
        try {
            if (normalized.contains("mysql")) {
                jdbcTemplate.execute("alter table delivery_notes modify note_no varchar(40) null");
                dropMySqlUniqueIndexes("delivery_notes", "note_no");
            } else if (normalized.contains("h2")) {
                jdbcTemplate.execute("alter table delivery_notes alter column note_no set null");
                dropH2UniqueConstraints("delivery_notes", "note_no");
            }
        } catch (Exception ignored) {
            // The table may not exist yet on a fresh database; Hibernate will create it from the entity mapping.
        }
    }

    private void dropMySqlUniqueIndexes(String table, String column) {
        List<String> indexes = jdbcTemplate.queryForList(
            "select index_name from information_schema.statistics " +
                "where table_schema = database() and table_name = ? and column_name = ? " +
                "and non_unique = 0 and index_name <> 'PRIMARY'",
            String.class,
            table,
            column
        );
        for (String index : indexes) {
            jdbcTemplate.execute("alter table " + table + " drop index " + quoteMySqlIdentifier(index));
        }
    }

    private void dropH2UniqueConstraints(String table, String column) {
        List<String> constraints = jdbcTemplate.queryForList(
            "select tc.constraint_name from information_schema.table_constraints tc " +
                "join information_schema.key_column_usage kcu " +
                "on tc.constraint_catalog = kcu.constraint_catalog " +
                "and tc.constraint_schema = kcu.constraint_schema " +
                "and tc.constraint_name = kcu.constraint_name " +
                "where upper(tc.table_name) = upper(?) and upper(kcu.column_name) = upper(?) " +
                "and tc.constraint_type = 'UNIQUE'",
            String.class,
            table,
            column
        );
        for (String constraint : constraints) {
            jdbcTemplate.execute("alter table " + table + " drop constraint " + quoteH2Identifier(constraint));
        }
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

    private String databaseProductName() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isMySql(String productName) {
        return productName != null && productName.toLowerCase().contains("mysql");
    }

    private String quoteMySqlIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String quoteH2Identifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
