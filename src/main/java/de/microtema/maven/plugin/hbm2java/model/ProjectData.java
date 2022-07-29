package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProjectData {

    private String outputJavaDirectory;

    private String domainName;

    private String packageName;

    private Map<String, String> fieldMapping;

    private List<String> interfaceNames = new ArrayList<>();

    private List<String> excludes = new ArrayList<>();
}
