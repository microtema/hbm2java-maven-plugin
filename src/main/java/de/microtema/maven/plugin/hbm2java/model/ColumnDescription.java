package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

@Data
public class ColumnDescription {

    /**
     * (Optional) The name of the column. Defaults to the property or field name.
     */
    private String name;

    private String javaType;

    private String sqlType;

    /**
     * (Optional) The column length. (Applies only if a string-valued column is used.)
     */
    private int size;

    private boolean required;
}
