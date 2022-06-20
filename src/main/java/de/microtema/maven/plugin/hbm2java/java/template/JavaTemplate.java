package de.microtema.maven.plugin.hbm2java.java.template;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.microtema.maven.plugin.hbm2java.java.template.FileUtil.lineSeparator;


public class JavaTemplate {

    @SneakyThrows
    public void writeOutEntity(TableDescription tableDescription, ProjectData projectData) {

        List<ColumnDescription> listColumnDescriptions = tableDescription.getColumns();

        String tableName = tableDescription.getName();
        String outputJavaDirectory = projectData.getOutputJavaDirectory();

        Map<String, String> fieldMapping = projectData.getFieldMapping();

        StringBuilder stringBuilder = new StringBuilder();

        for (ColumnDescription columnDescription : listColumnDescriptions) {

            String columnType = resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());
            String fieldAnnotationTemplate = getFieldAnnotation(columnType);

            if (Objects.nonNull(fieldAnnotationTemplate)) {
                stringBuilder.append("    ").append(fieldAnnotationTemplate).append(lineSeparator(1));
            }

            stringBuilder.append("    ").append(getColumnAnnotationTemplate(columnDescription)).append(lineSeparator(1));
            stringBuilder.append("    ").append(getFieldTemplate(columnDescription, fieldMapping)).append(lineSeparator(2));
        }

        String packageDirectory = FileUtil.getPackageDirectory(projectData.getPackageName());

        String file = String.format("%s%s%s%s.java.template", outputJavaDirectory, File.separator, packageDirectory, tableName);
        System.out.println("Writing Java file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }

    private static String getFieldTemplate(ColumnDescription columnDescription, Map<String, String> fieldMapping) {

        String name = resolveFiledName(columnDescription.getName(), fieldMapping);
        String fieldType = resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());

        return String.format("private %s %s;", fieldType, name);
    }

    private static String getColumnAnnotationTemplate(ColumnDescription columnDescription) {

        String name = columnDescription.getName();
        boolean required = columnDescription.isRequired();

        if (required) {
            return String.format("@Column(name = \"[%s]\")", name);
        }

        return String.format("@Column(name = \"[%s]\", nullable = false)", name);
    }

    public static String resolveFiledName(String snakeWord, Map<String, String> fieldMapping) {

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

    private static String resolveFiledType(String javaType, String sqlType) {

        switch (javaType) {
            case "java.sql.Timestamp":
                return LocalDateTime.class.getSimpleName();
            case "java.math.BigDecimal":
                return BigDecimal.class.getSimpleName();
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

    private static String resolveFiledTypeFromSQlType(String sqlType) {

        switch (sqlType) {
            case "timestamp":
                return LocalDateTime.class.getSimpleName();
            case "image":
                return byte[].class.getSimpleName();
            default:
                return sqlType;
        }
    }

    private static String getFieldAnnotation(String sqlType) {

        switch (sqlType) {
            case "byte[]":
                return "@Lob";
            default:
                return null;
        }
    }
}
