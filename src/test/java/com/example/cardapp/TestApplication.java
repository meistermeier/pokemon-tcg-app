package com.example.cardapp;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

public class TestApplication extends IntegrationEnvironment {

    public static void main(String[] args) {
        container.start();
        var boltUrl = container.getBoltUrl();
        System.out.println("http://" + container.getHost() + ":" + container.getMappedPort(7474) + "/browser?dbms=" + boltUrl);
        System.setProperty("spring.neo4j.uri", boltUrl);
        System.setProperty("spring.neo4j.authentication.username", "neo4j");
        System.setProperty("spring.neo4j.authentication.password", container.getAdminPassword());

        var driver = GraphDatabase.driver(boltUrl, AuthTokens.basic("neo4j", container.getAdminPassword()));
        initDb(driver);

        CardAppApplication.main(args);
    }

}
