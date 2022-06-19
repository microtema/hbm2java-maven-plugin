package de.microtema.maven.plugin.hbm2java.java.template;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JavaTemplateService {

    public static boolean skipField(boolean isCommonClass, Set<String> commonFields, String name) {

        if (isCommonClass) {
            return !commonFields.contains(name);
        } else return commonFields.contains(name);
    }

    private final JavaTemplate javaTemplate;

    public void writeJavaTemplates(List<TableDescription> tableDescriptions, ProjectData projectData) {

        Set<String> commonColumns = getCommonColumns(tableDescriptions);

        if (!commonColumns.isEmpty()) {

            TableDescription tableDescription = tableDescriptions.get(0);
            List<ColumnDescription> commonsColumns = tableDescription.getColumns().stream().filter(it -> commonColumns.contains(it.getName())).collect(Collectors.toList());

            tableDescription = new TableDescription();
            tableDescription.setName("CommonEntity");
            tableDescription.setColumns(commonsColumns);
            javaTemplate.writeOutEntity(tableDescription, projectData);
        }

        for (TableDescription tableDescription : tableDescriptions) {

            tableDescription.getColumns().removeIf(it -> commonColumns.contains(it.getName()));

            javaTemplate.writeOutEntity(tableDescription, projectData);
        }
    }

    private Set<String> getCommonColumns(List<TableDescription> tableDescriptions) {

        if (tableDescriptions.size() == 0) {
            return Collections.emptySet();
        }

        Map<String, Set<String>> column2Table = new HashMap<>();

        for (TableDescription tableDescription : tableDescriptions) {

            String tableName = tableDescription.getName();
            List<ColumnDescription> columns = tableDescription.getColumns();

            for (ColumnDescription columnDescription : columns) {

                String columnName = columnDescription.getName();

                Set<String> tables = column2Table.get(columnName);

                if (Objects.isNull(tables)) {
                    tables = new HashSet<>();
                    column2Table.put(columnName, tables);
                }

                tables.add(tableName);
            }
        }

        column2Table.entrySet().removeIf(it -> it.getValue().size() != tableDescriptions.size());

        return column2Table.keySet();
    }
}
