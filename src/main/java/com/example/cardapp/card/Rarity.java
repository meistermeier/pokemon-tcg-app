package com.example.cardapp.card;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public record Rarity(@Id String rarity) {
}
