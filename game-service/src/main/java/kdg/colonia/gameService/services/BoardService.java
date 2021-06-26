package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.services.gameInfo.CostObject;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final PlayerService playerService;
    private final GameInfoService gameInfoService;
    private final GameConfig gameConfig;

    /**
     * Adds an initial settlement to a board
     *
     * @param board         the current board state
     * @param player        player that wants to place a settlement
     * @param coordinate    the coordinate where the player wants to place a settlement
     * @return whether the placement was successful
     */
    public boolean addInitialSettlementToBoard(Board board, Player player, Coordinate coordinate)
    {
        if (board.addSettlement(coordinate, player.getPlayerId())) {

            //add victory point
            player.increaseVictoryPoints(1);
            return true;
        }
        return false;
    }

    /**
     * Adds an initial road to a board
     *
     * @param board         the current board state
     * @param player        player that wants to place a road
     * @param coordinate    the coordinate where the player wants to place a road
     * @return whether the placement was successful
     */
    public boolean addInitialRoadToBoard(Board board, Coordinate coordinate, Player player) {
        return board.addRoad(coordinate, player.getPlayerId());
    }

    /**
     * Adds a settlement to a board if the placement is valid and resources are available
     * Adds a victory point if the placement was successful
     *
     * @param board         the current board state
     * @param player        player that wants to place a settlement
     * @param coordinate    the coordinate where the player wants to place a settlement
     * @return whether the placement was successful
     */
    public boolean addSettlementToBoard(Board board, Player player, Coordinate coordinate) {
        //Try adding settlement to board
        if (playerService.playerAllowedToBuildSettlement(player) && board.addSettlement(coordinate, player.getPlayerId())) {
            //subtract resources from player
            //Check if player has the available resources
            CostObject settlement = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Settlement"))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Costobject \"Settlement\" could not be found"));

            player.removeResources(settlement.getBrickCost(), settlement.getGrainCost(), settlement.getLumberCost(),
                    settlement.getOreCost(), settlement.getWoolCost());

            //Add a victory point
            player.increaseVictoryPoints(1);
            return true;
        }
        //Settlement could not be added
        return false;
    }

    /**
     * Adds a road to a board if the placement is valid and resources are available
     *
     * @param board         the current board state
     * @param player        player that wants to place a road
     * @param coordinate    the coordinate where the player wants to place a road
     * @return whether the placement was successful
     */
    public boolean addRoadToBoard(Board board, Player player, Coordinate coordinate) {
        //Try adding settlement to board
        if (playerService.playerAllowedToBuildRoad(player) && board.addRoad(coordinate, player.getPlayerId())) {
            //subtract resources from player
            CostObject road = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Road"))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Costobject \"Road\" could not be found"));

            player.removeResources(road.getBrickCost(), road.getGrainCost(), road.getLumberCost(),
                    road.getOreCost(), road.getWoolCost());

            return true;
        }
        //Settlement could not be added
        return false;
    }

    /**
     * Adds a city to a board if the placement is valid and resources are available
     * Adds a victory point if the placement was successful
     *
     * @param board         the current board state
     * @param player        player that wants to place a city
     * @param coordinate    the coordinate where the player wants to place a city
     * @return whether the placement was successful
     */
    public boolean upgradeSettlementToCity(Board board, Player player, Coordinate coordinate) {
        if (playerService.playerAllowedToBuildCity(player) && board.upgradeSettlement(coordinate, player.getPlayerId())) {
            //subtract resources from player
            CostObject road = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("City"))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Costobject \"City\" could not be found"));

            player.removeResources(road.getBrickCost(), road.getGrainCost(), road.getLumberCost(),
                    road.getOreCost(), road.getWoolCost());

            //Add a victory point
            player.increaseVictoryPoints(1);

            return true;
        }
        return false;
    }

    /**
     * Calculates the longest Road for a certain player, utilizing a recursive method that explores every possible path,
     * starting from every possible road
     *
     * @param board    The board containing roads and settlements to inspect
     * @param playerId The player for who we want to find the longest road
     * @return longest road found for given player
     */
    public int getLongestRoadForPlayer(Board board, int playerId) {
        int knownMaximum = 0;

        List<Road> roads = board.getRoads().stream().filter(r -> r.getPlayerId() == playerId).collect(Collectors.toList());
        List<Coordinate> settlementCoords = board.getSettlements().stream().filter(r -> r.getPlayerId() != playerId).map(Settlement::getCoordinate).collect(Collectors.toList());

        //For anyone thinking of using a method to select optimal starting positions, don't, this issue is NP-complete.
        //selecting deadEndRoads for example wouldn't work if there's a 6-circle and a separate 3-path
        //with random selections you risk missing out on one long road, if you have lots of smaller roads

        for (Road road : roads) {
            List<Coordinate> visitedRoads = new ArrayList<>();
            int roadMax = recursiveDepthSearch(0, road.getCoordinate(), null, roads, visitedRoads, settlementCoords);

            if (knownMaximum < roadMax) {
                knownMaximum = roadMax;
            }
        }

        return knownMaximum;
    }

    /**
     * Recursive method that takes a current road and a previous node/vertex to discover possible new branching paths/roads
     * and discovers the one with the longest length, while keeping track of previously visited paths.
     *
     * @param i                      The length of the path before reaching this point
     * @param roadCoord              The road we're now starting from
     * @param prevVertex             The previous node, that we can't return to (to prevent backtracking on a Y)
     * @param roads                  All possible roads we can traverse on
     * @param previouslyVisitedRoads The path we have taken before reaching this point
     * @param settlementCoords       All enemy settlements that we can't traverse through
     * @return the length of the longest possible path starting from roadCoord, without backtracking
     */
    private int recursiveDepthSearch(int i, Coordinate roadCoord, Coordinate prevVertex, List<Road> roads, List<Coordinate> previouslyVisitedRoads, List<Coordinate> settlementCoords) {
        //infinite recursion shouldn't occur, but there's no harm in having a safety check for good measure
        if (i > 1000) {
            log.error("An error occurred causing an infinite recursion while traversing possible paths, this should not happen!");
            return i;
        }

        //make a copy of visitedRoads that we can edit, without referencing the list in a parent method
        List<Coordinate> visitedRoads = new ArrayList<>(previouslyVisitedRoads); //DO NOT CHANGE
        visitedRoads.add(roadCoord);
        int newTotal = i + 1;

        //get all vertices surrounding a road
        List<Coordinate> vertices = roadCoord.calculateConnectedVerticesForEdge();

        //remove previously visited vertex, to prevent backtracking in a Y path
        if (prevVertex != null) {
            vertices.remove(prevVertex);
        }

        //remove node if owned by other player -> breaks longest road
        vertices.removeIf(vertex -> settlementCoords.stream().anyMatch(coordinate -> coordinate.equals(vertex)));

        //there should only be 1 remaining vertex
        for (Coordinate vertex : vertices) {
            //get all edges surrounding a vertex
            List<Coordinate> edges = vertex.calculateConnectedEdgesForVertex();

            //remove edges that don't have a respective road for the player
            edges.removeIf(edge -> roads.stream().noneMatch(c -> c.getCoordinate().equals(edge)));

            //remove all edges that we have visited, this includes the previous roadCoord to prevent backtracking
            edges.removeIf(edge -> visitedRoads.stream().anyMatch(coordinate -> coordinate.equals(edge)));

            //follow every remaining branch/edge
            for (Coordinate edge : edges) {

                //what is the total path length if we follow this branch
                int branchMax = recursiveDepthSearch(i + 1, edge, vertex, roads, visitedRoads, settlementCoords);

                //if our total path length increased by following this edge/branch, increase total
                if (newTotal < branchMax) {
                    newTotal = branchMax;
                }
            }
        }

        //Path ended, no more nodes left to explore
        return newTotal;
    }

    /**
     * Finds all playerId's that have a settlement in the vicinity of the robber.
     *
     * @param board The board we wish to inspect
     * @return      A list of playerId's
     */
    public List<Integer> getPlayersInRobberReach(Board board) {

        List<Integer> affectedPlayers = new ArrayList<>();

        for (Settlement settlement : board.getSettlementsForTile(board.getRobberTile().getCoordinate())) {
            int playerId = settlement.getPlayerId();

            //could've done this with a set or distinct, but a simple if seems more elegant for a max size of 6 id's
            if(!affectedPlayers.contains(playerId)) {
                affectedPlayers.add(playerId);
            }
        }
        return affectedPlayers;
    }

    /**
     * This method will check all tiles that correspond with the dice roll and check for settlements surrounding it.
     * Every settlement's owner will be granted the resource of the tile.
     * If the settlement is a city, double the resources will be granted.
     *
     * @param board     the current state of the board
     * @param roll      the number the dice roll resulted in (1-12), tiles with corresponding number have to be checked.
     * @param players   all players competing in the game
     * @return a map containing a list of resources granted, for each player
     */
    public Map<Integer, List<Resource>> getResourcesForRoll(Board board, int roll, List<Player> players){
        List<Tile> tilesForRoll = board.getTiles().stream().filter(t -> t.getNumber() == roll).collect(Collectors.toList());

        Map<Integer, List<Resource>> playerResources = new HashMap<>();
        for (Player player : players) {
            playerResources.put(player.getPlayerId(), new ArrayList<>());
        }

        for (Tile tile : tilesForRoll) {
            for (Settlement settlement : board.getSettlementsForTile(tile.getCoordinate())) {
                //Get the list for the player his resources
                List<Resource> l = playerResources.get(settlement.getPlayerId());

                int amountGained = gameConfig.getResourceGainSettlement();

                //If settlement is a city, the player gains double resources
                if(settlement.isCity()){
                    amountGained += gameConfig.getBonusIfCity();
                }

                //The tile won't produce resources if it's occupied by a robber
                if(tile.getCoordinate().equals(board.getRobberTile().getCoordinate())){
                    amountGained = 0;
                }

                //Add resources for the player
                for (int i = 0; i < amountGained; i++) {
                    l.add(tile.getResourceType());
                }
            }
        }

        return playerResources;
    }
}
