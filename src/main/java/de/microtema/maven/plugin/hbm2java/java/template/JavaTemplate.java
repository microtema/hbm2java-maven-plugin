package de.microtema.maven.plugin.hbm2java.java.template;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import de.microtema.maven.plugin.hbm2java.util.MojoUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.microtema.maven.plugin.hbm2java.util.MojoUtil.lineSeparator;


public class JavaTemplate {

    @SneakyThrows
    public void writeOutEntity(TableDescription tableDescription, ProjectData projectData) {

        String className = tableDescription.getName();
        String tableName = tableDescription.getTableName();
        String packageName = projectData.getPackageName();

        List<ColumnDescription> listColumnDescriptions = tableDescription.getColumns();
        boolean isIdentityColumn = listColumnDescriptions.stream().anyMatch(ColumnDescription::isIdentityColumn);
        String extendsClassName = tableDescription.getExtendsClassName();

        boolean isCommonClass = tableDescription.isCommonClass();
        boolean isEntityClassType = StringUtils.contains(className, "Entity");
        String tenantCode = tableDescription.getNamePrefix();

        if (StringUtils.isNotEmpty(tenantCode)) {
            packageName += "." + tenantCode.toLowerCase();
        }

        String outputJavaDirectory = projectData.getOutputJavaDirectory();

        Map<String, String> fieldMapping = projectData.getFieldMapping();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("package ").append(packageName).append(";").append(MojoUtil.lineSeparator(2));

        writeOutImports(tableDescription, stringBuilder, projectData.getPackageName(), projectData.getInterfaceNames(), isIdentityColumn);

        stringBuilder.append("@Data\n");
        if (isCommonClass) {
            stringBuilder.append("@MappedSuperclass").append(MojoUtil.lineSeparator(1));
        } else if (StringUtils.isNotEmpty(tenantCode)) {
            stringBuilder.append("@Entity").append(MojoUtil.lineSeparator(1));
            stringBuilder.append("@TenantCode(TenantType.").append(tenantCode.toUpperCase()).append(")").append(MojoUtil.lineSeparator(1));
            stringBuilder.append("@Table(name = \"").append(tableName).append("\")").append(MojoUtil.lineSeparator(1));
        }

        if (isEntityClassType) {

            if (!isIdentityColumn) {
                stringBuilder.append("@EqualsAndHashCode(callSuper = true)").append(MojoUtil.lineSeparator(1));
            }
        } else if (!isIdentityColumn) {
            stringBuilder.append("@Embeddable").append(MojoUtil.lineSeparator(1));
        }

        stringBuilder.append("public class ").append(className);
        if (StringUtils.isNotEmpty(extendsClassName)) {
            stringBuilder.append(isEntityClassType && !isIdentityColumn ? " extends " : " implements ").append(extendsClassName);
        }
        stringBuilder.append(" {").append(MojoUtil.lineSeparator(2));

        for (ColumnDescription columnDescription : listColumnDescriptions) {

            String columnType = resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());
            String fieldAnnotationTemplate = getFieldAnnotation(columnType);

            if (columnDescription.isIdentityColumn()) {
                stringBuilder.append("    ").append("@Id").append(lineSeparator(1));
                stringBuilder.append("    ").append("@GeneratedValue(strategy = GenerationType.IDENTITY)").append(lineSeparator(1));
            }

            if (Objects.nonNull(fieldAnnotationTemplate)) {
                stringBuilder.append("    ").append(fieldAnnotationTemplate).append(lineSeparator(1));
            }

            stringBuilder.append("    ").append(getColumnJsonAnnotationTemplate(columnDescription)).append(lineSeparator(1));
            stringBuilder.append("    ").append(getColumnAnnotationTemplate(columnDescription)).append(lineSeparator(1));
            stringBuilder.append("    ").append(getFieldTemplate(columnDescription, fieldMapping)).append(lineSeparator(2));
        }

        stringBuilder.append("}").append(MojoUtil.lineSeparator(1));

