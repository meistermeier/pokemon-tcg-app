package com.example.cardapp;

import org.neo4j.driver.Driver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.MountableFile;

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

        registry.add("spring.neo4j.uri", container::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", container::getAdminPassword);
    }

    public static void initDb(Driver driver) {
        try (var session = driver.session()) {
            var c = session.run("MATCH (n:Pokémon) return count(n) as c").single().get("c").asLong();
            if (c > 0) {
                return;
            }
            session.run("""
                            CREATE CONSTRAINT cardId FOR (card:Card) REQUIRE card.id IS UNIQUE
                    """);
            session.run("""
                            CREATE INDEX cardName IF NOT EXISTS
                            FOR (n:Card)
                            ON (n.name)
                    """);
            session.run("""
                            CREATE INDEX creatureName IF NOT EXISTS
                            FOR (n:Creature)
                            ON (n.name)
                    """);
            session.run("""
                    CALL apoc.load.json('sets.json', '$.data') yield value
                    CREATE (set:Set {
                        name: value.name,
                        id: value.id,
                        symbolUrl: value.symbol,
                        logoUrl: value.logo,
                        printedTotal: value.printedTotal
                    })
                    MERGE (series:Series {name: value.series})
                    MERGE (set)-[:IN_SERIES]->(series)
                    """).consume();
            session.run("""
                    CALL apoc.load.directory('*.json','cards') yield value as file
                    CALL apoc.load.json(file) yield value as value
                    CREATE (card:Card {
                        id: value.id,
                        name: value.name,
                        hp: toInteger(value.hp),
                        imgUrl: value.images.large,
                        numberInSet: toInteger(value.number),
                        nationalPokedexNumbers: value.nationalPokedexNumbers
                    })
                    WITH card, value, replace(replace(file, ".json", ""), "cards/", "") as setName
                    CALL apoc.create.addLabels(card, [value.supertype]+value.subtypes) yield node as node
                    WITH node, value, setName
                    MATCH (set:Set {id: setName})
                    MERGE (node)-[:IS_PART_OF]->(set)
                    MERGE (r:Rarity {rarity: coalesce(value.rarity, "unknown")})
                    MERGE (r)<-[:HAS_RARITY]-(node)
                    FOREACH (type in value.types |
                    MERGE (t:Type {type: type})
                    MERGE (t)<-[:HAS_TYPE]-(node))
                    FOREACH (weakness in value.weaknesses |
                    MERGE (wt:Type {type: weakness.type})
                    MERGE (wt)<-[:HAS_WEAKNESS {value: weakness.value}]-(node))
                    FOREACH (resistance in value.resistances |
                    MERGE (rt:Type {type: resistance.type})
                    MERGE (rt)<-[:HAS_RESISTANCE {value: resistance.value}]-(node))
                    """).consume();
            session.run("""
                             MATCH (p:Pokémon:Card) where (p.nationalPokedexNumbers is not null and size(p.nationalPokedexNumbers) = 1)
                             WITH distinct(p.nationalPokedexNumbers[0]) as number
                             CREATE (:Pokémon:Creature{nationalPokedexNumber: number})
                    """);
            session.run("""
                             MATCH (p:Pokémon:Creature)
                             WITH p.nationalPokedexNumber as pokeIndexNumber, p
                             MATCH (pc:Pokémon:Card) where any(pokidx in pc.nationalPokedexNumbers where pokidx = pokeIndexNumber)
                             WITH min(size(pc.name)) as minSize, pokeIndexNumber, p
                             MATCH (pc:Pokémon:Card) where any(pokidx in pc.nationalPokedexNumbers where pokidx = pokeIndexNumber) and size(pc.name) = minSize
                             WITH distinct(pc.name) as pokemonName, p
                             SET p.name = pokemonName
                    """);
            session.run("""
                    MATCH (p:Pokémon:Creature)
                    MATCH (pc:Pokémon:Card) WHERE pc.name =~ ".*"+p.name+".*"
                    MERGE (pc)-[:IS_POKEMON]->(p)
                    """);
        }
    }



}
