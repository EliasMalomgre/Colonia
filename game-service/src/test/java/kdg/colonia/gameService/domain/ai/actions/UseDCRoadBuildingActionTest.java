package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Board;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UseDCRoadBuildingActionTest {

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
        currentPlayer.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        game.setCurrentPlayerId(1);

        board.addSettlement(new Coordinate(0,0,0, Direction.TOP), 1);
    }

    @Test
    public void performRoadBuildingAction() {
        Action action1 = new UseDCRoadBuildingAction(currentPlayer.getPlayerId(), new Coordinate(0,0,0, CardDir.NORTH_WEST), gameLogicService);
        action1.performAction(game);

        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertTrue(currentPlayer.getRemainingActions().contains(PlayerAction.ROAD_BUILDING));
        assertEquals(1, board.getRoads().size());
        assertEquals(1, currentPlayer.getPlayedCards().size());

        Action action2 = new BuildRoadAction(currentPlayer.getPlayerId(), new Coordinate(0,0,0, CardDir.WEST), gameLogicService);
        action2.performAction(game);

        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.ROAD_BUILDING));
        assertEquals(2, board.getRoads().size());
        assertEquals(0, currentPlayer.getCards().size());
        assertEquals(1, currentPlayer.getPlayedCards().size());
    }

}