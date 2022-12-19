package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableDescription {

    private String namePrefix;

    private String name;

    private String tableName;

    private String databaseName;

    private String tableSchema;

    private List<ColumnDescription> columns = new ArrayList<>();

    private String extendsClassName;

    private boolean commonClass;
}
