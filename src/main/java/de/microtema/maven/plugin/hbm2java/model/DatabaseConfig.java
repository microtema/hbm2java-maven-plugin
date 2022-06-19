package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

@Data
public class DatabaseConfig {

    // JDBC driver name and database URL
    private String jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String host = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true";

    // Database credentials
    private String userName = "SA";
    private String password = "mssql1Ipw";
}
