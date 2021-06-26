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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UseDCMonopolyActionTest {

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

        game.getPlayers().get(0).addResources(2, 4, 3, 5, 1);
        game.getPlayers().get(1).addResources(1,1,1,1,1);
        game.getPlayers().get(2).addResources(2,2,2,2,2);
        game.getPlayers().get(3).addResources(3,3,3,3,3);

        currentPlayer.getRemainingActions().add(PlayerAction.PLAY_CARD);
        currentPlayer.getCards().add(new ProgressCard(ProgressCardType.MONOPOLY));
        game.setCurrentPlayerId(1);
    }

    @Test
    public void performMonopolyAction() {
        Action action = new UseDCMonopolyAction(currentPlayer.getPlayerId(), Resource.BRICK, gameLogicService);

        assertEquals(2, currentPlayer.getResources().get(Resource.BRICK));
        assertEquals(4, currentPlayer.getResources().get(Resource.GRAIN));
        assertEquals(3, currentPlayer.getResources().get(Resource.LUMBER));
        assertEquals(5, currentPlayer.getResources().get(Resource.ORE));
        assertEquals(1, currentPlayer.getResources().get(Resource.WOOL));
        assertEquals(15, currentPlayer.getResourcesTotal());

        action.performAction(game);

        //resources for player 1
        assertEquals(8, currentPlayer.getResources().get(Resource.BRICK));
        assertEquals(4, currentPlayer.getResources().get(Resource.GRAIN));
        assertEquals(3, currentPlayer.getResources().get(Resource.LUMBER));
        assertEquals(5, currentPlayer.getResources().get(Resource.ORE));
        assertEquals(1, currentPlayer.getResources().get(Resource.WOOL));
        assertEquals(21, currentPlayer.getResourcesTotal());

        //resources for player 2
        assertEquals(0, game.getPlayers().get(1).getResources().get(Resource.BRICK));
        assertEquals(1, game.getPlayers().get(1).getResources().get(Resource.GRAIN));
        assertEquals(1, game.getPlayers().get(1).getResources().get(Resource.LUMBER));
        assertEquals(1, game.getPlayers().get(1).getResources().get(Resource.ORE));
        assertEquals(1, game.getPlayers().get(1).getResources().get(Resource.WOOL));
        assertEquals(4, game.getPlayers().get(1).getResourcesTotal());

        //resources for player 3
        assertEquals(0, game.getPlayers().get(2).getResources().get(Resource.BRICK));
        assertEquals(2, game.getPlayers().get(2).getResources().get(Resource.GRAIN));
        assertEquals(2, game.getPlayers().get(2).getResources().get(Resource.LUMBER));
        assertEquals(2, game.getPlayers().get(2).getResources().get(Resource.ORE));
        assertEquals(2, game.getPlayers().get(2).getResources().get(Resource.WOOL));
        assertEquals(8, game.getPlayers().get(2).getResourcesTotal());

        //resources for player 4
        assertEquals(0, game.getPlayers().get(3).getResources().get(Resource.BRICK));
        assertEquals(3, game.getPlayers().get(3).getResources().get(Resource.GRAIN));
        assertEquals(3, game.getPlayers().get(3).getResources().get(Resource.LUMBER));
        assertEquals(3, game.getPlayers().get(3).getResources().get(Resource.ORE));
        assertEquals(3, game.getPlayers().get(3).getResources().get(Resource.WOOL));
        assertEquals(12, game.getPlayers().get(3).getResourcesTotal());


        assertFalse(currentPlayer.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertEquals(0, currentPlayer.getCards().size());
        assertEquals(1, currentPlayer.getPlayedCards().size());
    }

}