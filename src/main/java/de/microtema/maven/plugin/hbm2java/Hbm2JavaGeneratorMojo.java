package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.java.template.JavaTemplateService;
import de.microtema.maven.plugin.hbm2java.jdbc.JdbcMetadataService;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class Hbm2JavaGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "table-names", required = true)
    List<String> tableNames = new ArrayList<>();

    @Parameter(property = "host", required = true)
    String host;

    @Parameter(property = "user-name", required = true)
    String userName;

    @Parameter(property = "password", required = true)
    String password;

    @Parameter(property = "output-dir")
    String outputDir = "./target/generated/src/main";

    @Parameter(property = "package-name")
    String packageName = "de.microtema.repository";

    @Parameter(property = "field-mapping")
    Properties fieldMapping = new Properties();

    JdbcMetadataService jdbcMetadataService = ClassUtil.createInstance(JdbcMetadataService.class);
    JavaTemplateService javaTemplateService = ClassUtil.createInstance(JavaTemplateService.class);

    @SneakyThrows
    public void execute() {

        String appName = Optional.ofNullable(project.getName()).orElse(project.getArtifactId());

        // Skip maven sub modules
        if (tableNames.isEmpty()) {

            logMessage("Skip maven module: " + appName + " since it does not provide table name!");

            return;
        }

        logMessage("Generate Entities from DDL for " + appName + " -> " + outputDir);

        DatabaseConfig databaseConfig = new DatabaseConfig();

        databaseConfig.setHost(host);
        databaseConfig.setUserName(userName);
        databaseConfig.setPassword(password);

        List<TableDescription> tableDescriptions = new ArrayList<>();

        for (String tableName : tableNames) {

            List<ColumnDescription> columnDescriptions = jdbcMetadataService.getListColumnDescriptions(databaseConfig, tableName);

            TableDescription tableDescription = new TableDescription();
            tableDescription.setName(tableName);
            tableDescription.setColumns(columnDescriptions);

            tableDescriptions.add(tableDescription);
        }

        ProjectData projectData = new ProjectData();

        projectData.setPackageName(this.packageName);
        projectData.setFieldMapping(streamConvert(this.fieldMapping));
        projectData.setOutputJavaDirectory(this.outputDir);

        javaTemplateService.writeJavaTemplates(tableDescriptions, projectData);
    }

    public Map<String, String> streamConvert(Properties prop) {
        return prop.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }

    void logMessage(String message) {

        Log log = getLog();

        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }
}
