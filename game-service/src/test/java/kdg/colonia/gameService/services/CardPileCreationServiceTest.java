package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.DeckConfig;
import kdg.colonia.gameService.domain.devCard.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CardPileCreationServiceTest {
    @Autowired
    CardPileCreationService cardPileCreationService;
    @Autowired
    DeckConfig deckConfig;

    @Test
    void generateTest() {
        List<ProgressCard> developmentCards = cardPileCreationService.generate();

        assertEquals(deckConfig.getKnightAmount(), developmentCards.stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).count());
        assertEquals(deckConfig.getVictoryAmount(), developmentCards.stream().filter(c -> c.getCardType() == ProgressCardType.VICTORY_POINT).count());
        assertEquals(deckConfig.getMonopolyAmount(), developmentCards.stream().filter(c -> c.getClass() == ProgressCard.class && (c).getCardType() == ProgressCardType.MONOPOLY).count());
        assertEquals(deckConfig.getRoadBuildingAmount(), developmentCards.stream().filter(c -> c.getClass() == ProgressCard.class && (c).getCardType() == ProgressCardType.ROAD_BUILDING).count());
        assertEquals(deckConfig.getYopAmount(), developmentCards.stream().filter(c -> c.getClass() == ProgressCard.class && (c).getCardType() == ProgressCardType.YEAR_OF_PLENTY).count());
    }
}