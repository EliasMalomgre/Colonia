package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.CardPileCreationService;
import kdg.colonia.gameService.services.PlayerService;
import kdg.colonia.gameService.services.TradeService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class TradeBankActionTest {

    @MockBean
    GameRepository gameRepository;

    @Autowired
    TradeService tradeService;

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    Game game;
    Player currentPlayer;

    @BeforeEach
    public void setup() {
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0),
                boardCreationService.generate(), cardPileCreationService.generate(),"1");
        currentPlayer = game.getPlayers().get(0);

        //set-up mocking
        when(gameRepository.findById(anyString())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);
    }

    @Test
    public void successfulTrade() {
        //Add resources
        currentPlayer.addResources(Resource.BRICK, 4);

        //Trade resources with bank
        new TradeBankAction(tradeService, currentPlayer.getPlayerId(), Resource.BRICK, Resource.GRAIN).performAction(game);

        //Assert correct trade
        assertEquals(1, currentPlayer.getGrain());
        assertEquals(0, currentPlayer.getBricks());
        assertEquals(0, currentPlayer.getLumber());
        assertEquals(0, currentPlayer.getOre());
        assertEquals(0, currentPlayer.getWool());

    }
}