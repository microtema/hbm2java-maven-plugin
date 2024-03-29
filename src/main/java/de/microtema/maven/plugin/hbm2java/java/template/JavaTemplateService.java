package de.microtema.maven.plugin.hbm2java.java.template;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JavaTemplateService {

    private final JavaTemplate javaTemplate;

    public void writeJavaTemplates(List<TableDescription> tableDescriptions, ProjectData projectData) {

        Set<String> commonColumns = getCommonColumns(tableDescriptions);

        TableDescription baseTableDescription = null;

        TableDescription firstTableDescription = tableDescriptions.get(0);

        List<ColumnDescription> commonsColumns = new ArrayList<>(firstTableDescription.getColumns());

        boolean isIdentityColumn = commonsColumns.stream().anyMatch(ColumnDescription::isIdentityColumn);

        if (!isIdentityColumn) {

            commonsColumns = commonsColumns.stream().filter(ColumnDescription::isPrimaryKey).collect(Collectors.toList());

            TableDescription keyTableDescription = new TableDescription();

            keyTableDescription.setName(projectData.getDomainName() + "Key");
            keyTableDescription.setColumns(commonsColumns);
            keyTableDescription.setExtendsClassName("CompositeKey");

            javaTemplate.writeOutEntity(keyTableDescription, projectData);
        }

        if (!commonColumns.isEmpty()) {

            commonsColumns = firstTableDescription.getColumns().stream().filter(it -> commonColumns.contains(it.getName())).collect(Collectors.toList());

            commonsColumns.removeIf(it -> StringUtils.equals(it.getSqlType(), "timestamp"));

            baseTableDescription = new TableDescription();

            if (isIdentityColumn) {
                baseTableDescription.setExtendsClassName("Entity");
            } else {
                commonsColumns.removeIf(ColumnDescription::isPrimaryKey);
                baseTableDescription.setExtendsClassName("BaseEntity<" + projectData.getDomainName() + "Key>");
            }

            baseTableDescription.setName(projectData.getDomainName() + "Entity");
            baseTableDescription.setColumns(commonsColumns);
            baseTableDescription.setCommonClass(true);

            javaTemplate.writeOutEntity(baseTableDescription, projectData);
        }

        for (TableDescription tableDescription : tableDescriptions) {

            tableDescription.setName(tableDescription.getName() + "Entity");

            tableDescription.getColumns().removeIf(it -> commonColumns.contains(it.getName()));
            tableDescription.getColumns().removeIf(it -> StringUtils.equals(it.getSqlType(), "timestamp"));

            if (Objects.nonNull(baseTableDescription)) {
                tableDescription.setExtendsClassName(baseTableDescription.getName());
            }

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

                List<Boolean> requiredList = getAllRequired(columnName, tableDescriptions);
                columnDescription.setRequiredList(requiredList);

                tables.add(tableName);
            }
        }

        column2Table.entrySet().removeIf(it -> it.getValue().size() != tableDescriptions.size());

        return column2Table.keySet();
    }

    private List<Boolean> getAllRequired(String columnName, List<TableDescription> tableDescriptions) {

        List<Boolean> requiredList = new ArrayList<>();

        for (TableDescription tableDescription : tableDescriptions) {
            tableDescription.getColumns().stream()
                    .filter(it -> StringUtils.equalsIgnoreCase(it.getName(), columnName))
                    .forEach(it -> requiredList.add(it.isRequired()));
        }

        return requiredList;
    }
}
