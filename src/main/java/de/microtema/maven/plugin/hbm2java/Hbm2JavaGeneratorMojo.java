package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.java.template.JavaTemplateService;
import de.microtema.maven.plugin.hbm2java.jdbc.JdbcMetadataService;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import de.microtema.maven.plugin.hbm2java.util.MojoUtil;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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

    @Parameter(property = "package-name")
    List<String> interfaceNames = new ArrayList<>();

    @Parameter(property = "excludes")
    List<String> excludes = new ArrayList<>();

    @Parameter(property = "includes")
    List<String> includes = new ArrayList<>();

    @Parameter(property = "field-mapping")
    List<String> fieldMapping = new ArrayList<>();

    @Parameter(property = "domain-name")
    String domainName;

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

        domainName = Optional.ofNullable(domainName).orElse("Common");
        domainName = WordUtils.capitalize(domainName);

        logMessage("Generate Entities from DDL for " + appName + " -> " + outputDir);

        DatabaseConfig databaseConfig = new DatabaseConfig();

        databaseConfig.setHost(host);
        databaseConfig.setUserName(userName);
        databaseConfig.setPassword(password);
        databaseConfig.setJdbcDriver(MojoUtil.getJdbcDriver(host));

        List<TableDescription> tableDescriptions = new ArrayList<>();

        for (String tableNameRaw : tableNames) {

            tableNameRaw = StringUtils.trim(tableNameRaw);

            String prefix = MojoUtil.getNamePrefix(tableNameRaw);
            String tableName = MojoUtil.getTableName(tableNameRaw);
            String databaseName = MojoUtil.getDatabaseName(tableNameRaw);
            String tableSchema = MojoUtil.getTableSchema(tableNameRaw);

            List<ColumnDescription> columnDescriptions = jdbcMetadataService.getListColumnDescriptions(databaseConfig, tableNameRaw);

            TableDescription tableDescription = new TableDescription();
            tableDescription.setNamePrefix(prefix);
            tableDescription.setName(StringUtils.trimToEmpty(prefix).toUpperCase() + domainName);
            tableDescription.setTableName(tableName);
            tableDescription.setDatabaseName(databaseName);
            tableDescription.setTableSchema(tableSchema);
            tableDescription.setColumns(columnDescriptions);

            tableDescriptions.add(tableDescription);
        }

        ProjectData projectData = new ProjectData();

        projectData.setOraclePlatForm(MojoUtil.isOraclePlatform(host));
        projectData.setDomainName(domainName);
        projectData.setPackageName(packageName);
        projectData.setFieldMapping(streamConvert(fieldMapping));
        projectData.setOutputJavaDirectory(outputDir);
        projectData.setInterfaceNames(interfaceNames.stream().map(String::trim).collect(Collectors.toList()));
        projectData.setExcludes(excludes.stream().map(String::trim).collect(Collectors.toList()));
        projectData.setIncludes(includes.stream().map(String::trim).collect(Collectors.toList()));

        javaTemplateService.writeJavaTemplates(tableDescriptions, projectData);
    }

    public Map<String, String> streamConvert(List<String> properties) {
        return properties.stream()
                .filter(StringUtils::isNotEmpty)
                .map(it -> it.split("="))
                .collect(
                        Collectors.toMap(
                                it -> it[0].trim(),
                                it -> it[1].trim(),
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
