package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.GameState;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.services.CardPileCreationService;
import kdg.colonia.gameService.services.GameLogicService;
import kdg.colonia.gameService.services.PlayerService;
import kdg.colonia.gameService.services.TurnTokenService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EndTurnActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    TurnTokenService turnTokenService;

    @Autowired
    GameLogicService gameLogicService;

    @Autowired
    GameConfig gameConfig;

    Game game;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0)
                , boardCreationService.generate(), cardPileCreationService.generate(),"1");
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();
        currentPlayer.getRemainingActions().add(PlayerAction.END_TURN);
        game.setCurrentPlayerId(1);
    }

    @Test
    public void endTurn() {

        Action action = new EndTurnAction(currentPlayer.getPlayerId(), gameLogicService, turnTokenService, gameConfig);
        action.performAction(game);

        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.END_TURN));
        assertTrue(game.getPlayers().get(1).getRemainingActions().contains(PlayerAction.ROLL));
    }

    @Test
    public void gameEndedEndTurn() {
        currentPlayer.increaseVictoryPoints(10);

        Action action = new EndTurnAction(currentPlayer.getPlayerId(), gameLogicService, turnTokenService, gameConfig);
        action.performAction(game);

        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.END_TURN));
        assertFalse(game.getPlayers().get(1).getRemainingActions().contains(PlayerAction.ROLL));
        assertEquals(GameState.FINISHED, game.getGameState());
    }

}