package com.example.cardapp.card;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface CardRepository extends Neo4jRepository<Card, String> {

    @Query("MATCH " +
            "(rarity:Rarity)<-[rr:HAS_RARITY]-(c:Card)-[r:IS_PART_OF]->(s:Set)-[rs:IN_SERIES]-(series:Series)" +
            "where c.numberInSet > s.printedTotal " +
            "RETURN c, collect(r), collect(rr), collect(rarity), collect(s), collect(rs), collect(series)")
    List<Card> probablyRareCards();

    @Query("""
            MATCH (rarity:Rarity)<-[:HAS_RARITY]-(c:Card)-[:IS_PART_OF]->(s:Set)
            where rarity.rarity = 'Rare Secret' and c.numberInSet > s.printedTotal
            RETURN c
            """)
    List<Card> getSuperRare();
}
