package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.config.costobjects.CityConfig;
import kdg.colonia.gameService.config.costobjects.SettlementConfig;
import kdg.colonia.gameService.config.game.AiConfig;
import kdg.colonia.gameService.config.game.DeckConfig;
import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.controllers.RESTToSocketsController;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.ai.actions.Action;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.tiles.TileType;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import kdg.colonia.gameService.utilities.SassyExceptionMessageGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class GameServiceTest {
    private GameService gameService;
    private DummyGameRepository gameRepository;
    private DummyDiceService diceService;
    private PlayerService playerService;
    private GameLogicService gameLogicService;

    @MockBean
    AiConfig aiConfig;
    @Autowired
    CardConfig cardConfig;
    @Autowired
    DeckConfig deckConfig;
    @Autowired
    SettlementConfig settlementConfig;
    @Autowired
    CityConfig cityConfig;
    @Autowired
    GameInfoService gameInfoService;
    @Autowired
    GameConfig gameConfig;
    @Autowired
    MonteCarloService aiService;
    @Autowired
    MongoTemplate mongoTemplate;
    @MockBean
    RESTToSocketsController socketsController;
    @MockBean
    ChatBotService chatBotService;

    @BeforeEach
    public void setUp() {
        //Mocking
        doNothing().when(socketsController).sendNewAchievementNotice(anyString(),anyInt(),anyString());
        doNothing().when(socketsController).sendPauseGameNotice(anyString());
        doNothing().when(socketsController).sendEndTurnNotice(anyString());
        doNothing().when(socketsController).sendTradeNotice(anyString(),anyInt());
        doNothing().when(socketsController).sendRefreshBoard(anyString());

        doNothing().when(chatBotService).sendMessage(anyString(), any(Player.class), any(Action.class));

        this.gameRepository = new DummyGameRepository();
        this.diceService = new DummyDiceService();
        this.playerService = new PlayerService(this.gameInfoService);
        BoardService boardService = new BoardService(playerService, this.gameInfoService, this.gameConfig);
        CardService cardService = new CardService(cardConfig, socketsController);
        IBoardCreationService boardCreationService = new DummyBoardCreationService();
        this.gameLogicService = new GameLogicService(
                boardService,
                new GameInitService(this.diceService),
                new TurnTokenService(),
                cardService,
                playerService,
                diceService,
                new SassyExceptionMessageGenerator()
        );

        this.gameService = new GameService(
                this.gameRepository,
                this.mongoTemplate,
                playerService,
                boardCreationService,
                new SassyExceptionMessageGenerator(),
                new CardPileCreationService(deckConfig),
                new GameInitService(this.diceService),
                new TurnTokenService(),
                new TradeService(),
                this.gameLogicService,
                socketsController,
                aiService,
                chatBotService,
                gameConfig
        );

        List<String> userIds = List.of("usr001", "usr002", "usr003");
        gameService.createGame(userIds, 0,"1");
    }

    @Test
    void setUpGameWithAI() {
        List<String> userIds = List.of("usr001", "usr002");
        gameService.createGame(userIds, 2,"1");
        assertTrue(gameRepository.game.getPlayers().stream().anyMatch(p->p.getPlayerId()==1)
                && !gameRepository.game.getPlayers().stream().filter(p->p.getPlayerId()==1).findFirst().get().isAI());
        assertTrue(gameRepository.game.getPlayers().stream().anyMatch(p->p.getPlayerId()==2)
                && !gameRepository.game.getPlayers().stream().filter(p->p.getPlayerId()==2).findFirst().get().isAI());
        assertTrue(gameRepository.game.getPlayers().stream().anyMatch(p->p.getPlayerId()==3)
                && gameRepository.game.getPlayers().stream().filter(p->p.getPlayerId()==3).findFirst().get().isAI());
        assertTrue(gameRepository.game.getPlayers().stream().anyMatch(p->p.getPlayerId()==4)
                && gameRepository.game.getPlayers().stream().filter(p->p.getPlayerId()==4).findFirst().get().isAI());
    }

    // END TURN TESTS

    @Test
    void endTurnHappy() {
        //ifPresent check not needed because we know they are present in this test
        Player p1 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get();
        Player p2 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get();
        //simulate normal player status on end of turn
        p1.getRemainingActions().add(PlayerAction.BUILD);
        p1.getRemainingActions().add(PlayerAction.BUY);
        p1.getRemainingActions().add(PlayerAction.END_TURN);
        gameService.getGame("game001").setCurrentPlayerId(1);

        assertTrue(gameService.endTurn("game001", 1));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.BUILD));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.BUY));
    }

    @Test
    void endTurnButOtherPlayerTriesToEndTurn() {
        //ifPresent check not needed because we know they are present in this test
        Player p1 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get();
        Player p2 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get();
        //simulate normal player status on end of turn
        p1.getRemainingActions().add(PlayerAction.BUILD);
        p1.getRemainingActions().add(PlayerAction.BUY);
        gameService.getGame("game001").setCurrentPlayerId(1);

        assertFalse(gameService.endTurn("game001", 2));
        assertFalse(p2.getRemainingActions().contains(PlayerAction.ROLL));
        assertFalse(p2.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.BUILD));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.BUY));
    }

    // TURN TESTS

    /*
    test course:
    p1  has started
    p1 rolls dice
    p1 ends turn
    p2 start
    p2 rolls dice
    p2 ends turn
    p3 start
    p3 rolls 7
    p2 discards resources
    p3 moves robber
    p3 ends turn
    p1 start
     */
    @Test
    void normalTurnHappy() {
        //ifPresent check not needed because we know they are present in this test
        Player p1 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get();
        Player p2 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get();
        Player p3 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get();
        //simulate normal player status on end of turn
        p1.getRemainingActions().add(PlayerAction.ROLL);
        p1.getRemainingActions().add(PlayerAction.PLAY_CARD);
        gameService.getGame("game001").setCurrentPlayerId(1);
        p2.getResources().replace(Resource.BRICK, 10);

        gameService.rollDice("game001", 1);
        assertFalse(p1.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.BUY));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.BUILD));

        assertTrue(gameService.endTurn("game001", 1));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.BUILD));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.BUY));

        gameService.rollDice("game001", 2);
        assertFalse(p2.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.BUY));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.BUILD));

        assertTrue(gameService.endTurn("game001", 2));
        assertTrue(p3.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p3.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertFalse(p2.getRemainingActions().contains(PlayerAction.BUILD));
        assertFalse(p2.getRemainingActions().contains(PlayerAction.BUY));

        //player 3 rolls 7
        gameService.rollDice("game001", 3);
        assertTrue(p3.getRemainingActions().contains(PlayerAction.MOVE_ROBBER));
        assertTrue(p2.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES));
        assertFalse(p1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES));
        assertFalse(p3.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES));

        Map<Resource, Integer> discardMap = new HashMap<>();
        discardMap.put(Resource.BRICK,5);
        gameService.discardResources("game001", 2, discardMap);
        assertFalse(p2.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES));
        assertEquals(5, p2.getBricks());

        gameService.moveRobber("game001", 3, gameRepository.game.getBoard().getRobberTile().getCoordinate().getTopRightTile(null, null));
        assertFalse(p3.getRemainingActions().contains(PlayerAction.MOVE_ROBBER));
        assertTrue(p3.getRemainingActions().contains(PlayerAction.BUILD));
        assertTrue(p3.getRemainingActions().contains(PlayerAction.BUY));

        gameService.endTurn("game001", 3);
        assertTrue(p1.getRemainingActions().contains(PlayerAction.ROLL));
        assertTrue(p1.getRemainingActions().contains(PlayerAction.PLAY_CARD));
        assertFalse(p3.getRemainingActions().contains(PlayerAction.BUILD));
        assertFalse(p3.getRemainingActions().contains(PlayerAction.BUY));
    }

    @Test
    void rollDiceSuccessful() {
        String gameId = gameRepository.game.getId();
        Player player = gameRepository.game.getPlayers().get(0);

        //gives user a Roll action
        player.getRemainingActions().add(PlayerAction.ROLL);

        //executing the method
        boolean hasRollBeforeTurn = player.getRemainingActions().contains(PlayerAction.ROLL);
        gameService.rollDice(gameId, player.getPlayerId());
        boolean hasRollAfterTurn = player.getRemainingActions().contains(PlayerAction.ROLL);

        //tests
        assertTrue(hasRollBeforeTurn, "The player is supposed to have a Roll action before the method is called");
        assertFalse(hasRollAfterTurn, "The roll action wasn't correctly removed after the method was called");
    }

    @Test
    void rollDiceForTotal7() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        //gives user a Roll action
        player1.getRemainingActions().add(PlayerAction.ROLL);

        //setting resources
        player1.addResources(0, 0, 4, 0, 0);
        player2.addResources(0, 0, 7, 0, 0);
        player3.addResources(0, 0, 12, 0, 0);

        //executing the method
        diceService.roller = -1; //TODO replace with mockbean: when(diceService.roll()).thenReturn(new int[]{4, 3});
        gameService.rollDice(game.getId(), player1.getPlayerId());

        //tests
        assertTrue(player1.getRemainingActions().contains(PlayerAction.MOVE_ROBBER), "Player 1 should have received a MoveRobber action");
        assertFalse(player2.getRemainingActions().contains(PlayerAction.MOVE_ROBBER), "Player 2 should not have a MoveRobber action");
        assertFalse(player3.getRemainingActions().contains(PlayerAction.MOVE_ROBBER), "Player 3 should not have a MoveRobber action");

        assertFalse(player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES), "Player 2 should not have a DiscardResources action");
        assertFalse(player2.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES), "Player 2 should not have a DiscardResources action");
        assertTrue(player3.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES), "Player 3 should have received a DiscardResources action");
    }

    @Test
    void rollDiceIllegalState() {
        String gameId = gameRepository.game.getId();
        Player player = gameRepository.game.getPlayers().get(0);

        assertThrows(IllegalStateException.class, () -> {
            gameService.rollDice(gameId, player.getPlayerId());
        }, "The player had no Roll action and should have triggered an illegal state");
    }

    @Test //todo should be moved over to GameLogicService
    void processDiceRoll() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        Board board = game.getBoard();

        //adding settlements
        board.getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.TOP), player1.getPlayerId()));
        board.getSettlements().add(new Settlement(new Coordinate(1, -1, 0, Direction.LEFT), player2.getPlayerId()));
        board.getSettlements().add(new Settlement(new Coordinate(0, -1, 1, Direction.LEFT), player3.getPlayerId()));

        //executing the method
        player1.getRemainingActions().add(PlayerAction.ROLL);
        gameLogicService.processDiceRoll(game, player1, 8);

        //tests
        assertEquals(1, player1.getWool(), "Player 1 should have received 1 wool");
        assertEquals(2, player2.getWool(), "Player 2 should have received 2 wool");
        assertEquals(1, player3.getWool(), "Player 3 should have received 1 wool");

        //executing the method
        player2.getRemainingActions().add(PlayerAction.ROLL);
        gameLogicService.processDiceRoll(game, player2, 5);

        //tests
        assertEquals(1, player1.getLumber(), "Player 1 should have received 1 lumber");
        assertEquals(1, player2.getLumber(), "Player 2 should have received 1 lumber");
        assertEquals(0, player3.getLumber(), "Player 3 should have received 0 lumber");

        //adding a city for player 3
        Settlement settlement3 = new Settlement(new Coordinate(-1, 1, 0, Direction.LEFT), player3.getPlayerId());
        settlement3.setCity(true);
        board.getSettlements().add(settlement3);

        //adding another settlement for player 1
        Settlement settlement1 = new Settlement(new Coordinate(-2, 1, 1, Direction.TOP), player1.getPlayerId());
        board.getSettlements().add(settlement1);

        //executing the method
        player3.getRemainingActions().add(PlayerAction.ROLL);
        gameLogicService.processDiceRoll(game, player3, 6);

        //tests
        assertEquals(2, player1.getWool(), "Player 1 has no city and should have received 1 wool (total of 2 wool due to a previous turn)");
        assertEquals(2, player1.getLumber(), "Player 1 has no city and should have received 1 lumber (total of 2 lumber due to a previous turn)");

        assertEquals(3, player3.getWool(), "Player 3 has a city and should have received 2 wool (total of 3 wool due to a previous turn)");
        assertEquals(2, player3.getLumber(), "Player 3 has a city and should have received 2 lumber");
    }

    @Test
    void moveRobberToValidLocationWithoutPlayersToStealFrom() {
        Game game = gameRepository.game;
        Player player = gameRepository.game.getPlayers().get(0);
        Board board = game.getBoard();

        //gives user a moveRobber action
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);

        //executing the method
        boolean hasMoveRobberBeforeTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        boolean hasStealBeforeTurn = player.getRemainingActions().contains(PlayerAction.STEAL);
        List<Integer> affectedPlayers = gameService.moveRobber(game.getId(), player.getPlayerId(), new Coordinate(2, -1, -1));

        boolean hasMoveRobberAfterTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        boolean hasStealAfterTurn = player.getRemainingActions().contains(PlayerAction.STEAL);
        Coordinate newRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        //tests
        assertTrue(hasMoveRobberBeforeTurn, "The player is supposed to have a MoveRobber action before the method is called");
        assertFalse(hasMoveRobberAfterTurn, "The MoveRobber action wasn't correctly removed after the method was called");

        assertFalse(hasStealBeforeTurn, "The player shouldn't have had a Steal action before this turn");
        assertFalse(hasStealAfterTurn, "There are no players in reach of the robber after moving, so no Steal action is granted");

        assertEquals(2, newRobberLocation.getX(), "X Coordinate should have changed to 2");
        assertEquals(-1, newRobberLocation.getY(), "Y Coordinate should have changed to -1");
        assertEquals(-1, newRobberLocation.getZ(), "Z Coordinate should have changed to -1");

        assertEquals(0, affectedPlayers.size(), "No settlements around the robber, so we can't steal from anyone");
    }

    @Test
    void moveRobberToValidLocationWithPlayersToStealFrom() {
        Game game = gameRepository.game;
        Player player = gameRepository.game.getPlayers().get(0);
        Board board = game.getBoard();

        //gives user a moveRobber action
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);

        //places a couple settlements on target location
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.TOP),1)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.LEFT),2)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-2,0, Direction.LEFT),3)); //In reach

        //executing the method
        boolean hasMoveRobberBeforeTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        boolean hasStealBeforeTurn = player.getRemainingActions().contains(PlayerAction.STEAL);
        List<Integer> affectedPlayers = gameService.moveRobber(game.getId(), player.getPlayerId(), new Coordinate(2, -1, -1));

        boolean hasMoveRobberAfterTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        boolean hasStealAfterTurn = player.getRemainingActions().contains(PlayerAction.STEAL);
        Coordinate newRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        //tests
        assertTrue(hasMoveRobberBeforeTurn, "The player is supposed to have a MoveRobber action before the method is called");
        assertFalse(hasMoveRobberAfterTurn, "The MoveRobber action wasn't correctly removed after the method was called");

        assertFalse(hasStealBeforeTurn, "The player shouldn't have had a Steal action before this turn");
        assertTrue(hasStealAfterTurn, "There are players in reach of the robber after moving, so Steal action is granted");

        assertEquals(2, newRobberLocation.getX(), "X Coordinate should have changed to 2");
        assertEquals(-1, newRobberLocation.getY(), "Y Coordinate should have changed to -1");
        assertEquals(-1, newRobberLocation.getZ(), "Z Coordinate should have changed to -1");

        assertEquals(2, affectedPlayers.size(), "There should be two people we can steal from after this turn");
    }

    @Test
    void moveRobberToIdenticalLocation() {
        Game game = gameRepository.game;
        Player player = gameRepository.game.getPlayers().get(0);

        //gives user a moveRobber action
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);

        //executing the method
        boolean hasMoveRobberBeforeTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        Coordinate currentRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        gameService.moveRobber(game.getId(), player.getPlayerId(), currentRobberLocation);

        boolean hasMoveRobberAfterTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        Coordinate newRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        //tests
        assertTrue(hasMoveRobberBeforeTurn, "The player is supposed to have a MoveRobber action before the method is called");
        assertTrue(hasMoveRobberAfterTurn, "The MoveRobber method failed, the MoveRobber action shouldn't have been removed");

        assertEquals(currentRobberLocation.getX(), newRobberLocation.getX(), "X Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getY(), newRobberLocation.getY(), "Y Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getZ(), newRobberLocation.getZ(), "Z Coordinate shouldn't have changed");
    }

    @Test
    void moveRobberToWater() {
        Game game = gameRepository.game;
        Player player = gameRepository.game.getPlayers().get(0);

        //gives user a moveRobber action
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);

        //executing the method
        boolean hasMoveRobberBeforeTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        Coordinate currentRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        gameService.moveRobber(game.getId(), player.getPlayerId(), new Coordinate(-3, +3, 0));

        boolean hasMoveRobberAfterTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        Coordinate newRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        //tests
        assertTrue(hasMoveRobberBeforeTurn, "The player is supposed to have a MoveRobber action before the method is called");
        assertTrue(hasMoveRobberAfterTurn, "The MoveRobber method failed, the MoveRobber action shouldn't have been removed");

        assertEquals(currentRobberLocation.getX(), newRobberLocation.getX(), "X Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getY(), newRobberLocation.getY(), "Y Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getZ(), newRobberLocation.getZ(), "Z Coordinate shouldn't have changed");
    }

    @Test
    void moveRobberIllegalState() {
        Game game = gameRepository.game;
        Player player = gameRepository.game.getPlayers().get(0);

        //executing the method
        boolean hasMoveRobberBeforeTurn = player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER);
        Coordinate currentRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        assertThrows(IllegalStateException.class, () -> {
            gameService.moveRobber(game.getId(), player.getPlayerId(), new Coordinate(2, -1, -1));
        }, "The player had no MoveRobber action and should have triggered an illegal state");

        Coordinate newRobberLocation = game.getBoard().getRobberTile().getCoordinate();

        //tests
        assertFalse(hasMoveRobberBeforeTurn, "The player is not supposed to have a MoveRobber action before the method is called in this situation");

        assertEquals(currentRobberLocation.getX(), newRobberLocation.getX(), "X Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getY(), newRobberLocation.getY(), "Y Coordinate shouldn't have changed");
        assertEquals(currentRobberLocation.getZ(), newRobberLocation.getZ(), "Z Coordinate shouldn't have changed");
    }

    @Test
    void stealResourcesSuccess(){
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        Board board = game.getBoard();
        board.setRobberTile(board.getTileForCoordinate(new Coordinate(2,-1,-1)));
        //places a couple settlements on target location
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.TOP),1)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.LEFT),2)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-2,0, Direction.LEFT),3)); //In reach

        //gives user a Steal action
        player1.getRemainingActions().add(PlayerAction.STEAL);

        player1.addResources(1,0,0,0,0);
        player2.addResources(0,0,1,0,0);
        player3.addResources(0,0,0,0,1);

        Resource resource = gameService.stealResources(game.getId(), 1, 2);
        boolean hasStealAfterTurn = player1.getRemainingActions().contains(PlayerAction.STEAL);

        assertEquals(Resource.LUMBER, resource, "1 Lumber should have been transferred");
        assertEquals(1, player1.getResources().get(Resource.LUMBER), "Player 1 should have gained 1 lumber");
        assertEquals(0, player2.getResources().get(Resource.LUMBER), "Player 2 should have lost 1 lumber");
        assertFalse(hasStealAfterTurn,"The theft of resources should have been successful, taking away the steal action.");
    }

    @Test
    void stealResourcesFailNotEnoughResources(){
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        Board board = game.getBoard();
        board.setRobberTile(board.getTileForCoordinate(new Coordinate(2,-1,-1)));
        //places a couple settlements on target location
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.TOP),1)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.LEFT),2)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-2,0, Direction.LEFT),3)); //In reach

        //gives user a Steal action
        player1.getRemainingActions().add(PlayerAction.STEAL);

        player1.addResources(1,0,0,0,0);

        Resource resource = gameService.stealResources(game.getId(), 1, 2);
        boolean hasStealAfterTurn = player1.getRemainingActions().contains(PlayerAction.STEAL);

        assertEquals(Resource.NOTHING, resource, "Nothing should have been transferred");
        assertEquals(1, player1.getResourcesTotal(), "Player 1 should have remained at total of 1");
        assertEquals(0, player2.getResourcesTotal(), "Player 2 should have remained at total of 0");
        assertFalse(hasStealAfterTurn,"The theft of resources was unsuccesful, but the steal action should have been taken away anyway!");
    }

    @Test
    void stealResourcesFailNotAffected(){
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        Board board = game.getBoard();
        board.setRobberTile(board.getTileForCoordinate(new Coordinate(-2,2,0)));
        //places a couple settlements on target location
        board.getSettlements().add(new Settlement(new Coordinate(-2,2,0, Direction.TOP),1)); //In reach, BUT IS THE PLAYER
        board.getSettlements().add(new Settlement(new Coordinate(2,-1,-1, Direction.LEFT),2)); //Not in reach
        board.getSettlements().add(new Settlement(new Coordinate(2,-2,0, Direction.LEFT),3)); //Not in reach

        //gives user a Steal action
        player1.getRemainingActions().add(PlayerAction.STEAL);

        player1.addResources(1,0,0,0,0);
        player2.addResources(0,0,1,0,0);
        player3.addResources(0,0,0,0,1);

        assertThrows(IllegalStateException.class, () -> {
            gameService.stealResources(game.getId(), 1, 2);
        }, "Invalid option to steal from, no players near the robber!");

        boolean hasStealAfterTurn = player1.getRemainingActions().contains(PlayerAction.STEAL);

        assertEquals(0, player1.getResources().get(Resource.LUMBER), "Player 1 should have remained at 0 lumber");
        assertEquals(1, player2.getResources().get(Resource.LUMBER), "Player 2 should have remained at 1 lumber");
        assertTrue(hasStealAfterTurn,"No theft of resources has started, due to picking an unaffected player");
    }

    @Test
    void discardResourcesSuccessful() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        Player player3 = game.getPlayers().get(2);

        //gives user a DiscardResources action
        player1.getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        //total of 9 cards for player 1
        player1.addResources(2, 2, 2, 2, 1);

        //total of 4 cards for player 1
        player2.addResources(0, 0, 0, 0, 4);

        //total of 3 cards for player 1
        player3.addResources(0, 0, 0, 1, 2);

        //executing the method
        int previousTotalPlayer1 = player1.getResourcesTotal();
        int previousTotalPlayer2 = player2.getResourcesTotal();
        int previousTotalPlayer3 = player3.getResourcesTotal();

        //half of 9 is 4.5, should become 4
        boolean hasDiscardBeforeTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);
        Map<Resource, Integer> discardmap = new HashMap<>();
        discardmap.put(Resource.WOOL,1);
        discardmap.put(Resource.BRICK,1);
        discardmap.put(Resource.GRAIN,1);
        discardmap.put(Resource.LUMBER,1);
        gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap);
        boolean hasDiscardAfterTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);

        int newTotalPlayer1 = player1.getResourcesTotal();
        int newTotalPlayer2 = player2.getResourcesTotal();
        int newTotalPlayer3 = player3.getResourcesTotal();

        //tests
        assertEquals(1, player1.getBricks(), "Player 1 should have 1 BRICK after discarding 1");
        assertEquals(1, player1.getGrain(), "Player 1 should have 1 GRAIN after discarding 1");
        assertEquals(1, player1.getLumber(), "Player 1 should have 1 LUMBER after discarding 1");
        assertEquals(2, player1.getOre(), "Player 1 should have 1 ORE after discarding 0");
        assertEquals(0, player1.getWool(), "Player 1 should have 0 WOOL after discarding 1");

        assertNotEquals(previousTotalPlayer1, newTotalPlayer1, "Player 1's total should have changed");
        assertEquals(previousTotalPlayer2, newTotalPlayer2, "Player 2's total should remain unchanged");
        assertEquals(previousTotalPlayer3, newTotalPlayer3, "Player 3's total should remain unchanged");

        assertTrue(hasDiscardBeforeTurn, "The player is supposed to have a DiscardResources action before the method is called");
        assertFalse(hasDiscardAfterTurn, "The DiscardResources action wasn't correctly removed after the method was called");
    }

    @Test
    void discardResourcesWrongTotal() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);

        //gives user a DiscardResources action
        player1.getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        //total of 9 cards for player 1
        player1.addResources(2, 2, 2, 2, 1);

        //executing the method
        int previousTotalPlayer1 = player1.getResourcesTotal();

        //half of 9 is 4.5, should become 4. For the test we try to send 5
        boolean hasDiscardBeforeTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);
        Map<Resource, Integer> discardmap = new HashMap<>();
        discardmap.put(Resource.WOOL,1);
        discardmap.put(Resource.BRICK,1);
        discardmap.put(Resource.GRAIN,1);
        discardmap.put(Resource.LUMBER,1);
        discardmap.put(Resource.ORE,1);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap);
        }, "Player sent too many resources, should have resulted in an exception");

        Map<Resource, Integer> discardmap2 = new HashMap<>();
        discardmap2.put(Resource.WOOL,1);
        discardmap2.put(Resource.BRICK,1);
        discardmap2.put(Resource.GRAIN,1);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap2);
        }, "Player sent too few resources, should have resulted in an exception");

        boolean hasDiscardAfterTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);

        int newTotalPlayer1 = player1.getResourcesTotal();

        //tests
        assertEquals(previousTotalPlayer1, newTotalPlayer1, "Player 1's total should remain unchanged");

        assertTrue(hasDiscardBeforeTurn, "The player is supposed to have a DiscardResources action before the method is called");
        assertTrue(hasDiscardAfterTurn, "The method should have failed, meaning the DiscardResources action shouldn't have been removed");
    }

    @Test
    void discardResourcesEmptyMap() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);

        //gives user a DiscardResources action
        player1.getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        //total of 9 cards for player 1
        player1.addResources(2, 2, 2, 2, 1);

        //executing the method
        int previousTotalPlayer1 = player1.getResourcesTotal();

        //half of 9 is 4.5, should become 4. For the test we try to send 5
        boolean hasDiscardBeforeTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);
        Map<Resource, Integer> discardmap = new HashMap<>(); //empty map
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap);
        }, "Player sent no resources, should have resulted in an exception");
        boolean hasDiscardAfterTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);

        int newTotalPlayer1 = player1.getResourcesTotal();

        //tests
        assertEquals(previousTotalPlayer1, newTotalPlayer1, "Player 1's total should remain unchanged");

        assertTrue(hasDiscardBeforeTurn, "The player is supposed to have a DiscardResources action before the method is called");
        assertTrue(hasDiscardAfterTurn, "The method should have failed, meaning the DiscardResources action shouldn't have been removed");
    }

    @Test
    void discardResourcesWrongSubTotal() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);

        //gives user a DiscardResources action
        player1.getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        //total of 9 cards for player 1
        player1.addResources(6, 1, 0, 2, 0);

        //executing the method
        int previousTotalPlayer1 = player1.getResourcesTotal();

        Map<Resource, Integer> discardmap = new HashMap<>();
        discardmap.put(Resource.BRICK,1);
        discardmap.put(Resource.GRAIN,2);
        discardmap.put(Resource.ORE,1);

        //half of 9 is 4.5, should become 4. For the test we try to send 5
        boolean hasDiscardBeforeTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap);
        }, "Player has 1 GRAIN, tried to remove 2, should have resulted in exception");

        Map<Resource, Integer> discardmap2 = new HashMap<>();
        discardmap2.put(Resource.BRICK,3);
        discardmap2.put(Resource.LUMBER,1);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap2);
        }, "Player has 0 LUMBER, tried to remove 1, should have resulted in exception");

        Map<Resource, Integer> discardmap3 = new HashMap<>();
        discardmap3.put(Resource.BRICK,1);
        discardmap3.put(Resource.ORE,3);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap3);
        }, "Player has 2 ORE, tried to remove 3, should have resulted in exception");

        Map<Resource, Integer> discardmap4 = new HashMap<>();
        discardmap4.put(Resource.BRICK,3);
        discardmap4.put(Resource.WOOL,1);
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap4);
        }, "Player has 0 WOOL, tried to remove 1, should have resulted in exception");

        boolean hasDiscardAfterTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);

        int newTotalPlayer1 = player1.getResourcesTotal();

        //tests
        assertEquals(previousTotalPlayer1, newTotalPlayer1, "Player 1's total should remain unchanged");

        assertTrue(hasDiscardBeforeTurn, "The player is supposed to have a DiscardResources action before the method is called");
        assertTrue(hasDiscardAfterTurn, "The method should have failed, meaning the DiscardResources action shouldn't have been removed");
    }

    @Test
    void discardResourcesIllegalState() {
        Game game = gameRepository.game;
        Player player1 = game.getPlayers().get(0);

        //total of 9 cards for player 1
        player1.addResources(2, 2, 2, 2, 1);

        //executing the method
        int previousTotalPlayer1 = player1.getResourcesTotal();

        Map<Resource, Integer> discardmap = new HashMap<>();
        discardmap.put(Resource.WOOL,1);
        discardmap.put(Resource.BRICK,1);
        discardmap.put(Resource.GRAIN,1);
        discardmap.put(Resource.LUMBER,1);
        //half of 9 is 4.5, should become 4. For the test we try to send 5
        boolean hasDiscardBeforeTurn = player1.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES);
        assertThrows(IllegalStateException.class, () -> {
            gameService.discardResources(game.getId(), player1.getPlayerId(), discardmap);
        }, "The player had no DiscardResources action and should have triggered an illegal state");

        int newTotalPlayer1 = player1.getResourcesTotal();

        //tests
        assertEquals(previousTotalPlayer1, newTotalPlayer1, "Player 1's total should remain unchanged");

        assertFalse(hasDiscardBeforeTurn, "The player is not supposed to have a DiscardResources action before the method is called in this situation");
    }

    //Initial phase tests

    private void initialiseUpToPlayer1() {
        //initial rolls
        gameService.rollForInitiative("game001", "usr001");
        gameService.rollForInitiative("game001", "usr002");
        gameService.rollForInitiative("game001", "usr003");

        //player 1 gets the INITIAL1 token
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
    }

    /**
     * Tests wheter the gameService correctly assigns playerIds based on initial rolls
     */
    @Test
    void rollForInitiativeHappyTest() {
        //player 1
        gameService.rollForInitiative("game001", "usr001");

        //player 3
        gameService.rollForInitiative("game001", "usr002");

        //player 2
        gameService.rollForInitiative("game001", "usr003");

        assertEquals(1, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr001")).findFirst().get().getPlayerId());
        assertEquals(2, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr003")).findFirst().get().getPlayerId());
        assertEquals(3, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr002")).findFirst().get().getPlayerId());
    }


    @Test
    void rollForInitiativeUserRollsTwiceTest() {
        //player 1
        gameService.rollForInitiative("game001", "usr001");

        //player 3
        gameService.rollForInitiative("game001", "usr002");
        //this roll should be ignored
        gameService.rollForInitiative("game001", "usr002");

        //player 2
        gameService.rollForInitiative("game001", "usr003");

        assertEquals(1, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr001")).findFirst().get().getPlayerId());
        assertEquals(2, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr003")).findFirst().get().getPlayerId());
        assertEquals(3, gameRepository.game.getPlayers().stream().filter(p -> p.getUserId().equals("usr002")).findFirst().get().getPlayerId());
    }

    @Test
    void initialPhaseHappyTest() throws Exception {
        initialiseUpToPlayer1();

        Player p1 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get();
        Player p2 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get();
        Player p3 = gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get();

        //player 1 builds a settlement in a valid position
        Coordinate coordSP1 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 1, coordSP1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP1));

        //player 1 builds a road in a valid position
        Coordinate coordRP1 = new Coordinate(0, 0, 0, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordRP1);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordRP1));

        //player 2 now has the INITIAL1 token and player 1 lost it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));

        //player 2 builds a settlement in a valid position
        Coordinate coordSP2 = new Coordinate(-1, 1, 0, Direction.LEFT);
        gameService.build("game001", 2, coordSP2);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP2));

        //player 2 builds a road in a valid position
        Coordinate coordRP2 = new Coordinate(-1, 1, 0, CardDir.WEST);
        gameService.build("game001", 2, coordRP2);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(s -> s.getCoordinate() == coordRP2));

        //player 3 now has the INITIAL1 token and player 2 lost it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));

        //player 3 builds a settlement in a valid position
        Coordinate coordSP3 = new Coordinate(0, -1, 1, Direction.LEFT);
        gameService.build("game001", 3, coordSP3);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP3));

        //player 3 builds a road in a valid position
        Coordinate coordRP3 = new Coordinate(0, -1, 1, CardDir.WEST);
        gameService.build("game001", 3, coordRP3);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(s -> s.getCoordinate() == coordRP3));

        //player 3 now has the INITIAL2 token and loses their INITIAL1 token
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL2));

        //player 3 builds a settlement in a valid position
        Coordinate coordS2P3 = new Coordinate(-1, 0, 1, Direction.LEFT);
        gameService.build("game001", 3, coordS2P3);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordS2P3));

        //Check resources gained
        assertEquals(2, p3.getBricks());
        assertEquals(0, p3.getGrain());
        assertEquals(0, p3.getLumber());
        assertEquals(0, p3.getOre());
        assertEquals(1, p3.getWool());


        //player 3 builds a road in a valid position
        Coordinate coordR2P3 = new Coordinate(-1, 0, 1, CardDir.WEST);
        gameService.build("game001", 3, coordR2P3);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(s -> s.getCoordinate() == coordR2P3));

        //player 2 has the INITIAL2 token and player 3 loses it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 3).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL2));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL2));

        //player 2 builds a settlement in a valid position
        Coordinate coordS2P2 = new Coordinate(-1, 2, -1, Direction.LEFT);
        gameService.build("game001", 2, coordS2P2);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordS2P2));

        //Check resources gained
        assertEquals(0, p2.getBricks());
        assertEquals(1, p2.getGrain());
        assertEquals(0, p2.getLumber());
        assertEquals(0, p2.getOre());
        assertEquals(0, p2.getWool());

        //player 2 builds a road in a valid position
        Coordinate coordR2P2 = new Coordinate(-1, 2, -1, CardDir.WEST);
        gameService.build("game001", 2, coordR2P2);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(s -> s.getCoordinate() == coordR2P2));

        //player 1 has the INITIAL2 token and player 2 loses it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL2));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL2));

        //player 1 builds a settlement in a valid position
        Coordinate coordS2P1 = new Coordinate(1, 0, -1, Direction.TOP);
        gameService.build("game001", 1, coordS2P1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordS2P1));

        //Check resources gained
        assertEquals(0, p1.getBricks());
        assertEquals(2, p1.getGrain());
        assertEquals(1, p1.getLumber());
        assertEquals(0, p1.getOre());
        assertEquals(0, p1.getWool());

        //player 1 builds a road in a valid position
        Coordinate coordR2P1 = new Coordinate(1, 0, -1, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordR2P1);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordR2P1));

        //TODO: make sure the first real turn starts
    }

    @Test
    void initialPhasePlayerBuilds2SettlementsTest() throws Exception {
        initialiseUpToPlayer1();

        //player 1 builds a settlement in a valid position
        Coordinate coordSP1 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 1, coordSP1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP1));

        //player 1 builds a second settlement in a valid position
        Coordinate coordS2P1 = new Coordinate(1, 0, -1, Direction.TOP);
        gameService.build("game001", 1, coordS2P1);
        assertFalse(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordS2P1));
    }

    @Test
    void initialPhasePlayerBuildsOutOfTurnTest() throws Exception {
        initialiseUpToPlayer1();

        //player 2 builds a settlement out of turn in a valid position
        Coordinate coordS2P2 = new Coordinate(-1, 2, -1, Direction.LEFT);
        gameService.build("game001", 2, coordS2P2);
        assertFalse(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordS2P2));
    }

    @Test
    void initialPhasePlayerBuildsARoadFirstTest() throws Exception {
        initialiseUpToPlayer1();

        //player 1 builds a road in a valid position
        Coordinate coordRP1 = new Coordinate(0, 0, 0, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordRP1);
        assertFalse(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordRP1));
    }

    @Test
    void initialPhasePlayerBuildsAnInvalidRoadTest() throws Exception {
        initialiseUpToPlayer1();

        //player 1 builds a settlement in a valid position
        Coordinate coordSP1 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 1, coordSP1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP1));

        //player 1 builds a road in an invalid position
        Coordinate coordR2P1 = new Coordinate(1, 0, -1, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordR2P1);
        assertFalse(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordR2P1));
    }

    @Test
    void initialPhasePlayerBuildsAnInvalidSettlementTooCloseTest() throws Exception {
        initialiseUpToPlayer1();

        //player 1 builds a settlement in a valid position
        Coordinate coordSP1 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 1, coordSP1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP1));

        //player 1 builds a road in a valid position
        Coordinate coordRP1 = new Coordinate(0, 0, 0, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordRP1);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordRP1));

        //player 2 now has the INITIAL1 token and player 1 lost it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));

        //player 2 builds a settlement too close to another
        Coordinate coordSP2 = new Coordinate(0, 0, 0, Direction.LEFT);
        gameService.build("game001", 2, coordSP2);
        assertFalse(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP2));
    }

    @Test
    void initialPhasePlayerBuildsAnInvalidSettlementAlreadyOccupiedTest() throws Exception {
        initialiseUpToPlayer1();

        //player 1 builds a settlement in a valid position
        Coordinate coordSP1 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 1, coordSP1);
        assertTrue(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP1));

        //player 1 builds a road in a valid position
        Coordinate coordRP1 = new Coordinate(0, 0, 0, CardDir.NORTH_EAST);
        gameService.build("game001", 1, coordRP1);
        assertTrue(gameRepository.game.getBoard().getRoads().stream().anyMatch(r -> r.getCoordinate() == coordRP1));

        //player 2 now has the INITIAL1 token and player 1 lost it
        assertFalse(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));
        assertTrue(gameRepository.game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().get().getRemainingActions().contains(PlayerAction.INITIAL1));

        //player 2 builds a settlement on another settlement
        Coordinate coordSP2 = new Coordinate(0, 0, 0, Direction.TOP);
        gameService.build("game001", 2, coordSP2);
        assertFalse(gameRepository.game.getBoard().getSettlements().stream().anyMatch(s -> s.getCoordinate() == coordSP2));
    }

    @Test
    /**
     * A lot of logic in this test depends on the previous state,
     * therefore we made the decision to make a long realistic path,
     * instead of small independent tests
     */
    public void updateLongestRoadRealisticPath() {
        Game game = gameRepository.game;
        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Player p3 = game.getPlayers().get(2);
        List<Achievement> p1a = game.getPlayers().get(0).getAchievements();
        List<Achievement> p2a = game.getPlayers().get(1).getAchievements();
        List<Achievement> p3a = game.getPlayers().get(2).getAchievements();

        List<Road> roads = game.getBoard().getRoads();
        List<Settlement> settlements = game.getBoard().getSettlements();

        //player 1 path: branch on 2-4 split of p2
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.WEST), 1));
        roads.add(new Road(new Coordinate(-1, 0, 1, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, -1, 1, CardDir.WEST), 1));

        //player 2 path
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_WEST), 2));
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_EAST), 2));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 2));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_EAST), 2));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 2));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 2));

        int previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        int newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(0, previousHolder, "There shouldn't have been a longestRoadHolder before this point");
        assertEquals(2, newHolder, "The longest road goes to player 2, with a 6 long road");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertTrue(p2a.contains(Achievement.LONGEST_ROAD), "P2 should have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(2, p2.getVictoryPointsAmount(), "Player 2 should have 2 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 1 adds a settlement and obstructs player 2's longest road
        settlements.add(new Settlement(new Coordinate(0, 0, 0, Direction.LEFT), 1));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(2, previousHolder, "Previous longestRoadHolder should have been p2");
        assertEquals(0, newHolder, "P1 has obstructed the longest road, should have reverted to no owner");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertFalse(p2a.contains(Achievement.LONGEST_ROAD), "P2 should not have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(0, p2.getVictoryPointsAmount(), "Player 2 should have 0 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 1 adds two roads and should now have a 5 long road
        roads.add(new Road(new Coordinate(-1, -1, 2, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-2, 0, 2, CardDir.NORTH_EAST), 1));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(0, previousHolder, "There should no longer be a longestRoadHolder at this point");
        assertEquals(1, newHolder, "The longest road goes to player 1, with a 5 long road");
        assertTrue(p1a.contains(Achievement.LONGEST_ROAD), "P1 should have Longest Road");
        assertFalse(p2a.contains(Achievement.LONGEST_ROAD), "P2 should not have Longest Road");
        assertEquals(2, p1.getVictoryPointsAmount(), "Player 1 should have 2 Victory Points");
        assertEquals(0, p2.getVictoryPointsAmount(), "Player 2 should have 0 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 2 adds one extra road and should now also have a 5 long road
        roads.add(new Road(new Coordinate(2, -2, 0, CardDir.NORTH_WEST), 2));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(1, previousHolder, "Player 1 should have gotten longestRoadHolder before this point");
        assertEquals(1, newHolder, "Player 1 already had a 5-long road before player 2, and should therefore keep it");
        assertTrue(p1a.contains(Achievement.LONGEST_ROAD), "P1 should have Longest Road");
        assertFalse(p2a.contains(Achievement.LONGEST_ROAD), "P2 should not have Longest Road");
        assertEquals(2, p1.getVictoryPointsAmount(), "Player 1 should have 2 Victory Points");
        assertEquals(0, p2.getVictoryPointsAmount(), "Player 2 should have 0 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 2 adds one extra road and should now also have a 6 long road
        roads.add(new Road(new Coordinate(2, -2, 0, CardDir.NORTH_EAST), 2));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(1, previousHolder, "Player 1 should have retained longestRoadHolder at this point");
        assertEquals(2, newHolder, "The longest road goes to player 2, with a 6 long road, while p1 only has 5");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertTrue(p2a.contains(Achievement.LONGEST_ROAD), "P2 should have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(2, p2.getVictoryPointsAmount(), "Player 2 should have 2 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 1 adds one extra road and should now also have a 6 long road
        roads.add(new Road(new Coordinate(-2, 0, 2, CardDir.NORTH_WEST), 1));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(2, previousHolder, "Player 2 should have gotten longestRoadHolder before this point");
        assertEquals(2, newHolder, "Player 2 already had a 6-long road before player 1, and should therefore keep it");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertTrue(p2a.contains(Achievement.LONGEST_ROAD), "P2 should have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(2, p2.getVictoryPointsAmount(), "Player 2 should have 2 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");

        //player 3 adds 7 roads and should get longest road
        roads.add(new Road(new Coordinate(0, 2, -2, CardDir.WEST), 3));
        roads.add(new Road(new Coordinate(0, 2, -2, CardDir.NORTH_WEST), 3));
        roads.add(new Road(new Coordinate(0, 2, -2, CardDir.NORTH_EAST), 3));
        roads.add(new Road(new Coordinate(1, 1, -2, CardDir.NORTH_WEST), 3));
        roads.add(new Road(new Coordinate(1, 1, -2, CardDir.NORTH_EAST), 3));
        roads.add(new Road(new Coordinate(2, 0, -2, CardDir.NORTH_WEST), 3));
        roads.add(new Road(new Coordinate(2, 0, -2, CardDir.NORTH_EAST), 3));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(2, previousHolder, "Player 2 should have retained longestRoadHolder at this point");
        assertEquals(3, newHolder, "Player 3 now has a 7-long road and should gain the achievement");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertFalse(p2a.contains(Achievement.LONGEST_ROAD), "P2 should not have Longest Road");
        assertTrue(p3a.contains(Achievement.LONGEST_ROAD), "P3 should have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(0, p2.getVictoryPointsAmount(), "Player 2 should have 0 Victory Points");
        assertEquals(2, p3.getVictoryPointsAmount(), "Player 3 should have 2 Victory Points");

        //player 2 adds a settlement and obstructs player 3's longest road
        settlements.add(new Settlement(new Coordinate(1, 1, -2, Direction.LEFT), 2));

        previousHolder = game.getPlayerIdWithLongestRoad();
        gameService.updateLongestRoad(game);
        newHolder = game.getPlayerIdWithLongestRoad();
        assertEquals(3, previousHolder, "Player 2 should have retained longestRoadHolder at this point");
        assertEquals(0, newHolder, "Both Player 1 and Player 2 have a tie at 6, after splitting player 3's road at the 3-4 split. None should have longestRoadHolder");
        assertFalse(p1a.contains(Achievement.LONGEST_ROAD), "P1 should not have Longest Road");
        assertFalse(p2a.contains(Achievement.LONGEST_ROAD), "P2 should not have Longest Road");
        assertFalse(p3a.contains(Achievement.LONGEST_ROAD), "P3 should not have Longest Road");
        assertEquals(0, p1.getVictoryPointsAmount(), "Player 1 should have 0 Victory Points");
        assertEquals(0, p2.getVictoryPointsAmount(), "Player 2 should have 0 Victory Points");
        assertEquals(0, p3.getVictoryPointsAmount(), "Player 3 should have 0 Victory Points");
    }

    //PLAYER PLAYS CARD TESTS

    /**
     * player has a knight card, has the PLAY_CARD token and plays their card
     */
    @Test
    void playerPlaysKnightHappy() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertTrue(gameService.playCard("game001", 1, ProgressCardType.KNIGHT));
    }

    /**
     * player has a knight card, but does not have the PLAY_CARD token
     */
    @Test
    void playerPlaysKnightButHasNoToken() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.KNIGHT));
    }

    /**
     * player has the PLAY_CARD token but does not have a knight card
     */
    @Test
    void playerPlaysKnightButHasNoKnight() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.KNIGHT));
    }

    /**
     * player has a victorypoint card and the PLAY_CARD token and plays their card
     */
    @Test
    void playerPlaysVictoryPointCardHappy() throws Exception {
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.VICTORY_POINT));
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertTrue(gameService.playCard("game001", 1, ProgressCardType.VICTORY_POINT));
    }

    /**
     * player has a victorypoint card but does not have the PLAY_CARD token
     */
    @Test
    void playerPlaysVictoryPointCardButHasNoToken() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.VICTORY_POINT));
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.VICTORY_POINT));
    }

    /**
     * player has the PLAY_CARD token but does not have a victorypoint card
     */
    @Test
    void playerPlaysVictoryPointCardButHasNoVictoryPointCard() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.VICTORY_POINT));
    }

    /**
     * player has the PLAY_CARD token, plays a YOP card (which they have) and selects the 2 resources they want
     */
    @Test
    void playerPlaysYOPHappy() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertTrue(gameService.playCard("game001", 1, ProgressCardType.YEAR_OF_PLENTY));
        assertTrue(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.YOP));

        assertTrue(gameService.yearOfPlenty("game001", 1, Resource.BRICK, Resource.WOOL));
        assertFalse(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.YOP));
        assertEquals(1, gameService.getPlayer("game001", 1).getResources().get(Resource.BRICK));
        assertEquals(1, gameService.getPlayer("game001", 1).getResources().get(Resource.WOOL));
    }

    /**
     * player has a YOP card, but doesn't have the PLAY_CARD token. They can't select resources.
     */
    @Test
    void playerPlaysYOPButHasNoToken() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.YEAR_OF_PLENTY));
        assertFalse(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.YOP));
        assertFalse(gameService.yearOfPlenty("game001", 1, Resource.BRICK, Resource.WOOL));
    }

    /**
     * player has the PLAY_CARD token, but doesn't have a YOP card. They can't select resources.
     */
    @Test
    void playerPlaysYOPButHasNoCard() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.YEAR_OF_PLENTY));
        assertFalse(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.YOP));
        assertFalse(gameService.yearOfPlenty("game001", 1, Resource.BRICK, Resource.WOOL));
    }

    /**
     * player has the PLAY_CARD token, has a MONOPOLY card, plays this card and chooses to steal all bricks
     */
    @Test
    void playerPlaysMonopolyHappy() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.MONOPOLY));
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().ifPresent(player -> {
            player.addResources(Resource.BRICK, 2);
        });
        gameRepository.save(game);

        assertTrue(gameService.playCard("game001", 1, ProgressCardType.MONOPOLY));
        assertTrue(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.MONOPOLY));

        assertTrue(gameService.monopoly("game001", 1, Resource.BRICK));
        assertEquals(2, gameService.getPlayer("game001", 1).getResources().get(Resource.BRICK));
    }

    /**
     * player has a monopoly card, but doesn't have the PLAY_CARD token
     */
    @Test
    void playerPlaysMonopolyButHasNoToken() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getCards().add(new ProgressCard(ProgressCardType.MONOPOLY));
        });
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().ifPresent(player -> {
            player.addResources(Resource.BRICK, 2);
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.MONOPOLY));
        assertFalse(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.MONOPOLY));

        assertFalse(gameService.monopoly("game001", 1, Resource.BRICK));
        assertEquals(0, gameService.getPlayer("game001", 1).getResources().get(Resource.BRICK));
    }

    /**
     * player has the PLAY_CARD token, but doesn't have a monopoly card
     */
    @Test
    void playerPlaysMonopolyButHasNoCard() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(player -> {
            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        });
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 2).findFirst().ifPresent(player -> {
            player.addResources(Resource.BRICK, 2);
        });
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.MONOPOLY));
        assertFalse(gameService.getPlayer("game001", 1).getRemainingActions().contains(PlayerAction.MONOPOLY));

        assertFalse(gameService.monopoly("game001", 1, Resource.BRICK));
        assertEquals(0, gameService.getPlayer("game001", 1).getResources().get(Resource.BRICK));
    }

    /**
     * player has a road building card and builds 2 roads
     */
    @Test
    void playerPlaysRoadBuildingHappy() {
        //set-up
        Game game = gameService.getGame("game001");
        game.setCurrentPlayerId(1);
        Player player = gameService.getPlayer("game001", 1);
        player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        player.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        game.getBoard().getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.TOP), 1));
        gameRepository.save(game);


        assertTrue(gameService.playCard("game001", 1, ProgressCardType.ROAD_BUILDING));
        assertEquals(2, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());

        assertEquals(0, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        gameService.build("game001", 1, new Coordinate(0, 0, 0, CardDir.NORTH_WEST));
        assertEquals(1, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        assertEquals(1, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());

        gameService.build("game001", 1, new Coordinate(0, 0, 0, CardDir.WEST));
        assertEquals(2, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        assertEquals(0, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());
    }

    /**
     * player has a road building card but doesn't have the PLAY_CARD token
     */
    @Test
    void playerPlaysRoadBuildingButHasNoToken() throws Exception {
        //set-up
        Game game = gameService.getGame("game001");
        Player player = gameService.getPlayer("game001", 1);
        player.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        game.getBoard().getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.TOP), 1));
        gameRepository.save(game);

        assertFalse(gameService.playCard("game001", 1, ProgressCardType.ROAD_BUILDING));
        assertEquals(0, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());
    }

    /**
     * player has a road building card, uses it, builds one road then builds a second in the same spot (which fails)
     * the player still has one ROAD_BUILDING token left
     */
    @Test
    void playerPlaysRoadBuildingButBuildsIllegal(){
        //set-up
        Game game = gameService.getGame("game001");
        game.setCurrentPlayerId(1);
        Player player = gameService.getPlayer("game001", 1);
        player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        player.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
        game.getBoard().getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.TOP), 1));
        gameRepository.save(game);

        assertTrue(gameService.playCard("game001", 1, ProgressCardType.ROAD_BUILDING));
        assertEquals(2, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());

        assertEquals(0, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        gameService.build("game001", 1, new Coordinate(0, 0, 0, CardDir.NORTH_WEST));
        assertEquals(1, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        assertEquals(1, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());

        gameService.build("game001", 1, new Coordinate(0, 0, 0, CardDir.NORTH_WEST));
        assertEquals(1, game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == 1).count());
        assertEquals(1, player.getRemainingActions().stream().filter(ra -> ra.equals(PlayerAction.ROAD_BUILDING)).count());
    }

    @Test
    void gameStatesHappy(){
        //set-up
        Game game = gameService.getGame("game001");

        assertEquals(GameState.ACTIVE, game.getGameState());
        gameService.pauseGame("game001");
        assertEquals(GameState.PAUSED, game.getGameState());
        game.getPlayers().get(0).setVictoryPointsAmount(10);
        gameService.endGame(game);
        assertEquals(GameState.FINISHED, game.getGameState());
    }

    @Test
    void pausePausedGame(){
        Game game = gameService.getGame("game001");
        game.setGameState(GameState.PAUSED);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.pauseGame("game001");
        });
    }

    @Test
    void resumeActiveGame(){
        Game game = gameService.getGame("game001");
        assertEquals(GameState.ACTIVE, game.getGameState());

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.resumeGame("game001");
        });
    }



    //IGNORE, THESE ARE THE INTERFACES WE CHANGED TO FACILITATE TESTING

    /**
     * Dummy CreationService that doesn't shuffle the tiles or harbours
     */
    static class DummyBoardCreationService implements IBoardCreationService {
        int fieldAmount = 4;
        int forestAmount = 4;
        int plainsAmount = 4;
        int hillsAmount = 3;
        int mountainAmount = 3;
        int desertAmount = 1;

        int twoAmount = 1;
        int threeAmount = 2;
        int fourAmount = 2;
        int fiveAmount = 2;
        int sixAmount = 2;
        int eightAmount = 2;
        int nineAmount = 2;
        int tenAmount = 2;
        int elevenAmount = 2;
        int twelveAmount = 1;

        int woolHarbourAmount = 1;
        int brickHarbourAmount = 1;
        int grainHarbourAmount = 1;
        int lumberHarbourAmount = 1;
        int oreHarbourAmount = 1;
        int genericHarbourAmount = 4;

        @Override
        public Board generate() {
            Board board = new Board();
            ArrayList<Tile> tilesToAdd = new ArrayList<>();
            ArrayList<Integer> numbersToAssign = new ArrayList<>();
            ArrayList<Harbour> harboursToAdd = new ArrayList<>();
            ArrayList<Coordinate> harbourCoordsToAssign = new ArrayList<>();

            for (int i = 0; i < fieldAmount; i++) {
                Tile field = new Tile();
                field.setTileType(TileType.FIELD);
                tilesToAdd.add(field);
            }

            for (int i = 0; i < forestAmount; i++) {
                Tile forest = new Tile();
                forest.setTileType(TileType.FOREST);
                tilesToAdd.add(forest);
            }

            for (int i = 0; i < plainsAmount; i++) {
                Tile plains = new Tile();
                plains.setTileType(TileType.PLAINS);
                tilesToAdd.add(plains);
            }

            for (int i = 0; i < hillsAmount; i++) {
                Tile hills = new Tile();
                hills.setTileType(TileType.HILLS);
                tilesToAdd.add(hills);
            }

            for (int i = 0; i < mountainAmount; i++) {
                Tile mountains = new Tile();
                mountains.setTileType(TileType.MOUNTAINS);
                tilesToAdd.add(mountains);
            }

            for (int i = 0; i < desertAmount; i++) {
                Tile desert = new Tile();
                desert.setTileType(TileType.DESERT);
                tilesToAdd.add(desert);
            }

            for (int i = 0; i < twoAmount; i++) {
                numbersToAssign.add(2);
            }

            for (int i = 0; i < threeAmount; i++) {
                numbersToAssign.add(3);
            }

            for (int i = 0; i < fourAmount; i++) {
                numbersToAssign.add(4);
            }

            for (int i = 0; i < fiveAmount; i++) {
                numbersToAssign.add(5);
            }

            for (int i = 0; i < sixAmount; i++) {
                numbersToAssign.add(6);
            }

            for (int i = 0; i < eightAmount; i++) {
                numbersToAssign.add(8);
            }

            for (int i = 0; i < nineAmount; i++) {
                numbersToAssign.add(9);
            }

            for (int i = 0; i < tenAmount; i++) {
                numbersToAssign.add(10);
            }

            for (int i = 0; i < elevenAmount; i++) {
                numbersToAssign.add(11);
            }

            for (int i = 0; i < twelveAmount; i++) {
                numbersToAssign.add(12);
            }

            //Shuffle has been disabled, to make sure we always get the same board
//            Collections.shuffle(numbersToAssign);
//            Collections.shuffle(tilesToAdd);

            //top row
            for (int x = 0, y = 3, i = 0; x <= 3; x++, y--, i++) {
                //z coord stays the same in a row
                int z = -3;

                Tile waterTile = new Tile();
                waterTile.setTileType(TileType.WATER);
                waterTile.setNumber(0);
                waterTile.setCoordinate(new Coordinate(x, y, z));
                waterTile.setIndex(i);
                board.getTiles().add(waterTile);
            }

            //board center
            {
                //these numbers will adapt for each tile, moving right
                int x = -1;
                int y = 3;
                int z = -2;
                //these numbers serve to reset the coords to the start of the row
                int xBase = -1;
                int yBase = 3;
                int zBase = -2;

                //index: the index of the tile (unique number)
                //x, y, z: coordinate
                //i: index in the tilesToAdd list
                //number: index in the numbersToAssign list
                for (int index = 4, i = 0, number = 0; index < 33; index++) {
                    //left water tile
                    if (y == 3 && x != -3 || x == -3 && y != 3 || y == 3 && z == 0) {
                        Tile waterTile = new Tile();
                        waterTile.setTileType(TileType.WATER);
                        waterTile.setNumber(0);
                        waterTile.setCoordinate(new Coordinate(x, y, z));
                        waterTile.setIndex(index);
                        board.getTiles().add(waterTile);
                        //move right
                        x++;
                        y--;
                    }
                    //right water tile
                    else if (x == 3 && y != -3 || x != 3 && y == -3 || x == 3 && z == 0) {
                        Tile waterTile = new Tile();
                        waterTile.setTileType(TileType.WATER);
                        waterTile.setNumber(0);
                        waterTile.setCoordinate(new Coordinate(x, y, z));
                        waterTile.setIndex(index);
                        board.getTiles().add(waterTile);
                        //top half of the board
                        if (y != -3) {
                            xBase = xBase - 1;
                            zBase = zBase + 1;

                            x = xBase;
                            y = yBase;
                            z = zBase;
                        }
                        //bottom half of the board
                        else {
                            yBase = yBase - 1;
                            zBase = zBase + 1;

                            x = xBase;
                            y = yBase;
                            z = zBase;
                        }
                    }
                    //resource tile
                    else {
                        Tile tile = tilesToAdd.get(i);
                        if (tile.getTileType() != TileType.DESERT) {
                            tile.setNumber(numbersToAssign.get(number));
                            number++;
                        } else {
                            board.setRobberTile(tile);
                        }
                        tile.setCoordinate(new Coordinate(x, y, z));
                        tile.setIndex(index);
                        board.getTiles().add(tile);
                        i++;
                        //move right
                        x++;
                        y--;
                    }
                }
            }

            //bottom row
            for (int x = -3, y = 0, i = 33; x <= 0; x++, y--, i++) {
                //z coord stays the same in a row
                int z = 3;

                Tile waterTile = new Tile();
                waterTile.setTileType(TileType.WATER);
                waterTile.setNumber(0);
                waterTile.setCoordinate(new Coordinate(x, y, z));
                waterTile.setIndex(i);
                board.getTiles().add(waterTile);
            }

            //HARBOURS

            for (int i = 0; i < woolHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(2);
                harbour.setResource(Resource.WOOL);
                harboursToAdd.add(harbour);
            }
            for (int i = 0; i < brickHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(2);
                harbour.setResource(Resource.BRICK);
                harboursToAdd.add(harbour);
            }
            for (int i = 0; i < lumberHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(2);
                harbour.setResource(Resource.LUMBER);
                harboursToAdd.add(harbour);
            }
            for (int i = 0; i < oreHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(2);
                harbour.setResource(Resource.ORE);
                harboursToAdd.add(harbour);
            }
            for (int i = 0; i < grainHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(2);
                harbour.setResource(Resource.GRAIN);
                harboursToAdd.add(harbour);
            }
            for (int i = 0; i < genericHarbourAmount; i++) {
                Harbour harbour = new Harbour();
                harbour.setRatio(3);
                harbour.setResource(Resource.NOTHING);
                harboursToAdd.add(harbour);
            }

            harbourCoordsToAssign.add(new Coordinate(0, 2, -2, CardDir.NORTH_WEST));
            harbourCoordsToAssign.add(new Coordinate(1, 1, -2, CardDir.NORTH_WEST));
            harbourCoordsToAssign.add(new Coordinate(2, -1, -1, CardDir.NORTH_EAST));
            harbourCoordsToAssign.add(new Coordinate(3, -3, 0, CardDir.WEST));
            harbourCoordsToAssign.add(new Coordinate(1, -3, 2, CardDir.NORTH_WEST));
            harbourCoordsToAssign.add(new Coordinate(-1, -2, 3, CardDir.NORTH_WEST));
            harbourCoordsToAssign.add(new Coordinate(-3, 0, 3, CardDir.NORTH_EAST));
            harbourCoordsToAssign.add(new Coordinate(-2, 1, 1, CardDir.WEST));
            harbourCoordsToAssign.add(new Coordinate(-1, 2, -1, CardDir.WEST));

            //shuffle has been disabled to ensure we always get the same board
//            Collections.shuffle(harbourCoordsToAssign);
//            Collections.shuffle(harboursToAdd);

            for (int i = 0; i < 9; i++) {
                Harbour harbour = harboursToAdd.get(i);
                harbour.setCoordinate(harbourCoordsToAssign.get(i));
                board.getHarbours().add(harbour);
            }

            return board;
        }
    }

    /**
     * used to eliminate the random factor in rolls
     */
    //TODO: replace with Mockbean
    static class DummyDiceService implements IDiceService {
        private int roller;

        public DummyDiceService() {
            this.roller = 0;
        }

        @Override
        public int[] roll() {
            switch (roller) {
                case -1:
                    return new int[]{3, 4};
                case 0:
                    roller++;
                    return new int[]{3, 5};
                case 1:
                    roller++;
                    return new int[]{2, 4};
                case 2:
                    roller++;
                    return new int[]{2, 5};
                default:
                    return new int[0];
            }
        }
    }

    //TODO: vervangen door mock!
    static class DummyGameRepository implements GameRepository {

        public Game game = null;

        @Override
        public <S extends Game> S save(S entity) {
            entity.setId("game001");
            this.game = entity;
            return (S) this.game;
        }

        @Override
        public Optional<Game> findById(String s) {
            if (game == null) {
                return Optional.empty();
            } else {
                return Optional.of(game);
            }
        }

        //UNUSED

        @Override
        public <S extends Game> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public List<Game> findAll() {
            return null;
        }

        @Override
        public List<Game> findAll(Sort sort) {
            return null;
        }

        @Override
        public <S extends Game> S insert(S entity) {
            return null;
        }

        @Override
        public <S extends Game> List<S> insert(Iterable<S> entities) {
            return null;
        }

        @Override
        public <S extends Game> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends Game> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public Page<Game> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public boolean existsById(String s) {
            return false;
        }

        @Override
        public Iterable<Game> findAllById(Iterable<String> strings) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(String s) {

        }

        @Override
        public void delete(Game entity) {

        }

        @Override
        public void deleteAll(Iterable<? extends Game> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends Game> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Game> Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Game> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Game> boolean exists(Example<S> example) {
            return false;
        }
    }
}