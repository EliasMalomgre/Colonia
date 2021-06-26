package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class UseDCYearOfPlentyActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    GameLogicService gameLogicService;

    Game game;
    Board board;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0),
                boardCreationService.generate(), cardPileCreationService.generate(),"1");
        board = game.getBoard();
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();

        currentPlayer.getRemainingActions().add(PlayerAction.PLAY_CARD);
        currentPlayer.getCards().add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
        game.setCurrentPlayerId(1);
    }

    @Test
    public void performYearOfPlentyAction() {
        Action action = new UseDCYearOfPlentyAction(currentPlayer.getPlayerId(), Resource.BRICK, Resource.GRAIN, gameLogicService);

        assertEquals(0, currentPlayer.getResources().get(Resource.BRICK));
        assertEquals(0, currentPlayer.getResources().get(Resource.GRAIN));
        assertEquals(0, currentPlayer.getResourcesTotal());

        action.performAction(game);

        assertEquals(1, currentPlayer.getResources().get(Resource.BRICK));
        assertEquals(1, currentPlayer.getResources().get(Resource.GRAIN));
        assertEquals(2, currentPlayer.getResourcesTotal());

        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertEquals(0, currentPlayer.getCards().size());
        assertEquals(1, currentPlayer.getPlayedCards().size());
    }

}