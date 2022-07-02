package de.microtema.maven.plugin.hbm2java.util;

import lombok.experimental.UtilityClass;

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

        if (parts.length == 2) {
            return parts[0];
        }

        return null;
    }

    public static String getTableName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 2) {
            tableName = parts[1];
        }

        return tableName;
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
}
