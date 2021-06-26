package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
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
class StealActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    GameLogicService gameLogicService;

    Game game;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0)
                , boardCreationService.generate(), cardPileCreationService.generate(),"1");
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();
        currentPlayer.getRemainingActions().add(PlayerAction.STEAL);
        game.setCurrentPlayerId(1);

        game.getBoard().addSettlement(new Coordinate(0,0,0, Direction.TOP), 2);
        game.getBoard().addSettlement(new Coordinate(0,-1,1, Direction.LEFT), 3);

        game.getBoard().getRobberTile().setCoordinate(new Coordinate(0,0,0));
    }

    @Test
    public void stealResource() {
        game.getPlayers().get(1).addResources(1,0,0,0,0);

        Action action = new StealAction(1,2,gameLogicService);
        action.performAction(game);
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();

        assertEquals(1, currentPlayer.getBricks());
    }

}