package de.microtema.maven.plugin.hbm2java.jdbc;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMetadataService {

    @SneakyThrows
    public  List<ColumnDescription> getListColumnDescriptions(DatabaseConfig databaseConfig, String tableName) {

        String jdbcDriver = databaseConfig.getJdbcDriver();
        String host = databaseConfig.getHost();

        // Register JDBC driver
        Class.forName(jdbcDriver);

        String userName = databaseConfig.getUserName();
        String password = databaseConfig.getPassword();

        List<ColumnDescription> columnNames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(host, userName, password)) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from " + tableName);

            ResultSetMetaData columns = rs.getMetaData();

            int index = 0;
            while (index++ < columns.getColumnCount()) {

                String columnName = columns.getColumnName(index);
                String columnSqlTypeName = columns.getColumnTypeName(index);
                String columnTypeName = columns.getColumnClassName(index);
                int isNullable = columns.isNullable(index);
                int columnDisplaySize = columns.getColumnDisplaySize(index);

                ColumnDescription columnDescription = new ColumnDescription();

                columnDescription.setName(columnName);
                columnDescription.setJavaType(columnTypeName);
                columnDescription.setSqlType(columnSqlTypeName);
                columnDescription.setRequired(isNullable == 1);
                columnDescription.setSize(columnDisplaySize);

                columnNames.add(columnDescription);
            }
        }

        return columnNames;
    }
}
