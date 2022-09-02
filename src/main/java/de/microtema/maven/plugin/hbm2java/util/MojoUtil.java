package de.microtema.maven.plugin.hbm2java.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

@UtilityClass
public class MojoUtil {

    public static String getPackageDirectory(String packageName) {

        String packageDirectory = packageName.replaceAll("\\.", File.separator);

        if (!packageDirectory.endsWith(File.separator)) {
            packageDirectory = packageDirectory + File.separator;
        }

        return packageDirectory;
    }

    public static String lineSeparator(int lines) {

        int index = 0;

        StringBuilder str = new StringBuilder();

        while (index++ < lines) {
            str.append(System.lineSeparator());
        }

        return str.toString();
    }

    public static String getNamePrefix(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length > 1) {
            return parts[0];
        }

        return null;
    }

    public static String getTableName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 1) {
            return parts[0];
        }
        if (parts.length == 2) {
            return parts[1];
        }
        if (parts.length == 3) {
            return parts[2];
        }

        return null;
    }

    public static String getDatabaseName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 3) {
            return parts[1];
        }

        return null;
    }

    public static String cleanupTableName(String tableName) {

        return tableName.replace("[", "").replace("]", "").replaceAll(File.separator, "");
    }

    public static String getClassName(String packageName) {

        String[] parts = packageName.split("\\.");

        return parts[parts.length - 1];
    }

    public static String cleanupClassName(String className) {

        String[] parts = className.split("<");

        return parts[0];
    }

    public static String getHostName(String host, String tableNameRaw) {

        String databaseName = getDatabaseName(tableNameRaw);

        if (StringUtils.isEmpty(databaseName)) {
            return host;
        }

        if (StringUtils.contains(host, "sqlserver")) {
            return host + ";databaseName=" + databaseName;
        }

        return host;
    }

    public static String getUserName(String host, String tableNameRaw, String userName) {

        String databaseName = getDatabaseName(tableNameRaw);

        if (StringUtils.isEmpty(databaseName)) {
            return userName;
        }

        if (StringUtils.contains(host, "oracle")) {
            return databaseName;
        }

        return userName;
    }

    public static String getJdbcDriver(String host) {

        if (StringUtils.contains(host, "sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (StringUtils.contains(host, "oracle")) {
            return "oracle.jdbc.OracleDriver";
        }

        throw new IllegalStateException("Unable to identify the JdbcDriver for host: " + host);
    }
}
