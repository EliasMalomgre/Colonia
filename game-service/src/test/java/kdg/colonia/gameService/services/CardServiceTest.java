package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.controllers.RESTToSocketsController;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class CardServiceTest {
    CardService cardService;
    @Autowired
    BoardCreationService boardCreationService;
    @Autowired
    CardPileCreationService cardPileCreationService;
    @Autowired
    CardConfig cardConfig;
    @MockBean
    RESTToSocketsController socketsController;

    Game game;
    Player p1;
    Player p2;

    @BeforeEach
    void setUp() {
        this.cardService = new CardService(cardConfig, socketsController);
        p1 = new Player();
        p1.setPlayerId(1);
        p1.getRemainingActions().add(PlayerAction.BUY);
        p1.getResources().replace(Resource.ORE, 3);
        p1.getResources().replace(Resource.WOOL, 1);
        p1.getResources().replace(Resource.GRAIN, 2);
        p2 = new Player();
        p2.setPlayerId(2);
        ArrayList<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        this.game = new Game(players, boardCreationService.generate(), cardPileCreationService.generate(),"1");
    }

    //PLAYER BUYS CARD TESTS

    /**
     * it's player 1's turn, they have enough resources and buy a card
     */
    @Test
    void playerBuysCardHappy() {
        assertTrue(cardService.buyCard(game, p1));
        assertEquals(1, p1.getNewCards().size());
    }

    /**
     * it's player 1's turn, they do not have enough resources to buy a card
     */
    @Test
    void playerDoesNotHaveEnoughResources() {
        p1.getResources().replace(Resource.GRAIN, 0);
        assertFalse(cardService.buyCard(game, p1));
        assertEquals(0, p1.getNewCards().size());
    }

    //KNIGHT CARD TESTS

    /**
     * p1 has a knight card and plays it
     * p1 only has 1 knight, so they don't get the largest army title
     */
    @Test
    void playerPlaysKnightHappy() throws Exception {
        //set-up
        p1.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));

        assertTrue(cardService.playKnightCard(game, p1));
        assertEquals(0, game.getPlayerWithLargestArmy());
    }

    /**
     * p1 tries to play a knight card, but doesn't have any cards to play
     */
    @Test
    void playerDoesntHaveKnightToPlay() throws Exception {
        assertFalse(cardService.playKnightCard(game, p1));
    }

    /**
     * p1 has already played 2 knight cards and plays a third
     * p1 should get the title of largest army
     */
    @Test
    void playerGetsLargestArmyHappy() throws Exception {
        //set-up
        p1.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p1.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p1.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        doNothing().when(socketsController).sendNewAchievementNotice(anyString(),anyInt(),anyString());

        assertTrue(cardService.playKnightCard(game, p1));
        assertEquals(1, game.getPlayerWithLargestArmy());
    }

    /**
     * p2 currently holds the title of largest army with 3 knights
     * p1 plays a 4th knight card and overtakes p2
     */
    @Test
    void playerTakesLargestArmyFromOtherPlayer() throws Exception {
        //set-up
        p1.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p1.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p1.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p2.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p2.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p2.getPlayedCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        p1.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        doNothing().when(socketsController).sendNewAchievementNotice(anyString(),anyInt(),anyString());

        game.setPlayerWithLargestArmy(2);
        assertTrue(cardService.playKnightCard(game, p1));
        assertEquals(1, game.getPlayerWithLargestArmy());
    }

    //VICTORY POINT CARD TESTS

    /**
     * p1 has a victoryPointCard and plays it
     */
    @Test
    void playerPlaysVictoryPointCardHappy() {
        p1.getCards().add(new ProgressCard(ProgressCardType.VICTORY_POINT));
        assertTrue(cardService.playVictoryPointCard(game, p1));
        assertEquals(1, p1.getVictoryPointsAmount());
    }

    /**
     * p1 plays victoryPointCard but has none
     */
    @Test
    void playerDoesntHaveVictoryPointCard() {
        assertFalse(cardService.playVictoryPointCard(game, p1));
        assertEquals(0, p1.getVictoryPointsAmount());
    }

    //YEAR OF PLENTY TESTS

    /**
     * p1 has a yop card and plays it
     */
    @Test
    void playerPlaysYOPHappy() {
        p1.getCards().add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
        assertTrue(cardService.playYearOfPlentyCard(game, p1));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.YOP));
    }

    /**
     * p1 plays a yop card but doesn't have any
     */
    @Test
    void playerPlaysYOPButHasNone() {
        assertFalse(cardService.playYearOfPlentyCard(game, p1));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.YOP));
    }

    //MONOPOLY TESTS

    /**
     * p1 has a monopoly card and plays it
     */
    @Test
    void playerPlaysMonopolyHappy() {
        p1.getCards().add(new ProgressCard(ProgressCardType.MONOPOLY));
        assertTrue(cardService.playMonopoly(game, p1));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.MONOPOLY));
    }

    /**
     * p1 plays a monopoly card but has none
     */
    @Test
    void playerPlaysMonopolyButHasNone() {
        assertFalse(cardService.playMonopoly(game, p1));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.MONOPOLY));
    }

    //ROAD BUILDING TESTS

    /**
     * p1 has a road bulding card and plays it
     */
    @Test
    void playerPlaysRoadBuildingHappy(){
        p1.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        assertTrue(cardService.playRoadBuildingCard(game,p1));
        assertEquals(2, p1.getRemainingActions().stream().filter(ra->ra.equals(PlayerAction.ROAD_BUILDING)).count());
    }

    /**
     * p1 plays a road building card but has none
     */
    @Test
    void playerPlaysRoadBuildingButHasNone(){
        assertFalse(cardService.playRoadBuildingCard(game, p1));
        assertEquals(0, p1.getRemainingActions().stream().filter(ra->ra.equals(PlayerAction.ROAD_BUILDING)).count());
    }
}