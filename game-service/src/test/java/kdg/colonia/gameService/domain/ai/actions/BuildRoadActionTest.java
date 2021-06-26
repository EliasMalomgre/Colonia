package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.services.*;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuildRoadActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    GameInfoService infoService;

    @Autowired
    GameLogicService gameLogicService;

    Game game;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0)
                , boardCreationService.generate(), cardPileCreationService.generate(),"1");
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();
        currentPlayer.getRemainingActions().add(PlayerAction.INITIAL1);
        game.getBoard().addSettlement(new Coordinate(0,0,0,Direction.TOP), 1);
        game.setCurrentPlayerId(1);

    }

    @Test
    public void placeRoad() {

        Action action = new BuildRoadAction(currentPlayer.getPlayerId(), new Coordinate(0,0,0, CardDir.NORTH_EAST), gameLogicService);
        action.performAction(game);

        assertEquals(1, game.getBoard().getRoads().size());
        assertEquals(1, game.getBoard().getRoads().get(0).getPlayerId());
        assertEquals(new Coordinate(0,0,0, CardDir.NORTH_EAST), game.getBoard().getRoads().get(0).getCoordinate());
    }

}