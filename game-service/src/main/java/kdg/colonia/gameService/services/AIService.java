package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.AiConfig;
import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.ai.Node;
import kdg.colonia.gameService.domain.ai.SimulationTimeStruct;
import kdg.colonia.gameService.domain.ai.State;

import kdg.colonia.gameService.domain.ai.actions.*;
import kdg.colonia.gameService.domain.boardCalculations.AddRoadCalculator;
import kdg.colonia.gameService.domain.boardCalculations.AddSettlementCalculator;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.domain.tiles.TileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class AIService {

    private final AiConfig aiConfig;

    private final PlayerService playerService;
    private final GameLogicService gameLogicService;
    private final TradeService tradeService;
    private final BoardService boardService;
    private final TurnTokenService turnTokenService;
    private final GameConfig gameConfig;

    /**
     * This method is called to determine the ideal simulation time. When there is only 1 possible action the game will
     * executes this action and return the next game state. When the move has been skipped, a negative number
     * will be returned
     *
     * @param game  the game that has to be simulated
     * @return a pair of a time and a game state
     */
    public SimulationTimeStruct getSimulationTime(Game game) {
        List<Action> actions = getLegalActions(game);

        int currentPlayerAtStartSimulation = game.getCurrentPlayerId();

        //Get all the types of actions
        List<Class<? extends Action>> actionTypes = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());

        //If there only have to be played random moves
        if (aiConfig.isOnlyRandomMoves()) {
            //If there are chance roll dice actions convert them to normal dice roll actions
            if (actionTypes.contains(ChanceRollDiceAction.class)) {
                actions = actions.stream().filter(action -> action.getClass() != ChanceRollDiceAction.class).collect(Collectors.toList());
                actions.addAll(getNormalDiceRollActions(getCurrentPlayer(game)));
            }
            //If there are chance steal actions convert them to normal actions actions
            if (actionTypes.contains(ChanceStealAction.class)) {
                actions = actions.stream().filter(action -> action.getClass() != ChanceStealAction.class).collect(Collectors.toList());
                actions.addAll(getNormalStealActions(game.getBoard(), game.getCurrentPlayerId()));
            }

            //Select a random action and execute it
            Action randomAction = chooseRandomAction(actions);
            log.info("Game[{}]: AI[{}] did random move, ran {}", game.getId(), currentPlayerAtStartSimulation, randomAction);
            game = getNextState(game, randomAction);
            return new SimulationTimeStruct(-1L, game, randomAction);
        }

        if (aiConfig.isLongerSimulationInitial() && game.getBoard().isInitialPhase(currentPlayerAtStartSimulation) && actions.size() > 3) {
            return new SimulationTimeStruct(aiConfig.getInitialSettlementSimulationTime(), game, null);
        }

        if (aiConfig.isLongerSimulationInitial() && game.getBoard().isInitialPhase(currentPlayerAtStartSimulation) && actions.size() <= 3) {
            return new SimulationTimeStruct(aiConfig.getInitialRoadSimulationTime(), game, null);
        }

        //If the only type of action is a ChanceRollDiceAction convert it to a normal dice roll action and execute it
        if (actionTypes.size()==1 && actionTypes.get(0) == ChanceRollDiceAction.class) {
            Action action = getNormalDiceRollActions(getCurrentPlayer(game)).get(0);
            game = getNextState(game, action);
            log.info("Game[{}]: AI[{}] skipped simulation, ran {}", game.getId(), getCurrentPlayer(game), action);
            return new SimulationTimeStruct(-1L, game, action);
        }

        //If the only type of action is a ChanceStealAction convert it to a normal steal action and execute it
        if (actionTypes.size()==1 && actionTypes.get(0) == ChanceStealAction.class) {
            Action action = getNormalStealActions(game.getBoard(), game.getCurrentPlayerId()).get(0);
            game = getNextState(game, action);
            log.info("Game[{}]: AI[{}] skipped simulation, ran {}", game.getId(), currentPlayerAtStartSimulation, action);
            return new SimulationTimeStruct(-1L, game, action);
        }

        //If there is only 1 action execute it
        if (actions.size() == 1) {
            Action action = actions.get(0);
            game = getNextState(game, actions.get(0));
            log.info("Game[{}]: AI[{}] skipped simulation, ran {}", game.getId(), currentPlayerAtStartSimulation, actions.get(0));
            return new SimulationTimeStruct(-1L, game, action);
        }

        //If the number of actions is fewer than the setting return the reduced simulation time
        if (aiConfig.isLessTimeFewActions() && actions.size() <= aiConfig.getFewActions()) {
            return new SimulationTimeStruct(aiConfig.getFewActionsSimulationTime(), game, null);
        }

        //else return the default simulation time
        return new SimulationTimeStruct(aiConfig.getSimulationTime(), game, null);
    }

    /**
     * Calculate all the possible state you can reach from a given game state
     *
     * @param game the game state from which you want to start
     * @return all the possible states
     */
    public List<State> getAllStates(Game game) {
        int playerId = getCurrentPlayer(game);
        List<Action> actions = getLegalActions(game);

        return actions.parallelStream().map(action -> new State(getNextState(game, action), playerId, action)).collect(Collectors.toList());
    }

    /**
     * get the next game state after performing an action to a game state
     *
     * @param game the game state on which you want to perform an action
     * @param action the action you want to perform on the game state
     * @return the next game state after performing the action
     */
    public Game getNextState(Game game, Action action) {

        Game tempGame = new Game(game);
        return new Game(action.performAction(tempGame));
    }

    /**
     * Get the current player of a game state
     *
     * @param game the game state from whom you want to get the current player
     * @return the current player
     */
    public int getCurrentPlayer(Game game) {
        return game.getCurrentPlayerId();
    }

    /**
     * Get the next player to act after a game state
     *
     * @param game the game state from whom you want to get the next player
     * @return the next player to act
     */
    public int getNextPlayer(Game game) {
        int currentPlayer = getCurrentPlayer(game);
        int playerCount = game.getPlayers().size();
        if (currentPlayer == playerCount - 1) {
            return 0;
        }
        return currentPlayer + 1;
    }

    /**
     * Perform a random action on a game state
     *
     * @param game the start game state
     * @return the next game state after performing the action
     */
    public Game randomAction(Game game) {
        List<Action> actions = getLegalActions(game);
        if (actions.isEmpty()){
            return game;
        }

        return getNextState(game, chooseRandomAction(actions));
    }

    /**
     * This method selects a random action out of list of actions
     *
     * @param actions   the actions from whom a random action has to be picked
     * @return a random action
     */
    public Action chooseRandomAction(List<Action> actions) {
        Random random = new Random();
        //Get all the classes from all the action types
        List<Class<? extends Action>> actionTypes = actions.stream().map(Action::getClass).distinct().collect(Collectors.toList());
        //Choose a random class
        Class<? extends Action> actionType = actionTypes.get(random.nextInt(actionTypes.size()));
        //Filter actions on this class
        actions = actions.stream().filter(action -> action.getClass()==actionType).collect(Collectors.toList());

        int randomAction = random.nextInt(actions.size());

        return actions.get(randomAction);
    }

    /**
     * Get the status of a game state
     * aiConfig.getOngoingGame() = ongoing game
     * 1-4 = the player who won the game
     *
     * @param game the game state you want to check
     * @return the status of the game
     */
    public int getStatus(Game game, boolean stoppedSimulation) {
        if (!game.getGameState().equals(GameState.FINISHED)&&!stoppedSimulation){
            return aiConfig.getOngoingGame();
        }

        return game.getPlayers().stream().max(Comparator.comparing(Player::getVictoryPointsAmount)).map(Player::getPlayerId)
                .orElseThrow(() -> {
                    String error = String.format("Game[%s]: game has no players",game.getId());
                    log.error(error);
                    return new IllegalArgumentException(error);
                });
    }

    /**
     * Calculates all the possible action for a player from a given game state
     *
     * @param game the game state from which you want to start
     * @return a list of all the possible actions
     */
    public List<Action> getLegalActions(Game game) {
        Board board = game.getBoard();
        Player currentPlayer = game.getPlayers().stream()
                .filter(player -> player.getPlayerId() == getCurrentPlayer(game)).findFirst().orElseThrow();

        if (game.getTradeRequest()!=null) {
            return getIncomingTradeRequestActions(game);
        }

        List<Action> legalActions = new ArrayList<>();


        //Check if the current player must discard resources
        if (currentPlayer.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)) {
            return getDiscardActions(currentPlayer);
        }

        //Check if other player has to discard resource cards
        Optional<Player> optPlayer = game.getPlayers().stream()
                .filter(player -> player.getPlayerId() != game.getCurrentPlayerId())
                .filter(player -> player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))
                .findFirst();

        if (optPlayer.isPresent()) {
            //Get all discard actions for the player
            return getDiscardActions(optPlayer.get());
        }

        //Check if the current player has PlayerActions
        if(currentPlayer.getRemainingActions().isEmpty()) {
            //Look for players with action and set him as the current player. If not found something has terribly gone wrong
            currentPlayer = game.getPlayers().stream().filter(player -> !player.getRemainingActions().isEmpty())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException(String.format(
                            "Game[%s]: SIMULATION FAILED no player has an action", game.getId())));

            //FAILSAFE, so that worst case we continue for next player
            game.setCurrentPlayerId(currentPlayer.getPlayerId());
        }

        //Check if the player is in the starting phase
        if (currentPlayer.getRemainingActions().contains(PlayerAction.INITIAL1)|| currentPlayer.getRemainingActions().contains(PlayerAction.INITIAL2)){
            Player finalCurrentPlayer = currentPlayer;

            if (board.getSettlements().stream().filter(s -> s.getPlayerId()== finalCurrentPlayer.getPlayerId()).count() ==
                    board.getRoads().stream().filter(r -> r.getPlayerId()==finalCurrentPlayer.getPlayerId()).count()) {

                //Get all the possible actions for building a settlement in the starting phase
                legalActions.addAll(getBuildSettlementActions(board, currentPlayer.getPlayerId(), true));
            }
            else {
                //Get all the possible actions for building a road in the starting phase
                legalActions.addAll(getBuildRoadActions(board, currentPlayer.getPlayerId(), true));
            }
            return legalActions;
        }

        //Checks if player is allowed to move the robber
        if (currentPlayer.getRemainingActions().contains(PlayerAction.MOVE_ROBBER)) {
            legalActions.addAll(getMoveRobberActions(board, currentPlayer.getPlayerId()));
            return legalActions;
        }

        //Check if the player must steal
        if (currentPlayer.getRemainingActions().contains(PlayerAction.STEAL)) {
            legalActions.addAll(getStealActions(game, currentPlayer.getPlayerId()));
            return legalActions;
        }

        //Checks if the player must build a road for his ROAD BUILDING development card
        if (currentPlayer.getRemainingActions().contains(PlayerAction.BUILD)
                && currentPlayer.getRemainingActions().contains(PlayerAction.ROAD_BUILDING)) {

            return getBuildRoadActions(board, currentPlayer.getPlayerId(), false);
        }

        //Check if the player is allowed to roll the dice
        if (currentPlayer.getRemainingActions().contains(PlayerAction.ROLL)) {
            legalActions.addAll(getDiceRollActions(currentPlayer.getPlayerId()));
        }

        if (!currentPlayer.getCards().isEmpty() && currentPlayer.getRemainingActions().contains(PlayerAction.PLAY_CARD)){

            //Forces the player to run victory point card
            if(currentPlayer.getCards().stream().anyMatch(pc -> pc.getCardType().equals(ProgressCardType.VICTORY_POINT))){
                legalActions.add(getVictoryPointAction(currentPlayer.getPlayerId()));
                return legalActions;
            }

            //Grants the player all knight actions
            if(currentPlayer.getCards().stream().anyMatch(pc -> pc.getCardType().equals(ProgressCardType.KNIGHT))){
                legalActions.addAll(getKnightCardActions(board, currentPlayer.getPlayerId()));
            }

            //Grants the player all road building actions
            if(currentPlayer.getCards().stream().anyMatch(pc -> pc.getCardType().equals(ProgressCardType.ROAD_BUILDING))){
                legalActions.addAll(getRoadBuildingCardActions(board, currentPlayer.getPlayerId()));
            }

            //Grants the player all yop actions
            if(currentPlayer.getCards().stream().anyMatch(pc -> pc.getCardType().equals(ProgressCardType.YEAR_OF_PLENTY))){
                legalActions.addAll(getYearOfPlentyCardActions(currentPlayer.getPlayerId()));
            }

            //Grants the player all monopoly actions
            if(currentPlayer.getCards().stream().anyMatch(pc -> pc.getCardType().equals(ProgressCardType.MONOPOLY))){
                legalActions.addAll(getMonopolyCardActions(currentPlayer.getPlayerId()));
            }
        }

        //adds action to purchase a development card if player has the requisite funds
        if (currentPlayer.getRemainingActions().contains(PlayerAction.BUY) && !game.getCardPile().isEmpty() &&
                playerService.playerAllowedToBuyCard(currentPlayer)){
            legalActions.add(getBuyDevelopmentCardAction(currentPlayer.getPlayerId()));
        }

        //Check if player is allowed to build settlements
        if (currentPlayer.getRemainingActions().contains(PlayerAction.BUILD)) {

            if (playerService.playerAllowedToBuildSettlement(currentPlayer)) {
                //Get all the possible actions for building a settlement
                legalActions.addAll(getBuildSettlementActions(board, currentPlayer.getPlayerId(), false));
            }

            if (playerService.playerAllowedToBuildRoad(currentPlayer)) {
                //Get all the possible actions for building a road
                legalActions.addAll(getBuildRoadActions(board, currentPlayer.getPlayerId(), false));
            }

            if (playerService.playerAllowedToBuildCity(currentPlayer)) {
                //Get all the possible actions for building a city
                legalActions.addAll(getBuildCityActions(board, currentPlayer.getPlayerId()));
            }
        }

        //allows player to end turn when all required actions have been completed
        if (currentPlayer.getRemainingActions().contains(PlayerAction.END_TURN)) {
            legalActions.add(new EndTurnAction(currentPlayer.getPlayerId(), gameLogicService, turnTokenService, gameConfig));
        }

        legalActions.addAll(getBankTradeActions(game, currentPlayer));

        return legalActions;
    }

    /**
     * This method is called to get the possible dice roll. Depending on the setting to use only random moves,
     * it will decide which type of action it will request
     *
     * @param currentPlayer the playerId of the current player
     * @return the correct type of dice roll action
     */
    private List<Action> getDiceRollActions(int currentPlayer) {
        if (aiConfig.isUseChanceNodes()) {
            return getChanceDiceRollActions(currentPlayer);
        }

        return getNormalDiceRollActions(currentPlayer);
    }

    /**
     * This method is called upon to get the possible normal dice roll actions
     *
     * @param currentPlayer the playerId of the current player
     * @return the possible normal dice roll actions
     */
    private List<Action> getNormalDiceRollActions(int currentPlayer) {
        return Stream.of(new RollDiceAction(currentPlayer, gameLogicService)).collect(Collectors.toList());
    }

    /**
     * This method is called upon to get the possible chance dice roll actions
     *
     * @param currentPlayer the playerId of the current player
     * @return the possible normal chance dice roll actions
     */
    private List<Action> getChanceDiceRollActions (int currentPlayer) {
        List<Action> actions = new ArrayList<>();

        int probability = 1;
        for (int i = 2; i < 13; i++) {
            actions.add(new ChanceRollDiceAction(i, currentPlayer, (double) probability/36, gameLogicService));
            if (i < 7) {
                probability++;
            }
            else {
                probability--;
            }
        }
        return actions;
    }

    private List<Action> getIncomingTradeRequestActions(Game game) {
        List<Action> actions = new ArrayList<>();

        //Get the players involved with the trade request
        Player receivingPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == game.getTradeRequest().getReceivingPlayer())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Receiving player could not be found"));
        Player askingPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == game.getTradeRequest().getAskingPlayer())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Asking player could not be found"));

        //Check if they have enough resources
        boolean removeFromAsker = askingPlayer.hasEnoughResources(game.getTradeRequest().getToSendResources());
        boolean removeFromReceiver = receivingPlayer.hasEnoughResources(game.getTradeRequest().getToReceiveResources());

        //If they don't, don't let the ai accept the trade request
        if (removeFromAsker && removeFromReceiver) {
            actions.add(new TradeAcceptAction(receivingPlayer.getPlayerId(), tradeService));
        }

        //Always add decline action
        actions.add(new TradeDeclineAction(receivingPlayer.getPlayerId(), tradeService));
        return actions;
    }

    /**
     * Turning all the possible build coordinates in to build actions
     *
     * @param board         the current board state
     * @param currentPlayer the player whose turn it is
     * @param initialPhase  whether the starting phase is going on
     * @return a list of all possible build actions
     */
    private List<Action> getBuildSettlementActions(Board board, int currentPlayer, boolean initialPhase) {
        return AddSettlementCalculator.getAllPossibleSettlementPlacements(currentPlayer,
                board.getSettlements(), board.getRoads(), board.getTiles(), initialPhase).
                stream().map(coordinate -> new BuildSettlementAction(currentPlayer, coordinate, gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Turning all the possible build coordinates in to build actions
     *
     * @param board         the current board state
     * @param currentPlayer the player whose turn it is
     * @param initialPhase  whether the starting phase is going on
     * @return a list of all possible build actions
     */
    private List<Action> getBuildRoadActions(Board board, int currentPlayer, boolean initialPhase) {
        return AddRoadCalculator.calculateAllPossiblePlacements(currentPlayer,
                board.getRoads(), board.getSettlements(), initialPhase).
                stream().map(coordinate -> new BuildRoadAction(currentPlayer, coordinate, gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Turning all the possible build coordinates in to build actions
     *
     * @param board         the current board state
     * @param currentPlayer the player whose turn it is
     * @return a list of all possible build actions
     */
    private List<Action> getBuildCityActions(Board board, int currentPlayer) {
        return AddSettlementCalculator.getValidCityPlacements(currentPlayer,
                board.getSettlements()).stream().map(coordinate ->
                new BuildCityAction(currentPlayer, coordinate, gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Get all possible move robber actions
     *
     * @param board         the current state of the board
     * @param currentPlayer the current player id
     * @return a list of all possible actions
     */
    private List<Action> getMoveRobberActions(Board board, int currentPlayer) {
        return board.getTiles().stream().filter(tile -> !tile.getCoordinate().equals(board.getRobberTile().getCoordinate()) && tile.getTileType() != TileType.WATER)
                .map(tile -> new MoveRobberAction(currentPlayer, tile.getCoordinate(), gameLogicService))
                .collect(Collectors.toList());
    }


    /**
     * Get all the possible steal actions for a player
     *
     * @param game         the current state of the game
     * @param currentPlayer the player that must steal
     * @return a list of all possible steal actions
     */
    private List<Action> getStealActions(Game game, int currentPlayer) {
        if (aiConfig.isUseChanceNodes()) {
            return getChanceStealActions(game, currentPlayer);
        }
        return getNormalStealActions(game.getBoard(), currentPlayer);
    }

    /**
     * Gets all normal steal actions for a player
     *
     * @param board         the current board state
     * @param currentPlayer the player that must steal
     * @return a list of all actions
     */
    private List<Action> getNormalStealActions(Board board, int currentPlayer) {
        return boardService.getPlayersInRobberReach(board).stream().filter(playerId -> playerId != currentPlayer)
                .map(playerId -> new StealAction(currentPlayer, playerId, gameLogicService)).collect(Collectors.toList());
    }

    /**
     * Gets all chance steal actions for a player
     *
     * @param game          the current game state
     * @param currentPlayer the player that must steal
     * @return a list of all actions
     */
    private List<Action> getChanceStealActions(Game game, int currentPlayer) {
        List<Action> actions = new ArrayList<>();

        List<Integer> playerIds = boardService.getPlayersInRobberReach(game.getBoard()).stream().filter(playerId ->
                playerId != currentPlayer).collect(Collectors.toList());

        List<Player> affectedPlayers = new ArrayList<>();
        playerIds.forEach(id -> affectedPlayers.add(game.getPlayers().stream().filter(player ->
                player.getPlayerId()==id).findFirst().orElseThrow(() -> new IllegalArgumentException("Affected player not found"))));

        int totalResources = affectedPlayers.stream().map(Player::getResourcesTotal).reduce(Integer::sum).orElse(0);
        affectedPlayers.forEach(player -> actions.addAll(getStealActionForAffectedPlayer(player, currentPlayer, totalResources)));
        return actions;
    }

    /**
     * Gets all steal actions for an affected player
     *
     * @param affectedPlayer the player that gets robbed
     * @param playerId       the player that must steal
     * @return a list of all actions
     */
    private List<Action> getStealActionForAffectedPlayer(Player affectedPlayer, int playerId, int totalResources) {
        List<Action> actions = new ArrayList<>();

        //TODO: functie voor map -> list extracted??
        Map<Resource, Integer> resourcesMap = affectedPlayer.getResources();
        List<Resource> resourceList = new ArrayList<>();

        //Converts the resource map into a list
        for (Resource resource: resourcesMap.keySet()) {
            for (int i = 0; i < resourcesMap.get(resource); i++) {
                resourceList.add(resource);
            }
        }

        if(resourceList.isEmpty()){
            actions.add(new StealAction(playerId, affectedPlayer.getPlayerId(), gameLogicService));
        } else {
            for (Resource resource: Resource.values()) {
                if (resource != Resource.NOTHING){
                    int count = (int) resourceList.stream().filter(r -> r == resource).count();
                    if (count > 0) {
                        actions.add(new ChanceStealAction((double) count/totalResources, playerId, affectedPlayer.getPlayerId(),
                                resource, gameLogicService));
                    }
                }
            }
        }

        return actions;

    }

    /**
     * Creates for all possible trades with the bank an action
     *
     * @param game          the current game state
     * @param currentPlayer the current player
     * @return a list of all possible bank trade actions
     */
    private List<Action> getBankTradeActions(Game game, Player currentPlayer) {
        List<Action> tradeActions = new ArrayList<>();
        Map<Resource, Integer> playerResources = currentPlayer.getResources();

        for (Resource from: Resource.values()) {
            //Skips checking the ratio if the player has not enough resources to trade with the bank with the lowest ratio
            if (from != Resource.NOTHING && playerResources.get(from) >= 2) {
                //Checks the ratio for trading a resource
                int ratio = tradeService.getBankRatio(game, currentPlayer, from);

                //Checks if the player has enough resources
                if (playerResources.get(from) >= ratio) {
                    //Creates for each resource that is not Resource.NOTHING and not the same resource the player wants to trade,
                    // a TradeBankAction
                    Arrays.stream(Resource.values()).filter(to -> to != from && to != Resource.NOTHING)
                            .forEach(to -> tradeActions.add(new TradeBankAction(tradeService, currentPlayer.getPlayerId(), from, to)));
                }
            }
        }
        return tradeActions;
    }

    /**
     * Gets all discard actions and chooses which type depending on the setting in AiConfig
     *
     * @param player    the player for who the discard actions are requested
     * @return a list of aal discard actions
     */
    private List<Action> getDiscardActions(Player player) {
        if (aiConfig.isRandomDiscards()) {
            return getRandomDiscard(player);
        }
        return getAllDiscardActions(player);
    }

    /**
     * Gets a random discard action for a player
     *
     * @param player    the player who has to discard resources
     * @return a random discard action
     */
    private List<Action> getRandomDiscard(Player player) {
        List<Action> actions = new ArrayList<>();

        List<Resource> resourceList = player.getResourceList();

        List<Resource> combination = new ArrayList<>();
        Random random = new Random();
        int cardsToDiscard = (int) Math.floor((double) resourceList.size() / 2.0);

        List<Resource> tempResources = new ArrayList<>(resourceList);
        for (int i = 0; i < cardsToDiscard; i++) {
            int randomResource = random.nextInt(tempResources.size());
            combination.add(tempResources.get(randomResource));
            tempResources.remove(randomResource);
        }

        actions.add(new DiscardAction(player.getPlayerId(), resourceListToMap(combination), gameLogicService));
        return actions;
    }

    /**
     * Gets all possible discard actions for a player
     *
     * @param player    the player who has to discard resources
     * @return a list of all possible discard actions
     */
    private List<Action> getAllDiscardActions(Player player) {
        List<Resource> resourceList = player.getResourceList();

        Set<List<Resource>> combinations = new HashSet<>();

        int cardsToDiscard = (int) Math.floor((double) resourceList.size() / 2.0);

        //Calling the recursive method with every resource in the resource list
        for (int i = 0; i < resourceList.size(); i++) {
            //Breaking reference
            List<Resource> tempResourceList = new ArrayList<>(resourceList);
            //Creating the start combination
            List<Resource> combination = new ArrayList<>();
            //Adding the resource at index i to the combination
            combination.add(resourceList.get(i));
            //Removing this resource from the available resources
            tempResourceList.remove(i);
            //Calling the recursive methode and breaking reference for combination and tempResourceList
            recursiveDiscardCombinationSearch(cardsToDiscard, new ArrayList<>(combination), new ArrayList<>(tempResourceList), combinations);
        }

        //Converts all possible combinations into DiscardActions
        return combinations.stream().distinct().map(combination -> new DiscardAction(player.getPlayerId(),
                resourceListToMap(combination), gameLogicService)).collect(Collectors.toList());
    }

    /**
     * Converts a list of Resources into a map
     *
     * @param resourceList  the list that has to be converted
     * @return a converted map
     */
    private Map<Resource, Integer> resourceListToMap(List<Resource> resourceList) {
        Map<Resource, Integer> resourceMap = new HashMap<>();

        //Converts combination list into a map
        for (Resource resource: Resource.values()) {
            if (resource != Resource.NOTHING) {
                resourceMap.put(resource, (int) resourceList.stream().filter(r -> r==resource).count());
            }
        }
        return resourceMap;
    }

    /**
     * A recursive method for making all possible combinations of given resources
     *
     * @param resourcesToAdd         how many resources that still have to be added to the combination
     * @param combination   an existing combination that has to be extended
     * @param resourceList  all resources that are still  available to add to combinations
     * @param combinations  all fully formed combination passed though as reference
     */
    private void recursiveDiscardCombinationSearch(int resourcesToAdd, List<Resource> combination, List<Resource> resourceList, Set<List<Resource>> combinations){
        //Decrease the amount of resources that have to be added
        resourcesToAdd--;

        //If no more resources have to be added save combination
        if(resourcesToAdd <= 0) {
            combinations.add(combination);
        }
        else {
            for (int i = 0; i < resourceList.size(); i++) {
                //Breaking reference
                List<Resource> tempResourceList = new ArrayList<>(resourceList);
                //Breaking reference
                List<Resource> tempCombination = new ArrayList<>(combination);
                //Adding the resource at index i to the combination
                tempCombination.add(resourceList.get(i));
                //Removing this resource from the available resources
                tempResourceList.remove(i);
                //Calling the recursive methode and breaking reference for combination and tempResourceList
                recursiveDiscardCombinationSearch(resourcesToAdd, tempCombination, new ArrayList<>(tempResourceList), combinations);
            }
        }
    }

    /**
     * Get all the possible Knight development card actions for a player
     *
     * @param board         the board
     * @param currentPlayer the current player id
     * @return a list of all possible knight development card actions
     */
    private List<Action> getKnightCardActions(Board board, int currentPlayer) {
        return board.getTiles().stream()
                .filter(
                        //filter out the current robbertile
                        t -> !t.getCoordinate().equals(board.getRobberTile().getCoordinate()))
                .filter(
                        //filter out all water tiles
                        t -> !t.getTileType().equals(TileType.WATER))
                .map(
                        //map to an action for all possible coordinates
                        t -> new UseDCKnightAction(currentPlayer, t.getCoordinate(), gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Get all the possible Road Building development card actions for a player
     * Immediately builds ONE road, the second has to be built after
     *
     * @param board         the board
     * @param currentPlayer the current player id
     * @return a list of all possible Road Building development card actions
     */
    private List<Action> getRoadBuildingCardActions(Board board, int currentPlayer){
        return AddRoadCalculator.calculateAllPossiblePlacements(
                currentPlayer, board.getRoads(), board.getSettlements(),
                false).stream()
                .map(
                        //map to an action for all possible coordinates
                        coord -> new UseDCRoadBuildingAction(currentPlayer, coord, gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Get all the possible Year of Plenty development card actions for a player
     *
     * @param currentPlayer the current player id
     * @return a list of all Year of Plenty development card actions
     */
    private List<Action> getYearOfPlentyCardActions(int currentPlayer){
        List<Action> yopActions = new ArrayList<>();
        List<Resource> resources = Arrays.stream(Resource.values())
                .filter(r -> !r.equals(Resource.NOTHING))
                .collect(Collectors.toList());

        resources.forEach(
                resource1 -> resources.forEach(
                        resource2 -> yopActions.add(new UseDCYearOfPlentyAction(currentPlayer, resource1, resource2, gameLogicService))
                )
        );

        return yopActions;
    }

    /**
     * Get all the possible Monopoly development card actions for a player
     *
     * @param currentPlayer the current player id
     * @return a list of all Monopoly development card actions
     */
    private List<Action> getMonopolyCardActions(int currentPlayer){
        return Arrays.stream(Resource.values())
                .filter(r -> !r.equals(Resource.NOTHING))
                .collect(Collectors.toList())
                .stream()
                .map(resource -> new UseDCMonopolyAction(currentPlayer, resource, gameLogicService))
                .collect(Collectors.toList());
    }

    /**
     * Get a Victory Point card action for the player
     *
     * @param currentPlayer the current player id
     * @return a Victory Point card action
     */
    private Action getVictoryPointAction(int currentPlayer){
        return new UseDCVictoryPointAction(currentPlayer, gameLogicService);
    }

    /**
     * Get an action to purchase a development card
     *
     * @param currentPlayer the current player id
     * @return a Buy Development Card action
     */
    private Action getBuyDevelopmentCardAction(int currentPlayer){
        return new BuyDevelopmentAction(currentPlayer, gameLogicService);
    }

    /**
     * Adds domain logic to the calculations, this includes bonus score for victory points, being the leader, holding
     * certain achievements. And might be expanded upon to include other metrics
     *
     * This is only executed if 'useOtherMetrics' is set to true
     *
     * @param tempNode this should be the last node of the branch, the one on which backpropagation starts
     * @return the score gotten upon reaching the end of the branch
     */
    public double calculateVirtualWins(Node tempNode){

        double virtualScore = 0.0;

        //virtual wins
        if(aiConfig.isUseOtherMetrics() && tempNode.getState().getGame() != null) {

            List<Player> players = tempNode.getState().getGame().getPlayers();

            //give bonus points per victory point gotten
            virtualScore += aiConfig.getVictoryPointScore() * players.stream()
                    .filter(player -> player.getPlayerId() == tempNode.getState().getPlayerNo())
                    .map(Player::getVictoryPointsAmount)
                    .findFirst().orElse(0);

            //give bonus incentive to stay in the leader position and have more VP than others
            Player leader = players.stream().max(Comparator.comparingInt(Player::getVictoryPointsAmount)).orElse(null);

            if (leader != null && leader.getPlayerId() == tempNode.getState().getPlayerNo()) {
                virtualScore += aiConfig.getVpLeaderBonus();
            }


            //give additional incentive points for getting longest road (players ALREADY get 2 VP's while holding it)
            if (tempNode.getState().getGame().getPlayerIdWithLongestRoad() == tempNode.getState().getPlayerNo()){
                virtualScore += aiConfig.getLongestRoadBonus();
            }
        }

        return virtualScore;
    }
}
