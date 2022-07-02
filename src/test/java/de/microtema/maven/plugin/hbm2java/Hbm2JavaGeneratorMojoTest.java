package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.util.MojoUtil;
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

        sut.interfaceNames.add("de.microtema.tenant.TenantType");
        sut.interfaceNames.add("de.microtema.tenant.TenantCode");
        sut.interfaceNames.add("de.microtema.repository.BaseEntity");
        sut.domainName = "customer";
        sut.tableNames = Arrays.asList("MT:[MT$Customer]", "DX:[DX$Customer]");
        sut.host = databaseConfig.getHost();
        sut.userName = databaseConfig.getUserName();
        sut.password = databaseConfig.getPassword();

        sut.execute();

        File[] files = outputSpecFile.listFiles();

        assertNotNull(files);
        assertEquals(3, files.length);

        File dir = new File(outputSpecFile, "mt");
        String answer = FileUtils.readFileToString(dir.listFiles()[0], "UTF-8");
        assertEquals("package de.microtema.repository.mt;\n" +
                "\n" +
                "import lombok.Data;\n" +
                "import lombok.EqualsAndHashCode;\n" +
                "import de.microtema.tenant.TenantCode;\n" +
                "import de.microtema.repository.CustomerEntity;\n" +
                "import de.microtema.tenant.TenantType;\n" +
                "\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "\n" +
                "@Data\n" +
                "@Entity\n" +
                "@TenantCode(TenantType.MT)\n" +
                "@Table(name = \"[MT$Customer]\")\n" +
                "@EqualsAndHashCode(callSuper = true)\n" +
                "public class MTCustomerEntity extends CustomerEntity {\n" +
                "\n" +
                "}\n", answer);

        File file = files[1];
        answer = FileUtils.readFileToString(file, "UTF-8");
        assertNotNull(answer);

        dir = new File(outputSpecFile, "dx");
        answer = FileUtils.readFileToString(dir.listFiles()[0], "UTF-8");

        assertEquals("package de.microtema.repository.dx;\n" +
                "\n" +
                "import lombok.Data;\n" +
                "import lombok.EqualsAndHashCode;\n" +
                "import de.microtema.tenant.TenantCode;\n" +
                "import de.microtema.repository.CustomerEntity;\n" +
                "import de.microtema.tenant.TenantType;\n" +
                "\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "\n" +
                "@Data\n" +
                "@Entity\n" +
                "@TenantCode(TenantType.DX)\n" +
                "@Table(name = \"[DX$Customer]\")\n" +
                "@EqualsAndHashCode(callSuper = true)\n" +
                "public class DXCustomerEntity extends CustomerEntity {\n" +
                "\n" +
                "}\n", answer);
    }
}
