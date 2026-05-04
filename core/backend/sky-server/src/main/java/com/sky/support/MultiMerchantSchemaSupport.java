package com.sky.support;

import com.sky.properties.StorefrontProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MultiMerchantSchemaSupport {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StorefrontProperties storefrontProperties;

    private final Map<String, Boolean> tableCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> columnCache = new ConcurrentHashMap<>();

    public boolean supportsCampusTable() {
        return hasTable("campus");
    }

    public boolean supportsMerchantTable() {
        return hasTable("merchant");
    }

    public boolean supportsEmployeeScope() {
        return hasColumn("employee", "merchant_id") && hasColumn("employee", "account_type");
    }

    public boolean supportsCategoryScope() {
        return hasColumn("category", "merchant_id");
    }

    public boolean supportsDishScope() {
        return hasColumn("dish", "merchant_id");
    }

    public boolean supportsSetmealScope() {
        return hasColumn("setmeal", "merchant_id");
    }

    public boolean supportsShoppingCartScope() {
        return hasColumn("shopping_cart", "merchant_id");
    }

    public boolean supportsOrdersScope() {
        return hasColumn("orders", "campus_id")
                && hasColumn("orders", "merchant_id")
                && hasColumn("orders", "merchant_name")
                && hasColumn("orders", "goods_amount")
                && hasColumn("orders", "delivery_fee")
                && hasColumn("orders", "item_count");
    }

    public boolean isCoreSchemaReady() {
        return supportsCampusTable()
                && supportsMerchantTable()
                && supportsEmployeeScope()
                && supportsCategoryScope()
                && supportsDishScope()
                && supportsSetmealScope()
                && supportsShoppingCartScope()
                && supportsOrdersScope();
    }

    public Long getDefaultMerchantId() {
        return storefrontProperties.getShopId() == null ? 1L : storefrontProperties.getShopId();
    }

    public String getReadinessSummary() {
        return "campus=" + supportsCampusTable()
                + ", merchant=" + supportsMerchantTable()
                + ", employeeScope=" + supportsEmployeeScope()
                + ", categoryScope=" + supportsCategoryScope()
                + ", dishScope=" + supportsDishScope()
                + ", setmealScope=" + supportsSetmealScope()
                + ", cartScope=" + supportsShoppingCartScope()
                + ", ordersScope=" + supportsOrdersScope();
    }

    public boolean hasTable(String tableName) {
        return tableCache.computeIfAbsent(tableName.toLowerCase(), this::detectTable);
    }

    public boolean hasColumn(String tableName, String columnName) {
        if (!hasTable(tableName)) {
            return false;
        }
        String cacheKey = tableName.toLowerCase() + "." + columnName.toLowerCase();
        return columnCache.computeIfAbsent(cacheKey, key -> detectColumn(tableName, columnName));
    }

    private boolean detectTable(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return existsTable(metaData, catalog, tableName);
        } catch (SQLException ex) {
            log.warn("Failed to inspect table {} readiness, fallback to false", tableName, ex);
            return false;
        }
    }

    private boolean detectColumn(String tableName, String columnName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return existsColumn(metaData, catalog, tableName, columnName);
        } catch (SQLException ex) {
            log.warn("Failed to inspect column {}.{} readiness, fallback to false", tableName, columnName, ex);
            return false;
        }
    }

    private boolean existsTable(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        return existsTable(metaData, catalog, tableName, "TABLE")
                || existsTable(metaData, catalog, tableName.toUpperCase(), "TABLE")
                || existsTable(metaData, catalog, tableName.toLowerCase(), "TABLE");
    }

    private boolean existsTable(DatabaseMetaData metaData, String catalog, String tableName, String type) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{type})) {
            return resultSet.next();
        }
    }

    private boolean existsColumn(DatabaseMetaData metaData, String catalog, String tableName, String columnName) throws SQLException {
        return existsColumn(metaData, catalog, tableName, columnName, false)
                || existsColumn(metaData, catalog, tableName.toUpperCase(), columnName.toUpperCase(), true)
                || existsColumn(metaData, catalog, tableName.toLowerCase(), columnName.toLowerCase(), true);
    }

    private boolean existsColumn(DatabaseMetaData metaData, String catalog, String tableName, String columnName, boolean exact) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (!resultSet.next()) {
                return false;
            }
            if (exact) {
                return true;
            }
            String actualColumnName = resultSet.getString("COLUMN_NAME");
            return actualColumnName != null && actualColumnName.equalsIgnoreCase(columnName);
        }
    }
}
