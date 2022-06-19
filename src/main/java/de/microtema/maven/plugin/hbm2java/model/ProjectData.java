package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProjectData {

    private String outputJavaDirectory;

    private String packageName;

    private Map<String, String> fieldMapping;
}
