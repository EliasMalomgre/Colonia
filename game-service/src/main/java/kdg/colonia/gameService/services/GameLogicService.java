package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.tiles.TileType;
import kdg.colonia.gameService.utilities.SassyExceptionMessageGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameLogicService {

    private final BoardService boardService;
    private final GameInitService gameInitService;
    private final TurnTokenService turnTokenService;
    private final CardService cardService;
    private final PlayerService playerService;
    private final IDiceService diceService;
    private final SassyExceptionMessageGenerator messageGenerator;

    /**
     * This method is called to build a road or settlement in and outside the starting phase
     *
     * @param game          the the current game state
     * @param player        the current player who's starting his turn
     * @param coordinate    the coordinate where the settlement/road is wanted to be placed
     * @return whether the placement was successful
     */
    public boolean build(Game game, Player player, Coordinate coordinate) {
        //Check if the player is in the starting phase
        if (player.getRemainingActions().contains(PlayerAction.INITIAL1) || player.getRemainingActions().contains(PlayerAction.INITIAL2)) {
            //Check if the player has no settlements or has 1 settlement and 1 road
            if (game.getBoard().getSettlements().stream().noneMatch(s -> s.getPlayerId() == player.getPlayerId()) ||
                    (game.getBoard().getSettlements().stream().filter(s -> s.getPlayerId() == player.getPlayerId()).count() == 1
                            && game.getBoard().getRoads().stream().filter(r -> r.getPlayerId() == player.getPlayerId()).count() == 1)) {

                //Try add the settlement
                if (!boardService.addInitialSettlementToBoard(game.getBoard(), player, coordinate)) {
                    log.error(String.format("Game[%s]: Player %d tried to build a settlement on an illegal place: %s"
                            , game.getId(), player.getPlayerId(), coordinate));
                }

                if (player.getRemainingActions().contains(PlayerAction.INITIAL2)) {
                    List<Tile> tiles = game.getBoard().getTilesAroundSettlement(coordinate);
                    tiles.stream().filter(tile -> tile.getTileType() != TileType.WATER && tile.getTileType() != TileType.DESERT)
                            .forEach(tile -> player.addResources(tile.getResourceType(), 1));
                }
            }
            //Else a road has to be placed
            else {
                //Check if it is a settlement coordinate
                if (coordinate.getDirection() != Direction.NONE) {
                    log.error(String.format("Received a settlement coordinate but should have been a road coordinate: %s", coordinate));
                    return false;
                }
                //Try add the road
                if (boardService.addInitialRoadToBoard(game.getBoard(), coordinate, player)) {
                    //Try pass the next starting phase turn to the next player
                    if (!gameInitService.passInitialTurn(game, player.getPlayerId(), player)) {
                        //if the former method returns false, the init phase has ended
                        turnTokenService.startTurn(player);
                    }
                } else {
                    log.error(String.format("Game[%s]: Player %d tried to build a road on an illegal place: %s",
                            game.getId(), player.getPlayerId(), coordinate));
                }
            }
            return true;

        }
        //Else we are not in the starting phase
        //Check if the player has played a road building development card
        else if (player.getRemainingActions().contains(PlayerAction.ROAD_BUILDING) && !(player.getRemainingActions()
                .contains(PlayerAction.MOVE_ROBBER) || player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))) {
            if (game.getBoard().addRoad(coordinate, player.getPlayerId())) {
                turnTokenService.builtRoad(player);
                return true;
            }

        }
        //Check if the player has the BUILD action
        else if (player.getRemainingActions().contains(PlayerAction.BUILD) && !(player.getRemainingActions()
                .contains(PlayerAction.MOVE_ROBBER) || player.getRemainingActions().contains(PlayerAction.ROAD_BUILDING))) {
            //Check for a road coordinate
            if (coordinate.getCardDir() != CardDir.NONE) {
                if (boardService.addRoadToBoard(game.getBoard(), player, coordinate)) {
                    updateLongestRoad(game);
                    return true;
                }
                return false;

            }
            //Check for a settlement coordinate
            else if (coordinate.getDirection() != Direction.NONE) {
                if (boardService.addSettlementToBoard(game.getBoard(), player, coordinate)) {
                    updateLongestRoad(game);
                    return true;
                }
                return false;

            } else {
                log.error(String.format("Received a settlement coordinate but should have been a road coordinate: %s", coordinate));
            }
        } else {
            log.error(String.format("Game[%s]: player %d tried to play out of turn", game.getId(), player.getPlayerId()));
        }
        return false;
    }

    public void endGame(Game game){
        game.setGameState(GameState.FINISHED);
        for (Player player : game.getPlayers()){
            player.getRemainingActions().clear();
        }
    }

    /**
     * Checks if player has the roll token and rolls the right amount of dice
     *
     * @param game   the entire game object.
     * @param player the current player who's starting his turn.
     * @return the array of thrown dice.
     */
    public int[] rollDice(Game game, Player player) {

        if (player.getRemainingActions().contains(PlayerAction.ROLL) && !(player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER))) {

            int[] rolls = diceService.roll();
            int total = Arrays.stream(rolls).sum();

            processDiceRoll(game, player, total);

            return rolls;

        }
        return null;

    }

    /**
     * Processes the outcome of the dice roll, grants resources and moveRobber/discardResources actions accordingly
     *
     * @param game   the entire game object.
     * @param player the current player who's starting his turn.
     * @param roll   the number the dice roll resulted in (1-12).
     */
    public void processDiceRoll(Game game, Player player, int roll) {
        if(roll == 7){
            turnTokenService.rolledRobber(game, player);
        } else {

            List<Player> players = game.getPlayers();

            //Get all resources that the players have earned by rolling the dice
            Map<Integer, List<Resource>> playerResources = boardService.getResourcesForRoll(game.getBoard(), roll, players);

            //Give every player his earned resources
            for (Map.Entry<Integer, List<Resource>> entry : playerResources.entrySet()) {

                //Get the player corresponding with the id in the hashmap, gotten with a filter in case list or map isn't sorted equally
                players.stream().filter(p -> p.getPlayerId() == entry.getKey()).findFirst().ifPresent(p -> p.addResources(entry.getValue()));
            }
            turnTokenService.rolled(game, player);
        }
    }

    /**
     * This method is called to build a city
     *
     * @param game          the the current game state
     * @param player        the current player who's starting his turn
     * @param coordinate    the coordinate where the settlement/road is wanted to be placed
     * @return whether the placement was successful
     */
    public boolean upgradeSettlementToCity(Game game, Player player, Coordinate coordinate) {
        return boardService.upgradeSettlementToCity(game.getBoard(), player, coordinate);
    }

    /**
     * This method updates who holds the longest road
     *
     * Note to others, if you want to make changes, study all edge cases,
     * or better, contact me: Vink Van den Bosch
     *
     * Here, there be dragons.
     */
    protected int updateLongestRoad(Game game) {
        int prevLongestRoadHolder = game.getPlayerIdWithLongestRoad();
        int newLongestRoadHolder = prevLongestRoadHolder;
        int longestRoad = 0;
        int[] longestRoadPerPlayer = new int[game.getPlayers().size()];

        //calculate the current holder's road length first, to compare others to
        if (prevLongestRoadHolder != 0) {
            int longestRoadForPrevHolder = boardService.getLongestRoadForPlayer(game.getBoard(), prevLongestRoadHolder);
            longestRoad = longestRoadForPrevHolder;
            longestRoadPerPlayer[prevLongestRoadHolder - 1] = longestRoadForPrevHolder;
        }

        //filter out all players that aren't the current holder
        List<Player> playersToTest = game.getPlayers().stream()
                .filter(player -> player.getPlayerId() != prevLongestRoadHolder)
                .collect(Collectors.toList());


        //filter out all players that don't even have enough roads to get longest road
        int finalLongestRoad = longestRoad;
        playersToTest = playersToTest.stream()
                .filter(p -> game.getBoard().getRoads().stream()
                        .filter(road -> road.getPlayerId() == p.getPlayerId())
                        .count() > finalLongestRoad)
                .collect(Collectors.toList());


        //calculate all viable players their road lengths
        for (Player player : playersToTest) {
            longestRoadPerPlayer[player.getPlayerId() - 1] = boardService.getLongestRoadForPlayer(game.getBoard(), player.getPlayerId());
        }

        //check all found lengths and check if someone has a road longer than the current holder
        for (int i = 1; i <= longestRoadPerPlayer.length; i++) {
            //current holder will take priority over others
            if (i == prevLongestRoadHolder) {
                if (longestRoadPerPlayer[i - 1] >= longestRoad) {
                    newLongestRoadHolder = i;
                    longestRoad = longestRoadPerPlayer[i - 1];
                }
            } else {
                if (longestRoadPerPlayer[i - 1] > longestRoad) {
                    newLongestRoadHolder = i;
                    longestRoad = longestRoadPerPlayer[i - 1];
                }
            }
        }

        //if no one has a road longer or equal to 5, longest road gets taken away
        if (longestRoad < 5) {
            newLongestRoadHolder = 0;
            longestRoad = 0;
        }

        //if a new longestRoadHolder is detected, move card over to new holder
        if (prevLongestRoadHolder != newLongestRoadHolder) {
            //take achievement and score away from previous holder
            if (prevLongestRoadHolder != 0) {
                game.getPlayers().stream().filter(player -> player.getPlayerId() == prevLongestRoadHolder).findFirst().ifPresent(
                        player -> {
                            player.getAchievements().remove(Achievement.LONGEST_ROAD);
                            player.decreaseVictoryPoints(2);
                        }
                );
            }
            //if there is no tie detected, give the new holder his card
            int finalLongestRoad1 = longestRoad;
            if (Arrays.stream(longestRoadPerPlayer).filter(i -> i == finalLongestRoad1).count() == 1) {
                game.setPlayerIdWithLongestRoad(newLongestRoadHolder);

                //Add 2 victory point for new holder
                int finalNewLongestRoadHolder = newLongestRoadHolder;
                game.getPlayers().stream().filter(player -> player.getPlayerId() == finalNewLongestRoadHolder).findFirst().ifPresent(
                        player -> {
                            player.getAchievements().add(Achievement.LONGEST_ROAD);
                            player.increaseVictoryPoints(2);
                        }
                );
                return newLongestRoadHolder;
            }
            //if a tie has been detected or if longest road is shorter than 4, do not give the achievement or victorypoints to anyone
            else {
                game.setPlayerIdWithLongestRoad(0);
            }
        }
        return 0;
    }

    /**
     * Players can buy development cards using this method
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @return returns the updated  player or null if error
     */
    public boolean buyCard(Game game, Player player) {

        if (player.getRemainingActions().contains(PlayerAction.BUY)) {

            return cardService.buyCard(game, player);
        } else {
            log.warn(String.format("Game[%s]: player %d tried to buy a card out of turn", game.getId(), player.getPlayerId()));
        }
        return false;
    }

    /**
     * This method is called when a player wants to play any card.
     * If the player has the PLAY_CARD token, the requests gets handed to the cardService which will handle the rest of the request.
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @param cardType the type of progress card that the player wants to play
     * @return true if successful
     */
    public boolean playCard(Game game, Player player, ProgressCardType cardType) {
        boolean result;
        if (player.getRemainingActions().contains(PlayerAction.PLAY_CARD) && !(player.getRemainingActions()
                .contains(PlayerAction.MOVE_ROBBER) || player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES))) {

            switch (cardType) {
                case KNIGHT:
                    result = cardService.playKnightCard(game, player);
                    if (result) {
                        turnTokenService.playedKnight(player);
                    }
                    break;
                case VICTORY_POINT:
                    result = cardService.playVictoryPointCard(game, player);
                    break;
                case YEAR_OF_PLENTY:
                    result = cardService.playYearOfPlentyCard(game, player);
                    break;
                case MONOPOLY:
                    result = cardService.playMonopoly(game, player);
                    break;
                case ROAD_BUILDING:
                    result = cardService.playRoadBuildingCard(game, player);
                    break;
                default:
                    return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a development card, but he doesn't have the correct token"
                    , game.getId(), player.getPlayerId()));
            return false;
        }
        if (result) {
            player.getRemainingActions().remove(PlayerAction.PLAY_CARD);
        }
        return result;
    }

    /**
     * this method is called when a player tries to receive their year of plenty resources.
     * player needs to have the YOP token.
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @param resource1 the first resource the player wants
     * @param resource2 the second resource the player wants
     * @return true if successful
     */
    public boolean yearOfPlenty(Game game, Player player, Resource resource1, Resource resource2) {
        if (player.getRemainingActions().contains(PlayerAction.YOP)) {
            player.getRemainingActions().remove(PlayerAction.YOP);

            List<Resource> resources = new ArrayList<>();
            resources.add(resource1);
            resources.add(resource2);
            player.addResources(resources);
            return true;
        }
        log.warn(String.format("Game[%s]: player %d tried to select year of plenty resources, but did not have the YOP token."
                , game.getId(), player.getPlayerId()));
        return false;
    }

    /**
     * this method is called when a player tries to receive their year of plenty resources.
     * player needs to have the YOP token.
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @param resource  the resource the player wants
     * @return true if successful
     */
    public boolean monopoly(Game game, Player player, Resource resource) {
        if (player.getRemainingActions().contains(PlayerAction.MONOPOLY)) {
            player.getRemainingActions().remove(PlayerAction.MONOPOLY);
            int amountTaken = 0;

            for (Player gamePlayer : game.getPlayers()) {
                if (gamePlayer.getPlayerId() != player.getPlayerId()) {
                    amountTaken += gamePlayer.removeForMonopoly(resource);
                }
            }
            player.addResources(resource, amountTaken);
            return true;
        }
        log.warn(String.format("Game[%s]: player %d tried to select monopoly resource, but did not have the monopoly token."
                , game.getId(), player.getPlayerId()));
        return false;
    }

    /**
     * The player completes his action to remove a certain amount of resources
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @param resources the resources the player discard.
     */
    public boolean discardResources(Game game, Player player,  Map<Resource, Integer> resources){
        if (player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)) {

            int amountOfResources = resources.values().stream().reduce(Integer::sum).orElse(0);

            if (amountOfResources == (int) Math.floor((double) player.getResourcesTotal() / 2.0)) {
                if (player.removeResources(resources)) {
                    player.getRemainingActions().remove(PlayerAction.DISCARD_RESOURCES);
                    return true;
                } else {
                    throw new IllegalArgumentException(
                            String.format("Game[%s]: Player %d tried to discard more resources of a certain type, than what he owned",
                                    game.getId(),
                                    player.getPlayerId()
                            ));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("Game[%s]: Player %d tried to discard %d resources, but was expected to discard %d",
                                game.getId(),
                                player.getPlayerId(),
                                amountOfResources,
                                (int) Math.floor((double) player.getResourcesTotal() / 2.0)
                        ));
            }
        } else {
            throw new IllegalStateException(messageGenerator.generateException(
                    String.format("Game[%s]: Received an illegal request to access method: discardResources(%s,%d,%s)",
                            game.getId(),
                            game.getId(),
                            player.getPlayerId(),
                            resources
                    )));
        }
    }

    /**
     * The current player is allowed to move the robber.
     *
     * @param game      the current state of the game
     * @param player    the current player
     * @return a list of players you can steal from, is null if none available
     */
    public List<Integer> moveRobber(Game game, Player player, Coordinate coordinate) {
        //Check if the player is allowed to move the robber and doesn't has to discard
        if (player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER) && !player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)) {

            Coordinate strippedCoordinate = new Coordinate(coordinate.getX(), coordinate.getY(), coordinate.getZ()); //done this way to prevent directions from making it through
            Tile tileAtCoordinate = game.getBoard().getTileForCoordinate(strippedCoordinate);

            //Check if the new coordinate is the same as the old coordinate
            if (strippedCoordinate.equals(game.getBoard().getRobberTile().getCoordinate())) {
                log.info(String.format("Game[%s]: Player %d tried to move robber to its current location", game.getId(), player.getPlayerId()));
            }
            //Check if it tile exist or a water tile
            else if (tileAtCoordinate == null || tileAtCoordinate.getTileType().equals(TileType.WATER)) {
                log.info(String.format("Game[%s]: Player %d tried to drown the robber", game.getId(), player.getPlayerId()));
            } else {
                //TODO: checked in the first if
                if (!turnTokenService.movedRobber(game, player)) {
                    return null;
                }
                game.getBoard().setRobberTile(tileAtCoordinate);

                //Get all player id's from the players who have a settlement or city adjacent to the new robber tile
                List<Integer> affectedPlayers = boardService.getPlayersInRobberReach(game.getBoard()).stream()
                        .filter(p -> !p.equals(player.getPlayerId())).collect(Collectors.toList());

                //Check if the player can steal from a player
                if (!affectedPlayers.isEmpty()) {
                    player.getRemainingActions().add(PlayerAction.STEAL);
                }

                return affectedPlayers;
            }
            return null;
        } else {
            log.warn("Game[{}]: Received an illegal request to access method: moveRobber({},{},{})", game.getId(),
                    game.getId(), player.getPlayerId(), coordinate.toString());
            throw new IllegalStateException(messageGenerator.generateException(
                    String.format("Player %d tried to move the robber, but wasn't allowed!", player.getPlayerId())));
        }
    }

    /**
     * This method steals a random resource from one player and transfers it to another
     *
     * @param game              the current state of the game
     * @param player            the current player who's starting his turn.
     * @param playerToStealFrom the player we're stealing from.
     * @return The resource that has been transferred. In case front end would like to display this
     */
    public Resource stealResources(Game game, Player player, Player playerToStealFrom) {

        if(player.getRemainingActions().contains(PlayerAction.STEAL) && boardService.getPlayersInRobberReach(game.getBoard()).contains(playerToStealFrom.getPlayerId())){

            Resource resource = playerService.transferRandomResource(player, playerToStealFrom);

            //no else, the player loses his STEAL if opposing player has no resources
            turnTokenService.stoleResource(player);
            return resource;

        } else {
            log.warn("Game[{}]: Received an illegal request to access method: stealResources({},{},{})", game.getId(),
                    game.getId(), player.getPlayerId(), playerToStealFrom.getPlayerId());
            throw new IllegalStateException(messageGenerator.generateException(String.format(
                    "Player %d tried to steal from Player %d, but wasn't allowed!", player.getPlayerId(), playerToStealFrom.getPlayerId())));
        }

    }

    /**
     * This method steals a specific resource from one player and transfers it to another
     * This method is used by the ai to create chance nodes
     *
     * @param game              the current state of the game
     * @param player            the current player who's starting his turn.
     * @param playerToStealFrom the player we're stealing from.
     */
    public void stealResourcesNotRandom(Game game, Player player, Player playerToStealFrom, Resource resource) {

        if(player.getRemainingActions().contains(PlayerAction.STEAL) && boardService.getPlayersInRobberReach(game.getBoard()).contains(playerToStealFrom.getPlayerId())){

            //no else, the player loses his STEAL if opposing player has no resources
            turnTokenService.stoleResource(player);
            player.addResources(resource,1);

        } else {
            log.warn("Game[{}]: Received an illegal request to access method: stealResources({},{},{})", game.getId(),
                    game.getId(), player.getPlayerId(), playerToStealFrom.getPlayerId());
            throw new IllegalStateException(messageGenerator.generateException(String.format(
                    "Player %d tried to steal from Player %d, but wasn't allowed!", player.getPlayerId(), playerToStealFrom.getPlayerId())));
        }

    }
}