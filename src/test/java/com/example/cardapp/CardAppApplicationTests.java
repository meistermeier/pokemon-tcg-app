package com.example.cardapp;

import com.example.cardapp.card.Card;
import com.example.cardapp.card.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CardAppApplicationTests extends IntegrationEnvironment {

    @Autowired
    private Driver driver;

    @BeforeEach
    void setupData() {
        initDb(driver);
    }

    @Test
    void superRareCardsAsExpected(@Autowired CardRepository repository) {
        List<Card> superRareCards = repository.getSuperRare();
        assertThat(superRareCards).extracting("name").contains("Dark Raichu");
        assertThat(superRareCards).extracting("name").doesNotContain("Eevee");
    }

}
