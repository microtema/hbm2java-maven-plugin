package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.List;

@Data
public class TableDescription {

    private String name;

    private List<ColumnDescription> columns;
}
