package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.AiConfig;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.Ignore;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@Ignore
@SpringBootTest
public class AIIntegrationTest {

    @Autowired
    AiConfig aiConfig;

    @Autowired
    MonteCarloService monteCarloService;

    @Autowired
    GameLogicService gameLogicService;

    @Autowired
    PlayerService playerService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    BoardCreationService boardCreationService;

    @Ignore
    @Test
    public void nextMove() {
        Game game = new Game(playerService.generateGamePlayers(Stream.of("1","2").collect(Collectors.toList()),0), boardCreationService.generate(), cardPileCreationService.generate(),"0");
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).forEach(player -> player.getRemainingActions().add(PlayerAction.INITIAL1));
        game.setCurrentPlayerId(1);

//        //Repeats same move multiple times
//        for (int i = 0; i < 5; i++) {
//            Game test = monteCarloService.findNextMove(game);
//        }
    }

    /*@Ignore
    @Test
    public void miniGame(){
        Game game = new Game(playerService.generateGamePlayers(Stream.of("1","2").collect(Collectors.toList()),0), boardCreationService.generate(), cardPileCreationService.generate(),"1");
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).forEach(player -> player.getRemainingActions().add(PlayerAction.INITIAL1));
        game.setCurrentPlayerId(1);

//        int moves = 10000;
//
//        for (int i = 0; i < moves; i++) {
//            if(game.getGameState().equals(GameState.FINISHED)){
//                break;
//            }
//            game = monteCarloService.findNextMove(game);
//            Thread.sleep(aiConfig.getWaitTimeBetweenMoves());
//        }

        while(game.getGameState().equals(GameState.ACTIVE)) {
            game = monteCarloService.findNextMove(game);
            try {
                Thread.sleep(aiConfig.getWaitTimeBetweenMoves());
            }catch(InterruptedException ignore){}
        }

        assertEquals(GameState.FINISHED, game.getGameState());
    }*/


    /*@Ignore
    @Test
    public void simulateIncomingTradeRequest() {
        Game game = new Game(playerService.generateGamePlayers(Stream.of("1","2").collect(Collectors.toList()),0), boardCreationService.generate(), cardPileCreationService.generate(),"1");
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).forEach(player -> player.getRemainingActions().add(PlayerAction.INITIAL1));
        game.setCurrentPlayer(1);

        game.getPlayers().get(0).addResources(1,0,0,0,0);
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

        game = monteCarloService.findNextMove(game);

        assertNull(game.getTradeRequest());
        assertEquals(1, game.getCurrentPlayer());
    }*/
}
