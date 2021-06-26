package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.services.CardPileCreationService;
import kdg.colonia.gameService.services.GameLogicService;
import kdg.colonia.gameService.services.PlayerService;
import kdg.colonia.gameService.services.TradeService;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeDeclineActionTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    GameInfoService infoService;

    @Autowired
    TradeService tradeService;

    @Autowired
    GameLogicService gameLogicService;

    Game game;
    Player currentPlayer;

    @BeforeEach
    public void reset(){
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0)
                , boardCreationService.generate(), cardPileCreationService.generate(),"1");
        currentPlayer = game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().orElseThrow();
        currentPlayer.getRemainingActions().add(PlayerAction.ROLL);
        game.getBoard().addSettlement(new Coordinate(0,0,0, Direction.TOP), 1);
        game.setCurrentPlayerId(1);
        currentPlayer.addResources(1,0,0,0,0);
        game.getPlayers().get(1).addResources(0,2,0,0,0);

        Map<Resource, Integer> resourcesToSend = new HashMap<>();
        resourcesToSend.put(Resource.BRICK, 1);
        resourcesToSend.put(Resource.GRAIN, 0);
        resourcesToSend.put(Resource.WOOL, 0);
        resourcesToSend.put(Resource.LUMBER, 0);
        resourcesToSend.put(Resource.ORE, 0);

        Map<Resource, Integer> resourcesToReceive = new HashMap<>();
        resourcesToReceive.put(Resource.BRICK, 0);
        resourcesToReceive.put(Resource.GRAIN, 2);
        resourcesToReceive.put(Resource.WOOL, 0);
        resourcesToReceive.put(Resource.LUMBER, 0);
        resourcesToReceive.put(Resource.ORE, 0);

        game.setTradeRequest(new TradeRequest(1,2, resourcesToSend, resourcesToReceive));
    }

    @Test
    public void declineTrade() {

        Action action = new TradeDeclineAction(game.getTradeRequest().getReceivingPlayer(), tradeService);

        action.performAction(game);

        assertNull(game.getTradeRequest());
        assertEquals(1, currentPlayer.getBricks());
        assertEquals(1, currentPlayer.getResourcesTotal());
    }

}