package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Board;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
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
class UseDCKnightActionTest {

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
        currentPlayer.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        game.setCurrentPlayerId(1);

        board.setRobberTile(board.getTileForCoordinate(new Coordinate(0,0,0)));

    }

    @Test
    public void performKnightAction() {
        Action action = new UseDCKnightAction(currentPlayer.getPlayerId(), new Coordinate(1,-1,0), gameLogicService);
        action.performAction(game);

        assertEquals(new Coordinate(1,-1,0), board.getRobberTile().getCoordinate());
        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertEquals(0, currentPlayer.getCards().size());
        assertEquals(1, currentPlayer.getPlayedCards().size());
    }

}