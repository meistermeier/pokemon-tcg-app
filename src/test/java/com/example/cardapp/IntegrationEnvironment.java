package com.example.cardapp;

import org.neo4j.driver.Driver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.MountableFile;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class IntegrationEnvironment {

    final static Neo4jContainer<?> container = new Neo4jContainer<>("neo4j:4.4")
            .withCopyFileToContainer(MountableFile.forHostPath("src/main/resources/import/"), "/import")
            .withEnv("NEO4JLABS_PLUGINS", "[\"apoc\"]")
            .withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.\\*")
            .withEnv("apoc.import.file.enabled", "true")
            .withAdminPassword("secret")
            .withReuse(true);

    @DynamicPropertySource
    public static void setupNeo4j(DynamicPropertyRegistry registry) {
        container.start();
        var boltUrl = container.getBoltUrl();

        System.out.println("http://" + container.getHost() + ":" + container.getMappedPort(7474) + "/browser?dbms=" + boltUrl);

        registry.add("spring.neo4j.uri", () -> boltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", container::getAdminPassword);
    }

    public static void initDb(Driver driver) {
        try (var session = driver.session()) {
            var c = session.run("MATCH (n:Pokémon) return count(n) as c").single().get("c").asLong();
            if (c > 0) {
                return;
            }
        }
        try {
            AsciidoctorCypher.executeDocument(driver, Paths.get(AsciidoctorCypher.class.getResource("/import.adoc").toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
