package com.example.cardapp.card;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Set")
public record CardSet(@Id String id, String name, Integer printedTotal,
                      @Relationship("IN_SERIES") Series series) {
}
