package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.controllers.RESTToSocketsController;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.ai.actions.Action;
import kdg.colonia.gameService.domain.boardCalculations.AddRoadCalculator;
import kdg.colonia.gameService.domain.boardCalculations.AddSettlementCalculator;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.utilities.SassyExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final MongoTemplate mongoTemplate;
    private final PlayerService playerService;
    private final IBoardCreationService boardCreationService;
    private final SassyExceptionMessageGenerator messageGenerator;
    private final CardPileCreationService cardPileCreationService;
    private final GameInitService gameInitService;
    private final TurnTokenService turnTokenService;
    private final TradeService tradeService;
    private final GameLogicService gameLogicService;
    private final RESTToSocketsController socketController;
    private final MonteCarloService aiService;
    private final ChatBotService chatBotService;
    private final GameConfig gameConfig;

    private final ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();

    /**
     * This method Handles the creation of a new game.
     *
     * @param userIds is a list of the users id's from the authentication db.
     * @return A game object with the player data and a generated board.
     */
    public Game createGame(List<String> userIds, int amountOfAIs, String userIdOfHost) {
        if (userIds.size() + amountOfAIs > 4 && userIds.size() + amountOfAIs < 2) {
            log.warn("Can not create game with more than 4 or less than 2 players");
            throw new IllegalArgumentException("Can not create game with more than 4 or less than 2 players");
        }

        try {
            //Create game with new list of users and the gameboard
            Game game = new Game(playerService.generateGamePlayers(userIds, amountOfAIs), boardCreationService.generate(), cardPileCreationService.generate(), userIdOfHost);

            if (gameConfig.isOnlyAIGameExperimental()) {
                log.info("Game[{}]: WARNING: ALL USERS CREATED ARE AI'S!! THIS IS AN UNSTABLE DEVELOPMENT FEATURE", game.getId());
                game.getPlayers().forEach(player -> player.setAI(true));
            }

            gameRepository.save(game);
            initialRollsAI(game);

            log.info("Game[{}]: Created with following users [{}]", game.getId(), game.getPlayers().stream().map(Player::getUserId).collect(Collectors.joining(", ")));

            return getGame(game.getId());
        } catch (Exception e) {
            log.error("Error creating game: " + e.getMessage());
            throw e;
        }
    }

    private void initialRollsAI(Game game) {
        for (Player ai : game.getPlayers().stream().filter(Player::isAI).collect(Collectors.toList())) {
            rollForInitiative(game.getId(), ai.getUserId());
        }
    }

    /**
     * This method is called when the player has clicked the "end turn" button or their play-time has expired.
     * Logic handled by @see TurnService.java
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential number of the player
     * @return true if successful
     */
    public boolean endTurn(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (turnTokenService.endTurn(game, player)) {
            gameRepository.save(game);

            if (player.getVictoryPointsAmount() >= gameConfig.getVictoryPointsWin()) {
                game.setCurrentPlayerId(playerId);
                endGame(game);
                log.info("Game[{}]: Ending game due to reaching max score", game.getId());
                gameRepository.save(game);
                return true;

            } else {
                boolean result;
                Player nextPlayer = validatePlayerID(game, game.getCurrentPlayerId());
                log.info("Game[{}]: Player[{}] ended turn, Player[{}] is up next", game.getId(), player.getPlayerId(), nextPlayer.getPlayerId());
                if (nextPlayer.isAI()) {
                    turnTokenService.startTurn(nextPlayer);
                    gameRepository.save(game);
                    taskScheduler.execute(() -> startAITurn(gameId, nextPlayer.getPlayerId()));
                    result = true;
                } else {
                    result = turnTokenService.startTurn(nextPlayer);
                    gameRepository.save(game);
                }
                try {
                    socketController.sendEndTurnNotice(gameId);
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }
                return result;
            }
        }
        return false;
    }

    /**
     * This method is called when the current player is an AI player
     *
     * @param gameId the UUID of the current game
     * @param playerId the player we started with
     * @return true if successful
     */
    public boolean startAITurn(String gameId, int playerId) {
        return startAITurn(gameId, playerId, 0);
    }

    /**
     * This method is called when the current player is an AI player
     *
     * @param gameId the UUID of the current game
     * @param playerId the player we started with
     * @param failedAttempts after this reaches zero we act for the player
     * @return true if successful
     */
    private boolean startAITurn(String gameId, int playerId, int failedAttempts) {
        Game game = getGame(gameId);

        if (playerId != game.getCurrentPlayerId()) {
            return false;
        }

        if(failedAttempts == 0) {
            log.info("Game[{}]: - - - MC starting", game.getId());
        }

        while (true) {
            //tries to stop the game if it notices it has stopped
            if (!game.getGameState().equals(GameState.ACTIVE)) {
                log.info("Game[{}]: Has finished", gameId);
                return true;
            }

            //Gets current player and checks if he is an AI
            Player player = validatePlayerID(game, game.getCurrentPlayerId());

            if (!player.isAI()) {
                log.info("Game[{}]: - - - MC stopping, Next player[{}] is Human", gameId, player.getPlayerId());
                return true;
            }

            if(game.getPlayers().stream().filter(p -> !p.isAI()).anyMatch(p -> p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))) {

                log.info("Game[{}]: Waiting for humans to discard, attempt #{}", gameId, failedAttempts);

                taskScheduler.schedule(() ->
                        startAITurn(gameId, player.getPlayerId(), failedAttempts + 1), Instant.now().plusSeconds(gameConfig.getTimeBetweenAttempts()));

                if (failedAttempts == 0) {
                        List<Integer> playerIds = new ArrayList<>();
                        for (Player gamePlayer : game.getPlayers()) {
                            if (gamePlayer.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)){
                                playerIds.add(gamePlayer.getPlayerId());
                            }
                        }
                        try{
                            socketController.sendDiscard(gameId, playerIds);
                        } catch (Exception e) {
                            log.error("Could not reach socket controller");
                        }
                }

                if (failedAttempts >= gameConfig.getAttemptsBeforeAutoDiscard()) {
                    log.info("Game[{}]: HUMAN SLOW, AI[{}] BORED, TAKE OVER NOW!", gameId, player.getPlayerId());

                    game = letAIsDiscardResources(game);

//                    game = aiService.findNextMove(game);
//                    //todo perform action on fresh gotten game
//                    gameRepository.save(game);
                }
                return true;
            }

            if(game.getPlayers().stream().filter(Player::isAI).anyMatch(p -> p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))) {
                game = letAIsDiscardResources(game);
            }

            game = doAITurn(game, player);
        }
    }

    /**
     * Completes all actions available for the AI until an other player takes over
     *
     * @param game   the current game
     * @param player the current player
     * @return the game after one action has been completed
     */
    public Game doAITurn(Game game, Player player) {
        log.info("Game[{}]: Running Monte Carlo for Player[{}]", game.getId(), player.getPlayerId());
        while (game.getCurrentPlayerId() == player.getPlayerId()) {

            if(game.getPlayers().stream().anyMatch(p -> p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))){
                log.info("Game[{}]: Pausing AI[{}]", game.getId(), player.getPlayerId());
                return game;
            }

            //Do the action
            game = doAIAction(game, player);

            try {
                socketController.sendRefreshBoard(game.getId());
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
        }

        return game;
    }

    public Game doAIAction(Game game, Player player){
        Pair<Game, Action> pair = aiService.findNextState(game, player.getPlayerId());

        if(gameConfig.isPerformAIOnFreshGame()){
            try {
                Game freshGame = gameRepository.findById(game.getId()).orElse(null);
                if(freshGame != null){
                    game = freshGame;
                }
                game = pair.getSecond().performAction(game);
                gameRepository.save(game);

                //Send a random message fitting for the action
                chatBotService.sendMessage(game.getId(), player, pair.getSecond());

            } catch(Exception e){
                if(game != null) {
                    log.info("Game[{}]: AI[{}] couldn't perform action on fresh game, restarting simulation.", game.getId(), player.getPlayerId());
                }
            }
        }
        //This can lead to potential async issues where recent actions by other players are overwritten by AI
        else {
            gameRepository.save(pair.getFirst());
        }
        return game;
    }

    /**
     * This method ends the game and sets the state to FINISHED
     *
     * @param game  the game that has ended
     */
    public void endGame(Game game) {
        game.setGameState(GameState.FINISHED);
        for (Player player : game.getPlayers()) {
            player.getRemainingActions().clear();
        }

        if (game.getPlayers().stream().map(Player::getVictoryPointsAmount).max(Comparator.comparingInt(Integer::intValue)).orElse(0) >= gameConfig.getVictoryPointsWin()) {
            log.info("Game[{}]: Ended after reaching required victory points", game.getId());
        } else {
            log.info("Game[{}]: Ended without reaching the end score", game.getId());
        }

        try {
            socketController.sendEndGame(game.getId(), game.getCurrentPlayerId());
        } catch (Exception e) {
            log.error("Could not reach socket controller");
        }
    }

    /**
     * This method end a game early
     *
     * @param gameId    the UUID of the game
     */
    public void endGameEarly(String gameId) {
        Game game = getGame(gameId);
        endGame(game);
        gameRepository.save(game);
        log.info("Game[{}]: Ended early", game.getId());
    }

    /**
     * This method pauses an active game
     *
     * @param gameId    the UUID of the game
     * @return true if successful
     */
    public boolean pauseGame(String gameId) {
        Game game = validateGameID(gameId);
        if (game.getGameState() == GameState.ACTIVE) {
            game.setGameState(GameState.PAUSED);
            gameRepository.save(game);
            try {
                socketController.sendPauseGameNotice(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
            log.info("Game[{}]: Paused", game.getId());
            return true;
        } else {
            throw new IllegalArgumentException(String.format("Game[%s] is not paused and thus cannot be resumed", gameId));
        }
    }

    /**
     * This method resumes a paused game
     *
     * @param gameId    the UUID of the game
     * @return the now reactivated game if successful
     */
    public Game resumeGame(String gameId) {
        Game game = validateGameID(gameId);
        if (game.getGameState() == GameState.PAUSED) {
            game.setGameState(GameState.ACTIVE);
            gameRepository.save(game);
            log.info("Game[{}]: Resumed", game.getId());

            if (game.getCurrentPlayer().isAI()) {
                taskScheduler.execute(()->startAITurn(gameId, game.getCurrentPlayerId()));
            }
            return game;
        } else {
            throw new IllegalArgumentException(String.format("Game[%s] is not paused and thus cannot be resumed", gameId));
        }
    }

    /**
     * Processes the request of the user to roll the dice
     *
     * @param gameId    the UUID of the game.
     * @param playerId  the current player who's starting his turn.
     * @return an array of the individual die outcomes
     */
    public int[] rollDice(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (player.getRemainingActions().contains(PlayerAction.ROLL) && !(player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER))) {

            int[] rolls = gameLogicService.rollDice(game, player);
            boolean seven = false;
            if (Arrays.stream(rolls).sum() == 7) {
                seven = true;
            }

            log.info("Game[{}]: Player[{}] has rolled the dice and got {}", gameId, playerId, rolls);

            gameRepository.save(game);
            if (seven) {
                try {
                    socketController.sendRolledSeven(gameId, playerId);
                    socketController.sendRefreshBoard(gameId);
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }

                try{
                    List<Integer> playerIds = game.getPlayers().stream()
                            .filter(p -> p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))
                            .map(Player::getPlayerId)
                            .collect(Collectors.toList());

                    socketController.sendDiscard(gameId, playerIds);
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }

                gameRepository.save(game);
                taskScheduler.execute(() -> waitForHumansToDiscard(game.getId(), playerId, 0));

            } else {
                try {
                    socketController.sendRefreshBoard(gameId);
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }
            }
            return rolls;

        } else {
            log.warn("Game[{}]: Received an illegal request to access method: rollDice({},{})", gameId, gameId, playerId);
            throw new IllegalStateException(messageGenerator.generateException(String.format("Player %d tried to roll the dice, but wasn't allowed!", playerId)));
        }
    }

    /**
     * Starts a recursive loop that waits for all human players to discard, before letting the AI's discard.
     * If it takes too long he starts discarding for others
     *
     * @param gameId the gameId we want to run this for
     * @param playerId the current player
     * @param failedAttempts the number of recursive attempts made to wait for DUMB HUUMIES
     */
    private void waitForHumansToDiscard(String gameId, int playerId, int failedAttempts) {
        Game game = getGame(gameId);

        if (!game.getGameState().equals(GameState.ACTIVE)) {
            log.info("Game[{}]: Has finished", game.getId());
            return;
        }

        if (game.getPlayers().stream().anyMatch(p -> !p.isAI() && p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))) {

            log.info("Game[{}]: Waiting for humans to discard, attempt #{}", game.getId(), failedAttempts);

            taskScheduler.schedule(() ->
                    waitForHumansToDiscard(gameId, playerId, failedAttempts + 1),
                    Instant.now().plusSeconds(gameConfig.getTimeBetweenAttempts()));

            if (failedAttempts >= gameConfig.getAttemptsBeforeAutoDiscard()) {
                log.info("Game[{}]: HUMAN SLOW, AI[{}] BORED, TAKE OVER NOW!", game.getId(), playerId);
                game = letAIsDiscardResources(game);
                gameRepository.save(game);
                return;
            }
            return;
        }

        letAIsDiscardResources(game);
    }

    /**
     * Loops through players and AI's that still have discard_resources and runs a findNextMove for them
     *
     * @param game AI edited game
     * @return the edited game
     */
    private Game letAIsDiscardResources(Game game) {
        for (Player player : game.getPlayers().stream().filter(p -> p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)).collect(Collectors.toList())) {
            Pair<Game, Action> pair = aiService.findNextState(game, player.getPlayerId());
            game = pair.getFirst();
        }

        gameRepository.save(game);
        return game;
    }

    /**
     * This method is called to build a road or settlement in and outside the starting phase
     *
     * @param gameId        the UUID of the game
     * @param playerId      the current player who's starting his turn
     * @param coordinate    the coordinate where the settlement/road is wanted to be placed
     * @return the board state after placements
     */
    public Board build(String gameId, int playerId, Coordinate coordinate) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);
        int playerWithLongestRoadBeginning = game.getPlayerIdWithLongestRoad();

        if (gameLogicService.build(game, player, coordinate)) {
            gameRepository.save(game);

            log.info("Game[{}]: Player[{}] built at {}", gameId, playerId, coordinate);

            if (!(player.getRemainingActions().contains(PlayerAction.INITIAL1) || player.getRemainingActions().contains(PlayerAction.INITIAL2)) || coordinate.getDirection() == Direction.NONE) {
                try {
                    socketController.sendRefreshBoard(gameId);
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }
            }

            if (game.getCurrentPlayerId() != playerId && game.getCurrentPlayer().isAI()) {
                taskScheduler.execute(() -> startAITurn(gameId, game.getCurrentPlayerId()));
            }

            if (game.getPlayerIdWithLongestRoad() != playerWithLongestRoadBeginning) {
                try {
                    socketController.sendNewAchievementNotice(game.getId(), game.getPlayerIdWithLongestRoad(), "LONGEST_ROAD");
                } catch (Exception e) {
                    log.error("Could not reach socket controller");
                }
            }
            return game.getBoard();
        }
        return null;
    }

    /**
     * This method is called to build a city
     *
     * @param gameId        the UUID of the game
     * @param playerId      the current player who's starting his turn
     * @param coordinate    the coordinate where the settlement/road is wanted to be placed
     * @return the board state after placements
     */
    public Board upgradeSettlementToCity(String gameId, int playerId, Coordinate coordinate) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (gameLogicService.upgradeSettlementToCity(game, player, coordinate)) {
            gameRepository.save(game);
            log.info("Game[{}]: Player[{}] upgraded settlement at {}", gameId, playerId, coordinate);
            try {
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
            return game.getBoard();
        }

        return null;
    }

    /**
     * Before the game starts, every player rolls for initiative.
     * Once all players have rolled, initial1-phase begins.
     *
     * @param gameId UUID of the game
     * @param userId UUID of the player
     * @return The resulting dice roll or null on error
     */
    public int[] rollForInitiative(String gameId, String userId) {
        Game game = validateGameID(gameId);
        final Player[] player = {null};
        game.getPlayers().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .ifPresent(player1 -> player[0] = player1);

        if (player[0] == null) {
            log.warn(String.format("Game[%s]: user %s tried to roll for initiative, but doesn't belong in this game.", gameId, userId));
            return new int[]{0, 0};
        }
        int[] roll = gameInitService.rollForInitiative(game, player[0]);

        gameRepository.save(game);
        if (game.getPlayers().stream().noneMatch(p -> p.getRemainingActions().contains(PlayerAction.INITROLL))) {
            if (game.getCurrentPlayer().isAI()) {
                taskScheduler.execute(() -> startAITurn(gameId, game.getCurrentPlayerId()));
            }
        }
        //if initial rolls are done => send notice
        if (game.getCurrentPlayerId() == 1) {
            try {
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
        }
        return roll;
    }

    /**
     * The current player is allowed to move the robber.
     *
     * @param gameId     the UUID of the game.
     * @param playerId   the current player who's starting his turn.
     * @param coordinate the coordinate the robber will be moved to.
     * @return a list of players you can steal from, is null if none available
     */
    public List<Integer> moveRobber(String gameId, int playerId, Coordinate coordinate) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        List<Integer> affectedPlayers = gameLogicService.moveRobber(game, player, coordinate);

        log.info("Game[{}]: Player[{}] attempting to move robber to {}", gameId, playerId, coordinate);

        if (affectedPlayers != null) {
            gameRepository.save(game);
            try {
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
            return affectedPlayers;
        }

        return null;
    }

    /**
     * This method steals a random resource from one player and transfers it to another
     *
     * @param gameId              the UUID of the game.
     * @param playerId            the current player who's starting his turn.
     * @param playerIdToStealFrom the player we're stealing from.
     * @return The resource that has been transferred. In case front end would like to display this
     */
    public Resource stealResources(String gameId, int playerId, int playerIdToStealFrom) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        Player playerToStealFrom = validatePlayerID(game, playerIdToStealFrom);
        isPlayable(game);

        Resource stolenResource = gameLogicService.stealResources(game, player, playerToStealFrom);

        if (stolenResource != null) {
            log.info("Game[{}]: Player[{}] stole '{}' from Player[{}]", gameId, playerId, stolenResource, playerIdToStealFrom);
        }
        try{
            socketController.sendRefreshBoard(gameId);
        } catch (Exception e) {
            log.error("Could not reach socket server");
        }

        gameRepository.save(game);
        return stolenResource;
    }

    /**
     * Players can buy development cards using this method
     *
     * @param gameId   UUID of the game
     * @param playerId sequential player number
     * @return returns the updated  player or null if error
     */
    public ProgressCard buyCard(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (gameLogicService.buyCard(game, player)) {
            gameRepository.save(game);
            ProgressCard pc = player.getNewCards().get(player.getNewCards().size() - 1);
            log.info("Game[{}]: Player[{}] bought {}", gameId, playerId, pc.getCardType());

            try{
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Socket controller could not be reached");
            }
            return pc;
        }
        return null;
    }

    /**
     * This method is called when a player wants to play any card.
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential ID of the player
     * @param cardType the type of progress card that the player wants to play
     * @return true if successful
     */
    public boolean playCard(String gameId, int playerId, ProgressCardType cardType) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);
        int largestArmyBeginning = game.getPlayerWithLargestArmy();

        if (gameLogicService.playCard(game, player, cardType)) {
            gameRepository.save(game);
            if (game.getPlayerWithLargestArmy() != largestArmyBeginning) {
                try {
                    socketController.sendNewAchievementNotice(game.getId(), game.getPlayerWithLargestArmy(), "LARGEST_ARMY");
                } catch (Exception e) {
                    log.error("could not reach socket for new achievement");
                }

            }
            return true;
        }

        return false;
    }

    /**
     * this method is called when a player tries to receive their year of plenty resources.
     * player needs to have the YOP token.
     *
     * @param gameId    the UUID of the game
     * @param playerId  the sequential ID of the player
     * @param resource1 the first resource the player wants
     * @param resource2 the second resource the player wants
     * @return true if successful
     */
    public boolean yearOfPlenty(String gameId, int playerId, Resource resource1, Resource resource2) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (gameLogicService.yearOfPlenty(game, player, resource1, resource2)) {
            gameRepository.save(game);
            log.info("Game[{}]: Player[{}] used 'Year of Plenty' and got {} and {}", gameId, playerId, resource1, resource2);
            return true;
        }

        return false;
    }

    /**
     * this method is called when a player tries to receive their year of plenty resources.
     * player needs to have the MONOPOLY token.
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential ID of the player
     * @param resource the resource the player wants
     * @return true if successful
     */
    public boolean monopoly(String gameId, int playerId, Resource resource) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (gameLogicService.monopoly(game, player, resource)) {
            gameRepository.save(game);
            log.info("Game[{}]: Player[{}] used 'Monopoly' and got all {}", gameId, playerId, resource);
            return true;
        }

        return false;
    }

    /**
     * The player completes his action to remove a certain amount of resources
     *
     * @param gameId    the UUID of the game.
     * @param playerId  the player discarding resources.
     * @param resources the resources the player discard.
     */
    public boolean discardResources(String gameId, int playerId, Map<Resource, Integer> resources) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (gameLogicService.discardResources(game, player, resources)) {
            try {
                gameRepository.save(game);
                log.info("Game[{}]: Player[{}] discarded {}", gameId, playerId, resources);
            } catch (Exception e) {
                log.warn(e.getMessage());
                throw e;
            }
            return checkForAITurnAfterDiscard(game, playerId);
        }
        return false;
    }

    /**
     * This method checks whether all players have discarded resources so the AI can move on
     *
     * @param game     the current game.
     * @param playerId the player discarding resources.
     * @return true if successful
     */
    private boolean checkForAITurnAfterDiscard(Game game, int playerId) {
        if (validatePlayerID(game, playerId).isAI()) {
            boolean playersDone = game.getPlayers().stream().anyMatch(p ->
                    !p.isAI() && p.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES));

            if (playersDone) {
                //doAITurn(game.getId(), playerId);
                return endTurn(game.getId(), playerId);
            }
        }
        return true;
    }

    /**
     * Gets resources from a player.
     *
     * @param gameId   the Id of the game the player is playing.
     * @param playerId the players logical id in the game.
     * @return a map of resource type and the amount of this resource that the player has.
     */
    public Map<Resource, Integer> getResourcesFromPlayer(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        return playerService.getResourcesForPlayerInGame(player);
    }

    /**
     * Get all the possible road placements for a player
     *
     * @param gameId   the UUID of the game.
     * @param playerId the player to check for
     * @return a list of all possible coordinates
     */
    public List<Coordinate> possibleRoadPlacements(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Board board = game.getBoard();
        Player player = getPlayer(gameId, playerId);
        isPlayable(game);

        playerId = player.getPlayerId();
        boolean initialPhase = board.isInitialPhase(playerId);
        if (!playerService.playerAllowedToBuildRoad(player) && !initialPhase) {
            return new ArrayList<>();
        }
        return AddRoadCalculator.calculateAllPossiblePlacements(playerId,
                board.getRoads(), board.getSettlements(), initialPhase);
    }

    /**
     * Get all the possible settlements placements for a player
     *
     * @param gameId   the UUID of the game.
     * @param playerId the player to check for
     * @return a list of all possible coordinates
     */
    public List<Coordinate> possibleSettlementPlacements(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Board board = game.getBoard();
        Player player = getPlayer(gameId, playerId);
        isPlayable(game);
        playerId = player.getPlayerId();
        boolean initialPhase = board.isInitialPhase(playerId);
        if (!playerService.playerAllowedToBuildSettlement(player) && !initialPhase) {
            return new ArrayList<>();
        }
        return AddSettlementCalculator.getAllPossibleSettlementPlacements(player.getPlayerId(), board.getSettlements(),
                board.getRoads(), board.getTiles(), initialPhase);
    }

    /**
     * Checks if a player is a valid player in the game
     *
     * @param gameId   the UUID of the game.
     * @param playerId the player to check for
     * @return a validated player
     */
    public Player getPlayer(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        isPlayable(game);
        return validatePlayerID(game, playerId);
    }

    /**
     * This method returns the ratio at which a player can trade a resource.
     * The ratio is returnValue:1
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential ID of the player
     * @param resource the resource the player wants to obtain
     * @return the amount of resources needed to get 1 of the chosen resource
     */
    public int getTradeRatio(String gameId, int playerId, Resource resource) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        return tradeService.getBankRatio(game, player, resource);
    }

    /**
     * This method lets the player trade with the bank.
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential number of the player
     * @param from     list of resources the player wants give up for the trade
     * @param to       the resource the player expects in return from the trade
     * @return true if successful
     */
    public boolean tradeWithBank(String gameId, int playerId, Resource from, Resource to) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);

        if (tradeService.tradeWithBank(game, player, from, to)) {
            gameRepository.save(game);
            log.info("Game[{}]: Player[{}] traded {} for {}", gameId, playerId, from, to);
            try{
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket server");
            }
            return true;
        }
        return false;
    }

    /**
     * This method returns all paused and finished games for a certain userid
     *
     * @param userId the UUID of the player
     * @param filter can be NONE, PAUSED or FINISHED
     * @return a list of games if successful
     */
    public List<Game> getGamesOverview(String userId, String filter) {
        BasicQuery query;
        switch (filter) {
            case "NONE":
                query = new BasicQuery("{ players: { $elemMatch : { userId : \"" + userId + "\" } }, gameState : { $in : [\"PAUSED\",\"FINISHED\" ] } }");
                break;
            case "FINISHED":
                query = new BasicQuery("{ players: { $elemMatch : { userId : \"" + userId + "\" } }, gameState : { $in : [\"FINISHED\" ] } }");
                break;
            case "PAUSED":
                query = new BasicQuery("{ players: { $elemMatch : { userId : \"" + userId + "\" } }, gameState : { $in : [\"PAUSED\"] } }");
                break;
            default:
                log.warn(String.format("User[%s]: received gamesOverview request with invalid filter: %s", userId, filter));
                throw new IllegalArgumentException(String.format("User[%s]: received gamesOverview request with invalid filter: %s", userId, filter));
        }
        return mongoTemplate.find(query, Game.class);
    }

    /**
     * This methods starts a trade request
     * When the ai is the receiving player, he simulates whether he wants to accept of decline
     *
     * @param gameId            the UUID of the game
     * @param askingPlayerId    the id of the player wanting to trade
     * @param receivingPlayerId the id of the player receiving a trade request
     * @param toSend            the resources that are sent by the asker
     * @param toReceive         the resources that are received by the asker
     * @return the game state after creating the trade request
     */
    public Game startTradeRequest(String gameId, int askingPlayerId, int receivingPlayerId, Map<Resource, Integer> toSend, Map<Resource, Integer> toReceive) {
        Game game = validateGameID(gameId);
        Player askingPlayer = validatePlayerID(game, askingPlayerId);
        Player receivingPlayer = validatePlayerID(game, receivingPlayerId);
        isPlayable(game);
        if (!askingPlayer.getRemainingActions().contains(PlayerAction.TRADE)) {
            log.warn(String.format("Game[%s]: player %d does not have a trade token", gameId, askingPlayer.getPlayerId()));
            throw new IllegalStateException("Player does not have a trade token");
        }
        game = tradeService.startTradeRequest(game, askingPlayer, receivingPlayer, toSend, toReceive);
        askingPlayer.getRemainingActions().remove(PlayerAction.TRADE);

        gameRepository.save(game);

        log.info("Game[{}]: Player[{}] sent trade request to Player[{}]. Offering {} for {}", gameId, askingPlayer.getPlayerId(), receivingPlayer.getPlayerId(), toSend, toReceive);

        //If the receiving player is an ai let him simulate
        if (receivingPlayer.isAI()) {
            //Let the ai simualte
            game = aiService.findNextMove(game);

            //Notify front end
            try {
                gameRepository.save(game);
                socketController.sendRefreshBoard(gameId);

                return game;
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
        }

        try {
            socketController.sendTradeNotice(gameId, receivingPlayerId);
        } catch (Exception e) {
            log.error("Could not reach socket controller");
        }
        return game;
    }

    /**
     * This method is called when a player wants to accept a trade request
     *
     * @param gameId            the UUID of the game
     * @param tradeId           the UUID of the trade request
     * @param acceptingPlayerId the id of the player that wants to accept the trade request
     * @return the game state after acceptance of the trade request
     */
    public Game acceptTradeRequest(String gameId, String tradeId, String acceptingPlayerId)  {
        Game game = validateGameID(gameId);
        isPlayable(game);

        //Get validated players
        Player askingPlayer = validatePlayerID(game, game.getTradeRequest().getAskingPlayer());
        Player acceptingPlayer = game.getPlayers().stream()
                .filter(p -> p.getUserId().equals(acceptingPlayerId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Player could not be found in game"));

        //Check if the correct player tries to accept the trade
        if (game.getTradeRequest().getReceivingPlayer() != acceptingPlayer.getPlayerId()) {
            log.warn(String.format("Game[%s]: player %d tried to accept a trade, but the trade wasn't addressed at them",
                    game.getId(), acceptingPlayer.getPlayerId()));
            return game;
        }

        game = tradeService.acceptTradeRequest(game, askingPlayer, acceptingPlayer);

        log.info("Game[{}]: Player[{}] accepted request[{}]", gameId, acceptingPlayer.getPlayerId(), tradeId);

        gameRepository.save(game);
        try {
            socketController.sendRefreshBoard(gameId);
        } catch (Exception e) {
            log.error("Could not reach socket controller");
        }
        return game;
    }

    /**
     * this method is called to cancel a trade request
     *
     * @param gameId    the UUID of the game
     * @param playerId  the id of the player that wants to cancel the trade request
     * @return the game state after acceptance of the trade request
     */
    public Game cancelTradeRequest(String gameId, int playerId) {
        Game game = validateGameID(gameId);
        Player player = validatePlayerID(game, playerId);
        isPlayable(game);
        if (tradeService.cancelTradeRequest(game, player)) {
            gameRepository.save(game);

            log.info("Game[{}]: Player[{}] cancelled trade request", gameId, playerId);
            try {
                socketController.sendRefreshBoard(gameId);
            } catch (Exception e) {
                log.error("Could not reach socket controller");
            }
            return game;
        }
        return null;
    }

    /**
     * Checks if player has longest road and updates cards accordingly
     *
     * @param game the game we want to update the longest road for
     */
    protected void updateLongestRoad(Game game) {
        int result = gameLogicService.updateLongestRoad(game);
        if (result != 0) {
            socketController.sendNewAchievementNotice(game.getId(), result, "LONGEST_ROAD");
        }
    }

    /**
     * This methods validates a game
     *
     * @param gameId    the UUID of the game
     * @return a validated game
     */
    private Game validateGameID(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> {
                    String err = String.format("Game with id[%s] could not be found", gameId);
                    log.error(err);
                    return new IllegalArgumentException(err);
                });
    }

    /**
     * Checks if a game is playable
     *
     * @param game  the game to check
     */
    private void isPlayable(Game game) {
        if (game.getGameState().equals(GameState.PAUSED)) {
            throw new IllegalStateException(String.format("Game[%s]: game is paused an cannot receive requests", game.getId()));
        }
    }

    /**
     * validates a player in a game
     *
     * @param game      the game to check
     * @param playerId  the playerId to check
     * @return a validated player
     */
    private Player validatePlayerID(Game game, int playerId) {
        return game.getPlayers().stream().filter(p -> p.getPlayerId() == playerId).findFirst()
                .orElseThrow(() -> {
                    String err = String.format("Player with id[%d] could not be found", playerId);
                    log.error(err);
                    return new IllegalArgumentException(err);
                });
    }

    /**
     * Returns a validated game
     *
     * @param gameId   the UUID of the game
     * @return a validated game
     */
    public Game getGame(String gameId) {
        return validateGameID(gameId);
    }
}
