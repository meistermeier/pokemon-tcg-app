package com.example.cardapp.card;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
public record Card(@Id String id, Integer kp, String name, String imgUrl, Integer numberInSet,
                   @Relationship("IS_PART_OF") CardSet cardSet,
                   @Relationship("HAS_RARITY") Rarity rarity) {}
