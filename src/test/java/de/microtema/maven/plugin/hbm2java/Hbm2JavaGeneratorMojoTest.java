package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.util.MojoUtil;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class Hbm2JavaGeneratorMojoTest {

    @InjectMocks
    Hbm2JavaGeneratorMojo sut;

    @Mock
    MavenProject project;

    File outputSpecFile;

    @BeforeEach
    void setUp() {

        sut.project = project;
    }

    @Test
    void executeOnNonUpdateFalse() throws Exception {

        String packageDirectory = MojoUtil.getPackageDirectory(sut.packageName);

        outputSpecFile = new File(sut.outputDir, packageDirectory);

        DatabaseConfig databaseConfig = new DatabaseConfig();

        sut.tableNames = Arrays.asList("[MT$Customer]", "[DX$Customer]");
        sut.host = databaseConfig.getHost();
        sut.userName = databaseConfig.getUserName();
        sut.password = databaseConfig.getPassword();

        sut.execute();

        File[] files = outputSpecFile.listFiles();

        assertNotNull(files);
        assertEquals(3, files.length);

        outputSpecFile = files[0];

        String answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertEquals("", answer);

        outputSpecFile = files[1];

        answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertEquals("", answer);

        outputSpecFile = files[2];

        answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertNotNull(answer);
    }
}
