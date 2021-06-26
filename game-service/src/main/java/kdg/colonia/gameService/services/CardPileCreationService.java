package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.DeckConfig;
import kdg.colonia.gameService.domain.devCard.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class creates a pile of cards from which the development cards will be drawn this game.
 * The amount of each card is configured in the application properties.
 * in a normal game, there's a total of 25 cards.
 */
@Service
@Getter
@RequiredArgsConstructor
public class CardPileCreationService {
    private final DeckConfig deckConfig;

    public List<ProgressCard> generate(){
        List<ProgressCard> cards = new ArrayList<>();
        for (int i = 0; i< deckConfig.getKnightAmount(); i++){
            cards.add(new ProgressCard(ProgressCardType.KNIGHT));
        }
        for (int i = 0; i< deckConfig.getYopAmount(); i++){
            cards.add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
        }
        for (int i = 0; i < deckConfig.getMonopolyAmount(); i++) {
            cards.add(new ProgressCard(ProgressCardType.MONOPOLY));
        }
        for (int i = 0; i < deckConfig.getRoadBuildingAmount(); i++) {
            cards.add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        }
        for (int i = 0; i < deckConfig.getVictoryAmount(); i++) {
            cards.add(new ProgressCard(ProgressCardType.VICTORY_POINT));
        }

        Collections.shuffle(cards);
        return cards;
    }
}