        writeOutFile(tableDescription, projectData, className, outputJavaDirectory, stringBuilder);
    }

    private void writeOutFile(TableDescription tableDescription, ProjectData projectData, String tableName, String outputJavaDirectory, StringBuilder stringBuilder) throws IOException {

        String packageDirectory = MojoUtil.getPackageDirectory(projectData.getPackageName());

        String subFolder = StringUtils.trimToEmpty(tableDescription.getNamePrefix()).toLowerCase();

        if (StringUtils.isNotEmpty(subFolder)) {
            subFolder += File.separator;
        }

        String file = String.format("%s%s%s%s%s.java", outputJavaDirectory, File.separator, packageDirectory, subFolder, tableName);
        System.out.println("Writing Java Entity file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }

    private String getFieldTemplate(ColumnDescription columnDescription, Map<String, String> fieldMapping) {

        String name = resolveFiledName(columnDescription.getName(), fieldMapping);
        String fieldType = resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());

        return String.format("private %s %s;", fieldType, name);
    }

    private String getColumnAnnotationTemplate(ColumnDescription columnDescription) {

        String name = columnDescription.getName();
        boolean required = columnDescription.isRequired();
        int size = getSize(columnDescription);

        if (required) {

            if (size > -1) {
                return String.format("@Column(name = \"[%s]\", nullable = false, length = " + size + ")", name);
            }

            return String.format("@Column(name = \"[%s]\", nullable = false)", name);
        }

        if (size > -1) {
            return String.format("@Column(name = \"[%s]\", length = " + size + ")", name);
        }

        return String.format("@Column(name = \"[%s]\")", name);
    }

    private int getSize(ColumnDescription columnDescription) {

        String columnType = columnDescription.getJavaType();

        switch (columnType) {
            case "java.lang.String":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.math.BigDecimal":
                return columnDescription.getSize();
            default:
                return -1;
        }
    }

    private String getColumnJsonAnnotationTemplate(ColumnDescription columnDescription) {

        String name = columnDescription.getName();

        return String.format("@JsonProperty(\"[%s]\")", name);
    }

    public String resolveFiledName(String snakeWord, Map<String, String> fieldMapping) {

        snakeWord = fieldMapping.getOrDefault(snakeWord, snakeWord);

        snakeWord = snakeWord.replaceAll("\\(", " ");
        snakeWord = snakeWord.replaceAll("\\)", " ");
        snakeWord = snakeWord.replaceAll("ü", "ue");
        snakeWord = snakeWord.replaceAll("Ü", "Ue");
        snakeWord = snakeWord.replaceAll("ö", "oe");

        if (snakeWord.contains("_") || snakeWord.contains("-") || snakeWord.contains(" ")) {


            snakeWord = snakeWord.replaceAll("_", " ");
            snakeWord = snakeWord.replaceAll("-", " ");

            snakeWord = WordUtils.capitalizeFully(snakeWord);

            snakeWord = snakeWord.replaceAll(" ", "");
        }

        return WordUtils.uncapitalize(snakeWord);
    }

    private String resolveFiledType(String javaType, String sqlType) {

        switch (javaType) {
            case "java.sql.Timestamp":
                return LocalDateTime.class.getSimpleName();
            case "java.math.BigDecimal":
                return BigDecimal.class.getSimpleName();
            case "java.lang.Long":
                return Long.class.getSimpleName();
            case "java.lang.Integer":
                return int.class.getSimpleName();
            case "java.lang.String":
                return String.class.getSimpleName();
            case "java.lang.Boolean":
            case "java.lang.Short":
                return boolean.class.getSimpleName();
            default:
                return resolveFiledTypeFromSQlType(sqlType);
        }
    }

    private String resolveFiledTypeFromSQlType(String sqlType) {

        switch (sqlType) {
            case "timestamp":
                return LocalDateTime.class.getSimpleName();
            case "image":
                return byte[].class.getSimpleName();
            case "bigint":
                return Long.class.getSimpleName();
            default:
                return sqlType;
        }
    }

    private String getFieldAnnotation(String sqlType) {

        switch (sqlType) {
            case "byte[]":
                return "@Lob";
            default:
                return null;
        }
    }

    private void writeOutImports(TableDescription tableDescription, StringBuilder stringBuilder, String packageName, List<String> interfaceNames, boolean isIdentityColumn) {

        String className = tableDescription.getName();
        String namePrefix = tableDescription.getNamePrefix();
        String extendsClassName = tableDescription.getExtendsClassName();
        boolean isEntityClassType = StringUtils.contains(className, "Entity");

        boolean containFields = !tableDescription.getColumns().isEmpty();

        boolean isCommonClass = tableDescription.isCommonClass();

        List<String> imports = new ArrayList<>();

        if (containFields) {
            imports.add("com.fasterxml.jackson.annotation.JsonProperty");
        }

        imports.add("lombok.Data");

        if (isEntityClassType && !isIdentityColumn) {
            imports.add("lombok.EqualsAndHashCode");
        }

        if (isCommonClass) {

            if (StringUtils.isNotEmpty(extendsClassName)) {

                String classNameCleanUp = MojoUtil.cleanupClassName(extendsClassName);
                interfaceNames.stream().filter(it -> MojoUtil.getClassName(it).equals(classNameCleanUp)).forEach(imports::add);
                imports.add(null);
            }

        } else {

            if (isEntityClassType) {
                interfaceNames.stream().filter(it -> MojoUtil.getClassName(it).equals("TenantCode")).forEach(imports::add);

                String extendsClassPackageName = "";
                if (StringUtils.isNotEmpty(namePrefix)) {
                    extendsClassPackageName = packageName;
                }
                if (StringUtils.isNotEmpty(extendsClassName)) {
                    extendsClassPackageName += "." + extendsClassName;
                }

                if (StringUtils.isNotEmpty(extendsClassPackageName)) {
                    imports.add(extendsClassPackageName);
                }

                interfaceNames.stream().filter(it -> MojoUtil.getClassName(it).equals("TenantType")).forEach(imports::add);

                imports.add(null);

                if (containFields) {
                    imports.add("javax.persistence.Column");
                }

                imports.add("javax.persistence.Entity");
                imports.add("javax.persistence.Table");

            } else {

                interfaceNames.stream().filter(it -> MojoUtil.getClassName(it).equals("CompositeKey")).forEach(imports::add);

                imports.add(null);

                if (isIdentityColumn) {
                    imports.add("javax.persistence.GeneratedValue");
                    imports.add("javax.persistence.GenerationType");
                    imports.add("javax.persistence.Id");
                    imports.add("javax.persistence.MappedSuperclass");
                } else {
                    imports.add("javax.persistence.Column");
                    imports.add("javax.persistence.Embeddable");
                }
            }

        }

        List<String> importPackages = getImportPackages(tableDescription.getColumns(), isCommonClass, isEntityClassType, isIdentityColumn);

        imports.addAll(importPackages);

        for (String importName : imports) {

            if (StringUtils.isEmpty(importName)) {
                stringBuilder.append(MojoUtil.lineSeparator(1));
            } else {

                importName = StringUtils.trim(importName);

                stringBuilder.append("import ").append(importName).append(";").append(MojoUtil.lineSeparator(1));
            }
        }

        stringBuilder.append(MojoUtil.lineSeparator(1));
    }

    private List<String> getImportPackages(List<ColumnDescription> columns, boolean isSuperClass, boolean isEntityType, boolean isIdentityColumn) {

        List<String> packages = new ArrayList<>();

        if (!columns.isEmpty() && isEntityType) {

            packages.add("javax.persistence.Column");

            if (isIdentityColumn) {

                packages.add("javax.persistence.GeneratedValue");
                packages.add("javax.persistence.GenerationType");
                packages.add("javax.persistence.Id");
            }
        }

        boolean anyMatch = columns.stream()
                .anyMatch(it -> StringUtils.equals(it.getJavaType(), byte[].class.getName()));

        if (anyMatch) {
            packages.add("javax.persistence.Lob");
        }

        if (isSuperClass) {


            packages.add("javax.persistence.MappedSuperclass");
        }

        anyMatch = columns.stream()
                .anyMatch(it -> StringUtils.equals(it.getJavaType(), BigDecimal.class.getName()));

        if (anyMatch) {
            packages.add("java.math.BigDecimal");
        }

        anyMatch = columns.stream()
                .anyMatch(it -> StringUtils.equals(it.getJavaType(), Timestamp.class.getName()));

        if (anyMatch) {
            packages.add("java.time.LocalDateTime");
        }

        return packages;
    }
}
