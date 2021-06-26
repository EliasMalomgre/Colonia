package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.AiConfig;
import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.ai.SimulationTimeStruct;
import kdg.colonia.gameService.domain.ai.actions.*;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.gameInfo.CostObject;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AIServiceTest {

    @Autowired
    AIService aiService;

    @Autowired
    GameService gameService;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlayerService playerService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    GameInfoService gameInfoService;

    @Autowired
    CardConfig cf;

    @MockBean
    AiConfig aiConfig;

    Game game;

    @BeforeEach
    public void setup() {
        game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0), boardCreationService.generate(), cardPileCreationService.generate(),"1");
        game.setGameState(GameState.ACTIVE);
        game.setCurrentPlayerId(2);
        game.getPlayers().forEach(player -> player.getRemainingActions().clear());
        for (int i = 0; i < 4; i++) {
            game.getPlayers().get(i).setVictoryPointsAmount(i + 7);
        }

        when(aiConfig.getOngoingGame()).thenReturn(-1);
        when(aiConfig.isLessTimeFewActions()).thenReturn(true);
        when(aiConfig.getFewActions()).thenReturn(5);
        when(aiConfig.getFewActionsSimulationTime()).thenReturn(2000L);
        when(aiConfig.getSimulationTime()).thenReturn(2000L);
        when(aiConfig.getInitialRoadSimulationTime()).thenReturn(2000L);
        when(aiConfig.getInitialRoadSimulationTime()).thenReturn(5000L);
        when(aiConfig.isOnlyRandomMoves()).thenReturn(false);
        when(aiConfig.isUseChanceNodes()).thenReturn(true);


    }

    @Test
    public void getCurrentPlayerCorrectly() {
        assertEquals(game.getCurrentPlayerId(), aiService.getCurrentPlayer(game));
    }

    @Test
    public void getCurrentPlayerIncorrectly() {
        assertNotEquals(game.getCurrentPlayerId() + 1, aiService.getCurrentPlayer(game));
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3})
    public void getNextPlayerCorrectly(int currentPlayer) {
        game.setCurrentPlayerId(currentPlayer);
        int nextPlayer = 0;
        if (currentPlayer != 3) {
            nextPlayer = currentPlayer + 1;
        }
        assertEquals(nextPlayer, aiService.getNextPlayer(game));
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3})
    public void getNextPlayerIncorrectly(int currentPlayer) {
        game.setCurrentPlayerId(currentPlayer);
        int nextPlayer = 0;
        if (currentPlayer != 3) {
            nextPlayer = currentPlayer + 1;
        }
        assertNotEquals(nextPlayer + 1, aiService.getNextPlayer(game));
    }

    @Test
    public void getStatusOngoingGame() {
        assertEquals(-1, aiService.getStatus(game, false));
    }

    @Test
    public void getStatusFinishedGame() {
        game.setGameState(GameState.FINISHED);
        assertEquals(4, aiService.getStatus(game, false));
    }

    @Test
    public void getStatusStoppedGame() {
        assertEquals(4, aiService.getStatus(game, true));
    }

    @Test
    public void getStatusFinishedGameIncorrectly() {
        game.setGameState(GameState.FINISHED);
        assertNotEquals(2, aiService.getStatus(game, false));
    }

    @Test
    public void getSimulationTimeOneAction() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.END_TURN);

        SimulationTimeStruct pair = aiService.getSimulationTime(game);

        assertEquals(-1, pair.getTime());
        assertFalse(pair.getGame().getPlayers().get(1).getRemainingActions().contains(PlayerAction.END_TURN));
    }

    @Test
    public void getSimulationTimeChanceDiceRollActions() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.ROLL);

        SimulationTimeStruct pair = aiService.getSimulationTime(game);

        assertEquals(-1, pair.getTime());
        assertFalse(pair.getGame().getPlayers().get(1).getRemainingActions().contains(PlayerAction.ROLL));
    }

    @Test
    public void getSimulationTimeFewActions() {
        game.getBoard().addSettlement(new Coordinate(0,0,0,Direction.TOP) ,2);

        when(aiConfig.isLongerSimulationInitial()).thenReturn(false);

        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.INITIAL1);

        SimulationTimeStruct pair = aiService.getSimulationTime(game);

        assertEquals(aiConfig.getFewActionsSimulationTime(), pair.getTime());
    }

    @Test
    public void getSimulationTimeManyActions() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.INITIAL1);

        SimulationTimeStruct pair = aiService.getSimulationTime(game);

        assertEquals(aiConfig.getSimulationTime(), pair.getTime());
    }

    @Test
    public void getSimulationTimeRandomActions() {
        when(aiConfig.isOnlyRandomMoves()).thenReturn(true);

        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.INITIAL1);

        SimulationTimeStruct pair = aiService.getSimulationTime(game);

        assertEquals(-1, pair.getTime());
        assertTrue(pair.getGame().getPlayers().get(1).getRemainingActions().contains(PlayerAction.INITIAL1));

    }


    @Test()
    public void noOneHasActions() {
        assertThrows(IllegalArgumentException.class, () -> aiService.getLegalActions(game));
    }

    @Test
    public void currentPlayerHasNoActions() {
        game.getPlayers().get(0).getRemainingActions().add(PlayerAction.BUILD);
        aiService.getLegalActions(game);

        assertEquals(1, game.getCurrentPlayerId());
    }

    @Test
    public void currentPlayerHasNoActionsOtherPlayersMustDiscard() {
        game.getPlayers().get(0).getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);
        aiService.getLegalActions(game);

        assertEquals(2, game.getCurrentPlayerId());
    }

    @Test
    public void currentPlayerHasActionsOtherPlayersMustDiscard() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.BUILD);
        game.getPlayers().get(0).getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        game.getPlayers().get(0).addResources(2,2,2,2,2);

        when(aiConfig.isRandomDiscards()).thenReturn(true);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(action -> action.getClass() == DiscardAction.class)
                .collect(Collectors.toList());

        aiService.getLegalActions(game);

        assertTrue(0 < actions.size());
        assertEquals(2, game.getCurrentPlayerId());
    }

    @Test
    public void currentPlayerHasActionsOtherPlayersDont() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.INITIAL1);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(action -> action.getClass() == BuildRoadAction.class
                || action.getClass() == BuildSettlementAction.class).collect(Collectors.toList());

        assertTrue(0 < actions.size());
        assertEquals(2, game.getCurrentPlayerId());
    }

    @Test
    public void LegalSettlementActionsInitialPhase() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.INITIAL1);

        List<Action> actions = aiService.getLegalActions(game);
        //Selects all distinct action classes
        List<Class> actionClasses = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());

        //Tests the list for the correct size, no duplicate actions and if the actions are the correct class
        assertEquals(54, actions.size());
        assertEquals(54, new HashSet<>(actions).size());
        assertEquals(Stream.of(BuildSettlementAction.class).collect(Collectors.toList()), actionClasses);
    }

    @Test
    public void LegalSettlementActionsNotInitialPhaseEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().getRoads().add(new Road(new Coordinate(0,0,0, CardDir.WEST), game.getCurrentPlayerId()));

        //Gives the player the correct resources to build a settlement
        CostObject settlement = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Settlement")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(settlement.getBrickCost(), settlement.getGrainCost(), settlement.getLumberCost(), settlement.getOreCost(),
                        settlement.getWoolCost());

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildSettlementAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(2, actions.size());
        assertEquals(2, new HashSet<>(actions).size());
    }

    @Test
    public void LegalSettlementActionsNotInitialPhaseNotEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().getRoads().add(new Road(new Coordinate(0,0,0, CardDir.WEST), game.getCurrentPlayerId()));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildSettlementAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(0, actions.size());
        assertEquals(0, new HashSet<>(actions).size());
    }

    @Test
    public void LegalRoadsActionsInitialPhase() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.INITIAL1);
        game.getBoard().addSettlement(new Coordinate (0,0,0, Direction.TOP), aiService.getCurrentPlayer(game));

        List<Action> actions = aiService.getLegalActions(game);
        //Selects all distinct action classes
        List<Class> actionClasses = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());

        //Tests the list for the correct size, no duplicate actions and if the actions are the correct class
        assertEquals(3, actions.size());
        assertEquals(3, new HashSet<>(actions).size());
        assertEquals(Stream.of(BuildRoadAction.class).collect(Collectors.toList()), actionClasses);
    }

    @Test
    public void LegalRoadsActionsNotInitialPhaseEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().addSettlement(new Coordinate (0,0,0, Direction.TOP), aiService.getCurrentPlayer(game));
        game.getBoard().getRoads().add(new Road(new Coordinate(0,0,0, CardDir.NORTH_WEST), game.getCurrentPlayerId()));

        //Gives the player the correct resources to build a road
        CostObject road = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Road")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(road.getBrickCost(), road.getGrainCost(), road.getLumberCost(), road.getOreCost(),
                        road.getWoolCost());

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildRoadAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(4, actions.size());
        assertEquals(4, new HashSet<>(actions).size());
    }

    @Test
    public void LegalRoadsActionsNotInitialPhaseNotEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().addSettlement(new Coordinate (0,0,0, Direction.TOP), aiService.getCurrentPlayer(game));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildRoadAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(0, actions.size());
        assertEquals(0, new HashSet<>(actions).size());
    }

    @Test
    public void LegalCityActionsEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().addSettlement(new Coordinate (0,0,0, Direction.TOP), aiService.getCurrentPlayer(game));
        game.getBoard().addSettlement(new Coordinate (-1,1,0, Direction.TOP), aiService.getCurrentPlayer(game));

        //Gives the player the correct resources to build a city
        CostObject city = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("City")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(city.getBrickCost(), city.getGrainCost(), city.getLumberCost(), city.getOreCost(),
                        city.getWoolCost());

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildCityAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(2, actions.size());
        assertEquals(2, new HashSet<>(actions).size());
    }

    @Test
    public void LegalCityActionsNotEnoughResources() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().addSettlement(new Coordinate (0,0,0, Direction.TOP), aiService.getCurrentPlayer(game));
        game.getBoard().addSettlement(new Coordinate (-1,1,0, Direction.TOP), aiService.getCurrentPlayer(game));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildCityAction.class).collect(Collectors.toList());

        //Tests the list for the correct size and no duplicate actions
        assertEquals(0, actions.size());
        assertEquals(0, new HashSet<>(actions).size());
    }

    @Test
    public void AllLegalActionForBuildToken() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId()==1).findFirst().get().getRemainingActions().add(PlayerAction.BUILD);
        game.getBoard().getSettlements().add(new Settlement(new Coordinate (0,0,0, Direction.LEFT), aiService.getCurrentPlayer(game)));
        game.getBoard().getSettlements().add(new Settlement(new Coordinate (-1,1,0, Direction.TOP), aiService.getCurrentPlayer(game)));

        game.getBoard().getRoads().add(new Road(new Coordinate(0,0,0, CardDir.NORTH_WEST), game.getCurrentPlayerId()));
        game.getBoard().getRoads().add(new Road(new Coordinate(0,0,0, CardDir.NORTH_EAST), game.getCurrentPlayerId()));
        game.getBoard().getRoads().add(new Road(new Coordinate(-1,1,0, CardDir.NORTH_WEST), game.getCurrentPlayerId()));

        //Gives the player the correct resources to build a city
        CostObject city = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("City")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(city.getBrickCost(), city.getGrainCost(), city.getLumberCost(), city.getOreCost(),
                        city.getWoolCost());

        //Gives the player the correct resources to build a road
        CostObject road = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Road")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(road.getBrickCost(), road.getGrainCost(), road.getLumberCost(), road.getOreCost(),
                        road.getWoolCost());

        //Gives the player the correct resources to build a settlement
        CostObject settlement = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Settlement")).findFirst().get();
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(settlement.getBrickCost(), settlement.getGrainCost(), settlement.getLumberCost(), settlement.getOreCost(),
                        settlement.getWoolCost());

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()==BuildSettlementAction.class
                || a.getClass()==BuildCityAction.class || a.getClass()==BuildRoadAction.class).collect(Collectors.toList());

        //Tests the list of actions for the correct size, for no duplicate actions and for the correct amount of actions of each class
        assertEquals(11, actions.size());
        assertEquals(11, new HashSet<>(actions).size());
        //Check right amount of each action
        assertEquals(1, actions.stream().filter(a -> a.getClass()==BuildSettlementAction.class).count());
        assertEquals(2, actions.stream().filter(a -> a.getClass()==BuildCityAction.class).count());
        assertEquals(8, actions.stream().filter(a -> a.getClass()==BuildRoadAction.class).count());
    }

    @Test
    public void legalActionsTradeBank() {
        //Gives player resources to trade
        game.getPlayers().stream().filter(player -> player.getPlayerId()==2).findFirst().get()
                .addResources(4,4,0,0,0);

        game.getPlayers().stream().filter(player -> player.getPlayerId()==2).findFirst().get()
                .getRemainingActions().add(PlayerAction.BUILD);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass()== TradeBankAction.class).collect(Collectors.toList());

        //Check for right amount and for distinct trades
        assertEquals(8, actions.size());
        assertEquals(8, new HashSet<>(actions).size());
        //Check for the correct trade resources
        assertEquals(4, actions.stream().filter(action -> ((TradeBankAction) action).getFrom()==Resource.BRICK).count());
        assertEquals(4, actions.stream().filter(action -> ((TradeBankAction) action).getFrom()==Resource.GRAIN).count());
        //Check for no trades for the same resource
        assertTrue(actions.stream().noneMatch(action -> ((TradeBankAction) action).getFrom()==((TradeBankAction) action).getTo()));
        //Check no trade with Resource.NOTHING
        assertTrue(actions.stream().noneMatch(action -> ((TradeBankAction) action).getFrom()==Resource.NOTHING));
        assertTrue(actions.stream().noneMatch(action -> ((TradeBankAction) action).getTo()==Resource.NOTHING));
    }

    @Test
    public void legalStealActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().add(PlayerAction.STEAL);

        //Configure Board
        game.getBoard().addSettlement(new Coordinate(0, 0, 0, Direction.TOP), 2);
        game.getBoard().addSettlement(new Coordinate(0, -1, 1, Direction.LEFT), 3);
        game.getBoard().getRobberTile().setCoordinate(new Coordinate(0, 0, 0));

        when(aiConfig.isUseChanceNodes()).thenReturn(false);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == StealAction.class).collect(Collectors.toList());

        //Check for right amount and for distinct steal combinations
        assertEquals(2, actions.size());
        assertEquals(2, new HashSet<>(actions).size());
        //Check if current player is always the one that steals
        assertTrue(actions.stream().allMatch(action -> ((StealAction) action).getPlayerId() == 1));
    }

    @ParameterizedTest
    @CsvSource({"1,0,0,0,0, 0,0,0,0,0, 1","2,0,0,0,0, 0,0,0,0,0, 1", "1,0,0,0,0, 0,0,0,1,0, 2","2,0,0,0,0, 0,1,0,1,0, 3",
            "1,0,1,0,0, 3,0,0,4,0, 4","2,1,1,3,1, 5,1,5,4,0, 9", "1,8,4,1,4, 2,3,4,1,1, 10","2,0,2,0,0, 0,1,0,1,0, 4"})
    public void legalChanceStealActions(int p2b, int p2g, int p2l, int p2o, int p2w, int p3b, int p3g, int p3l, int p3o,
                                        int p3w, int actionCount) {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().get().getRemainingActions().add(PlayerAction.STEAL);

        game.getPlayers().get(1).addResources(p2b, p2g, p2l, p2o, p2w);
        game.getPlayers().get(2).addResources(p3b, p3g, p3l, p3o, p3w);

        //Configure Board
        game.getBoard().addSettlement(new Coordinate(0, 0, 0, Direction.TOP), 2);
        game.getBoard().addSettlement(new Coordinate(0, -1, 1, Direction.LEFT), 3);
        game.getBoard().getRobberTile().setCoordinate(new Coordinate(0, 0, 0));

        when(aiConfig.isUseChanceNodes()).thenReturn(true);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == ChanceStealAction.class).collect(Collectors.toList());

        assertEquals(actionCount, actions.size());
        assertEquals(actionCount, new HashSet<>(actions).size());
        assertTrue(1.0000001 >actions.stream().map(action -> ((ChanceStealAction)action).getProbability()).
                reduce(Double::sum).orElse(0.0));
        assertTrue(0.9999999 < actions.stream().map(action -> ((ChanceStealAction)action).getProbability()).
                reduce(Double::sum).orElse(0.0));

    }

    @ParameterizedTest
    //Checks some of the combination of resources a player can have
    @CsvSource({"1,1,0,0,0,2", "2,0,0,0,0,1", "1,1,1,0,0,3", "1,0,2,0,0,2", "1,1,1,1,0,12", "1,1,1,1,1,20", "2,2,2,0,0,24", "2,2,2,2,0,204", "2,2,2,2,2,2220"})
    public void allDiscards(int bricks, int grain, int lumber, int ore, int wool, int combinations) {
        game.getPlayers().get(0).getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        when(aiConfig.isRandomDiscards()).thenReturn(false);

        //Give the resources to the player
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(bricks, grain, lumber, ore, wool);

        List<Action> actions = aiService.getLegalActions(game);

        //Check for right amount and for distinct discard combinations
        assertEquals(combinations, actions.size());
        assertEquals(combinations, actions.stream().distinct().count());
    }

    @Test
    public void randomDiscard() {
        game.getPlayers().get(0).getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);

        when(aiConfig.isRandomDiscards()).thenReturn(true);

        //Give the resources to the player
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).findFirst().get()
                .addResources(5, 5, 1, 0, 1);

        List<Action> actions = aiService.getLegalActions(game);

        //Check for right amount and for distinct discard combinations
        assertEquals(1, actions.size());
        assertEquals(1, actions.stream().distinct().count());
    }

    @Test
    public void legalRobberMoves() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.MOVE_ROBBER);
        game.getBoard().setRobberTile(game.getBoard().getTileForCoordinate(new Coordinate(0,0,0)));

        List<Action> actions = aiService.getLegalActions(game);

        assertEquals(18, actions.size());
        assertEquals(18, actions.stream().distinct().count());
    }

    @Test
    public void legalUseKnightActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                        player -> {
                            player.getRemainingActions().add(PlayerAction.PLAY_CARD);
                            player.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
                        });

        //Configure Board
        game.getBoard().setRobberTile(game.getBoard().getTileForCoordinate(new Coordinate(0,0,0)));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == UseDCKnightAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(18, actions.size(), "There are 18 or (19-1) possible tiles, minus the current robbertile");
    }

    @Test
    public void legalUseRoadBuildingActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                player -> {
                    player.getRemainingActions().add(PlayerAction.PLAY_CARD);
                    player.getCards().add(new ProgressCard(ProgressCardType.ROAD_BUILDING));
                });

        game.getBoard().addSettlement(new Coordinate(0, 0, 0, Direction.TOP), 1);
        game.getBoard().addSettlement(new Coordinate(0, -1, 1, Direction.LEFT), 1);

        game.getBoard().addRoad(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 1);
        game.getBoard().addRoad(new Coordinate(0, 0, 0, CardDir.WEST), 1);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == UseDCRoadBuildingAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(5, actions.size(), "There are 5 different resources the player could use monopoly on");
    }

    @Test
    public void legalUseYearOfPlentyActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                player -> {
                    player.getRemainingActions().add(PlayerAction.PLAY_CARD);
                    player.getCards().add(new ProgressCard(ProgressCardType.YEAR_OF_PLENTY));
                });

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == UseDCYearOfPlentyAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(25, actions.size(), "There are 5 different resources the player could use monopoly on");
    }

    @Test
    public void legalUseMonopolyActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                player -> {
                    player.getRemainingActions().add(PlayerAction.PLAY_CARD);
                    player.getCards().add(new ProgressCard(ProgressCardType.MONOPOLY));
                });

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == UseDCMonopolyAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(5, actions.size(), "There are 5 different resources the player could use monopoly on");
    }

    @Test
    public void legalUseVictoryPointActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                player -> {
                    player.getRemainingActions().add(PlayerAction.PLAY_CARD);
                    player.getCards().add(new ProgressCard(ProgressCardType.VICTORY_POINT));
                });

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == UseDCVictoryPointAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(1, actions.size(), "There are 5 different resources the player could use monopoly on");
    }

    @Test
    public void legalBuyDevelopmentCardActions() {
        game.setCurrentPlayerId(1);
        game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == 1)
                .findFirst().ifPresent(
                player -> {
                    player.getRemainingActions().add(PlayerAction.BUY);
                    player.addResources(cf.getBrickCost(), cf.getGrainCost(), cf.getLumberCost(), cf.getOreCost(), cf.getWoolCost());
                });

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == BuyDevelopmentAction.class).collect(Collectors.toList());

        //Check for correct amount of actions
        assertEquals(1, actions.size(), "There are 5 different resources the player could use monopoly on");
    }

    @Test
    public void chanceDiceRolls() {

        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.ROLL);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == ChanceRollDiceAction.class).collect(Collectors.toList());

        assertEquals(11, actions.size());
        assertEquals(11, actions.stream().distinct().count());

        double totalProbability = actions.stream().map(action -> ((ChanceAction)action).getProbability()).reduce(Double::sum).orElse(0.0);

        assertTrue(totalProbability < 1.00000001);
        assertTrue(totalProbability > 0.99999999);

    }

    @Test
    public void normalDiceRolls() {
        when(aiConfig.isUseChanceNodes()).thenReturn(false);
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.ROLL);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == RollDiceAction.class).collect(Collectors.toList());

        assertEquals(1, actions.size());
        assertEquals(1, actions.stream().distinct().count());
    }

    @Test
    public void endTurnAction() {
        game.getPlayers().get(1).getRemainingActions().add(PlayerAction.END_TURN);

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == EndTurnAction.class).collect(Collectors.toList());

        assertEquals(1, actions.size());
        assertEquals(1, actions.stream().distinct().count());
    }

    @Test
    public void incomingIllegalTradeAction() {

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

        game.setTradeRequest(new TradeRequest(2,1, resourcesToSend, resourcesToReceive));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == TradeDeclineAction.class
            || a.getClass() == TradeAcceptAction.class).collect(Collectors.toList());

        assertEquals(1, actions.stream().filter(a -> a.getClass()== TradeDeclineAction.class).count());
        assertEquals(0, actions.stream().filter(a -> a.getClass()== TradeAcceptAction.class).count());
        assertEquals(1, ((TradeDeclineAction)actions.stream().filter(a -> a.getClass()== TradeDeclineAction.class).findFirst().get()).getPlayerId());

    }

    @Test
    public void incomingTradeAction() {

        Map<Resource, Integer> resourcesToSend = new HashMap<>();
        resourcesToSend.put(Resource.BRICK, 1);
        resourcesToSend.put(Resource.GRAIN, 0);
        resourcesToSend.put(Resource.WOOL, 0);
        resourcesToSend.put(Resource.LUMBER, 0);
        resourcesToSend.put(Resource.ORE, 0);

        game.getPlayers().get(1).addResources(resourcesToSend);

        Map<Resource, Integer> resourcesToReceive = new HashMap<>();
        resourcesToReceive.put(Resource.BRICK, 0);
        resourcesToReceive.put(Resource.GRAIN, 2);
        resourcesToReceive.put(Resource.WOOL, 0);
        resourcesToReceive.put(Resource.LUMBER, 0);
        resourcesToReceive.put(Resource.ORE, 0);

        game.getPlayers().get(0).addResources(resourcesToReceive);

        game.setTradeRequest(new TradeRequest(2,1, resourcesToSend, resourcesToReceive));

        List<Action> actions = aiService.getLegalActions(game).stream().filter(a -> a.getClass() == TradeDeclineAction.class
                || a.getClass() == TradeAcceptAction.class).collect(Collectors.toList());

        assertEquals(1, actions.stream().filter(a -> a.getClass()== TradeDeclineAction.class).count());
        assertEquals(1, actions.stream().filter(a -> a.getClass()== TradeAcceptAction.class).count());
        assertEquals(1, ((TradeDeclineAction)actions.stream().filter(a -> a.getClass()== TradeDeclineAction.class).findFirst().get()).getPlayerId());
        assertEquals(1, ((TradeAcceptAction)actions.stream().filter(a -> a.getClass()== TradeAcceptAction.class).findFirst().get()).getPlayerId());

    }
}