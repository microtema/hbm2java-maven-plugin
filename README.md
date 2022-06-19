# DDL to Java Beans Generator
Reducing Boilerplate Code with il-contract-api maven plugin
> More Time for Feature and functionality
  Through a simple set of hbm2java-maven-plugin templates and saving 60% of development time 

## Key Features
* Auto generation by maven compile phase
* Auto detection commons classes
* Generate lombok based java beans templates
* Custom Field mapping

## How to use

### Maven configuration
```
<plugin>
    <groupId>de.microtema</groupId>
    <artifactId>hbm2java-maven-plugin</artifactId>
    <version>2.0.1</version>
    <configuration>
        <packageName>${project.groupId}.repository</packageName>
        <outputDir>./target/generated/src/main</outputDir>
        <host></host>
        <userName></userName>
        <password></password>
        <tableNames>CSV</tableNames>
        <fieldMapping>
            <LASTNAME>LAST_NAME</LASTNAME>
            <FIRSTNAME>FIRST_NAME</FIRSTNAME>
            <MODIFIED_AT>MODIFIED_DATE</MODIFIED_AT>
            <CREATED_AT>CREATED_DATE</CREATED_AT>
        </fieldMapping>
    </configuration>
    <executions>
        <execution>
            <id>hbm2java</id>
            <phase>validate</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Output 
* ./target/generated/src/main/de/microtema/repository/PrivateCustomerEntity.java.template
* ./target/generated/src/main/de/microtema/repository/BusinessCustomerEntity.java.template
* ./target/generated/src/main/de/microtema/repository/CustomerEntity.java.template

> NOTE: This is an example file.

```
    @Column(name = "[vts]", nullable = false)
    private String vts;
```

```
    @Column(name = "[vtg]", nullable = false)
    private String vtg;
```

```
    @Column(name = "[id_]", nullable = false)
    private String id;

    @Column(name = "[address]", nullable = false)
    private String address;

    ...
```
    
## Technology Stack

* Java 1.8
    * Streams 
    * Lambdas
* Third Party Libraries
    * Commons-BeanUtils (Apache License)
    * Commons-IO (Apache License)
    * Commons-Lang3 (Apache License)
    * Junit (EPL 1.0 License)
* Code-Analyses
    * Sonar
    * Jacoco
    
## Test Coverage threshold
> 95%
    
## License

MIT (unless noted otherwise)

## Quality Gate Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)

[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)
