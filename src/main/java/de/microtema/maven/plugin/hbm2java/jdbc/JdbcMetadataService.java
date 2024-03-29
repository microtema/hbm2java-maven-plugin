package de.microtema.maven.plugin.hbm2java.jdbc;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.util.MojoUtil;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcMetadataService {

    @SneakyThrows
    public List<ColumnDescription> getListColumnDescriptions(DatabaseConfig databaseConfig, String tableNameRaw) {

        String jdbcDriver = databaseConfig.getJdbcDriver();
        String host = MojoUtil.getHostName(databaseConfig.getHost(), tableNameRaw);
        String userName = MojoUtil.getUserName(databaseConfig.getHost(), tableNameRaw, databaseConfig.getUserName());
        String password = databaseConfig.getPassword();

        // Register JDBC driver
        Class.forName(jdbcDriver);

        List<ColumnDescription> columnNames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(host, userName, password)) {

            String tableName = MojoUtil.getTableName(tableNameRaw);

            Set<String> primaryKeys = getPrimaryKeys(tableName, conn);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from " + tableName);

            ResultSetMetaData columns = rs.getMetaData();

            int index = 0;
            while (index++ < columns.getColumnCount()) {

                String columnName = columns.getColumnName(index);
                String columnSqlTypeName = columns.getColumnTypeName(index);
                String columnTypeName = columns.getColumnClassName(index);
                int isNullable = columns.isNullable(index);
               // int columnDisplaySize = columns.getColumnDisplaySize(index);
                int columnDisplaySize = columns.getPrecision(index);

                boolean identityColumn = columns.isAutoIncrement(index);

                ColumnDescription columnDescription = new ColumnDescription();

                columnDescription.setName(columnName);
                columnDescription.setJavaType(columnTypeName);
                columnDescription.setSqlType(columnSqlTypeName);
                columnDescription.setRequired(isNullable == 0);
                columnDescription.setSize(columnDisplaySize);
                columnDescription.setPrimaryKey(primaryKeys.contains(columnName));
                columnDescription.setIdentityColumn(identityColumn);

                columnNames.add(columnDescription);
            }
        }

        return columnNames;
    }

    private Set<String> getPrimaryKeys(String tableName, Connection connection) throws SQLException {

        tableName = MojoUtil.cleanupTableName(tableName);

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);

        Set<String> pkColumnSet = new HashSet<>();

        while (primaryKeys.next()) {

            String pkColumnName = primaryKeys.getString("COLUMN_NAME");
            pkColumnSet.add(pkColumnName);
        }

        return pkColumnSet;
    }
}
