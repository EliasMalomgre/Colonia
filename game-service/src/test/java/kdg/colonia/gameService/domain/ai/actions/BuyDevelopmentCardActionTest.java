package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.domain.Board;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.services.CardPileCreationService;
import kdg.colonia.gameService.services.GameLogicService;
import kdg.colonia.gameService.services.PlayerService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuyDevelopmentCardActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    GameLogicService gameLogicService;

    @Autowired
    CardConfig cf;

    Game game;
    Board board;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0),
                boardCreationService.generate(), cardPileCreationService.generate(),"1");
        board = game.getBoard();
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();

        currentPlayer.getRemainingActions().add(PlayerAction.BUY);
        currentPlayer.addResources(cf.getBrickCost(), cf.getGrainCost(), cf.getLumberCost(), cf.getOreCost(), cf.getWoolCost());
        game.setCurrentPlayerId(1);
    }

    @Test
    public void performBuyDevelopmentCardAction() {
        Action action = new BuyDevelopmentAction(currentPlayer.getPlayerId(), gameLogicService);

        assertEquals(0, currentPlayer.getCards().size(), "Player should not have bought cards before this point");
        assertNotEquals(0, currentPlayer.getResourcesTotal(), "Player should have enough resources");

        action.performAction(game);

        assertEquals(0, currentPlayer.getCards().size(), "Player should have received 1 card after this point");
        assertEquals(0, currentPlayer.getResourcesTotal(), "Player should have spent cardConfig amount of resources");
    }

}