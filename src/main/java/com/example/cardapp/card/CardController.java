package com.example.cardapp.card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CardController {

    private final CardRepository cardRepository;

    @Autowired
    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }


    @GetMapping("cards/{id}")
    public String cardById(@PathVariable("id") String id, Model model) {

        var card = cardRepository.findById(id).get();
        model.addAttribute("card", card);
        return "cardDetails";
    }

    @GetMapping("cards/rare")
    public String rareCards(Model model) {
        var cards = cardRepository.probablyRareCards();
        model.addAttribute("cards", cards);
        return "rareCards";
    }



}
