package com.teenthofabud.videoconference.mdr.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Base for repository tests that need a real PostgreSQL engine rather than a mock or an
 * in-memory stand-in: Hikari pooling, the actual JDBC driver, and the schema-level
 * UNIQUE/CHECK constraints defined in {@code sql/ddl/V2__main.sql} that Hibernate's own
 * ddl-auto cannot be trusted to reproduce faithfully.
 * <p>
 * {@code @ServiceConnection} points the datasource at the container automatically, so
 * subclasses need only extend this class and use their repository as normal.
 * {@code @DataJpaTest} wraps every test method in a transaction that is rolled back
 * afterwards, so tests stay isolated from one another without recreating the schema.
 * <p>
 * The container is started once in a static initializer rather than via {@code @Testcontainers}
 * / {@code @Container} — those tie start/stop to a single JUnit test class's lifecycle callbacks,
 * which stops the container as soon as the first subclass finishes, breaking every subclass that
 * runs after it in the same JVM. Started this way, it is a true singleton for the whole test run;
 * the Ryuk reaper that Testcontainers starts alongside it removes it when the JVM exits.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractPostgresRepositoryTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.4-alpine3.23");

    static {
        try {
            // 2. Read the directory programmatically
            Files.list(Paths.get("sql/scripts"))
                    .filter(path -> path.toString().endsWith(".sql")) // Ensure it's an SQL file
                    .filter(path -> !path.getFileName().toString().startsWith("V1__user")) // Exclude V1
                    .forEach(path ->
                            // 3. Mount each passing file dynamically
                            POSTGRES.withCopyFileToContainer(
                                    MountableFile.forHostPath(path),
                                    "/docker-entrypoint-initdb.d/" + path.getFileName().toString()
                            )
                    );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map SQL scripts to Testcontainer", e);
        }

        // 4. Start the container after all files are mounted
        POSTGRES.start();
    }
}