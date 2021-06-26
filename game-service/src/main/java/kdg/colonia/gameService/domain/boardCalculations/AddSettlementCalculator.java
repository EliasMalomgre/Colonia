package kdg.colonia.gameService.domain.boardCalculations;

import kdg.colonia.gameService.domain.Road;
import kdg.colonia.gameService.domain.Settlement;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.tiles.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * this class wil calculate whether the placement of a settlement is valid.
 * the game rules dictate that there should always be one empty settlement spot between 2 settlements.
 */
public class AddSettlementCalculator {

    public static boolean calculatePossiblePlacement(int playerId, Coordinate coordinate, List<Settlement> settlements,
                                                     List<Road> roads, boolean isInitialPlacement) {
        //When coordinate is not an intersection
        if (coordinate.getDirection() == Direction.NONE) {
            return false;
        }

        //initial placements don't have to be next to a road
        if (isInitialPlacement) {
            //Settlements
            List<Coordinate> coordinatesToCheckForEmpty = new ArrayList<>(coordinate.calculateConnectedVerticesForVertex());
            coordinatesToCheckForEmpty.add(coordinate); //current coordinate

            //See if other settlement already exists on the location
            return checkCoordinatesForEmpty(coordinatesToCheckForEmpty, settlements);

        } else {
            //Settlements
            List<Coordinate> coordinatesToCheckForEmpty = new ArrayList<>(coordinate.calculateConnectedVerticesForVertex());
            coordinatesToCheckForEmpty.add(coordinate); //current coordinate

            //See if other settlement already exists on the location
            if (!checkCoordinatesForEmpty(coordinatesToCheckForEmpty, settlements)) {
                return false;
            }

            //Roads
            return checkCoordinatesForPlayer(coordinate.calculateConnectedEdgesForVertex(), roads, playerId);
        }
    }

    /**
     * Get all possible coordinates where a player can place a settlement
     *
     * @param playerId           player to check
     * @param settlements        all settlements on the board
     * @param roads              all roads on the board
     * @param tiles              all the tiles on the board
     * @param isInitialPlacement if it
     * @return a list of all the valid coordinates
     */
    public static List<Coordinate> getAllPossibleSettlementPlacements(int playerId, List<Settlement> settlements, List<Road> roads,
                                                                      List<Tile> tiles, boolean isInitialPlacement) {
        List<Coordinate> possibleCoordinates = new ArrayList<>();

        if (isInitialPlacement) {
            List<Coordinate> coordinatesToCheck = new ArrayList<>();

            for (Tile tile : tiles.stream().filter(t -> t.getTileType() != TileType.WATER).collect(Collectors.toList())) {
                coordinatesToCheck.addAll(tile.getCoordinate().calculateVertices());
            }
            coordinatesToCheck = coordinatesToCheck.stream().distinct().collect(Collectors.toList());

            for (Settlement settlement : settlements) {
                Coordinate settlementCoordinate = settlement.getCoordinate();
                coordinatesToCheck.removeAll(settlementCoordinate.calculateConnectedVerticesForVertex());
                coordinatesToCheck.remove(settlementCoordinate);
            }
            possibleCoordinates = coordinatesToCheck;

            return possibleCoordinates;
        } else {
            List<Coordinate> coordinatesToCheck = new ArrayList<>();
            roads.stream().filter(road -> road.getPlayerId() == playerId).forEach(r -> coordinatesToCheck.addAll(r.getCoordinate().calculateConnectedVerticesForEdge()));

            for (Coordinate coordinate : coordinatesToCheck.stream().distinct().collect(Collectors.toList())) {
                if (calculatePossiblePlacement(playerId, coordinate, settlements, roads, false)) {
                    possibleCoordinates.add(coordinate);
                }
            }

            return possibleCoordinates;
        }
    }

    /**
     * Returns all the coordinates where a player is allowed to build a city
     *
     * @param playerId    player to check
     * @param settlements all settlements on the board
     * @return all the coordinates where a player is allowed to build a city
     */
    public static List<Coordinate> getValidCityPlacements(int playerId, List<Settlement> settlements) {
        return settlements.stream().filter(settlement -> !settlement.isCity() && settlement.getPlayerId() == playerId)
                .map(Settlement::getCoordinate).collect(Collectors.toList());
    }

    private static boolean checkCoordinatesForEmpty(List<Coordinate> coordinatesToCheckForEmpty, List<Settlement> settlements) {
        //All of these should be empty
        for (Coordinate c : coordinatesToCheckForEmpty) {
            Optional<Settlement> se = settlements.stream().filter(s -> s.getCoordinate().equals(c)).findFirst();
            if (se.isPresent()) {
                //there is already a settlement on this location
                return false;
            }
        }
        return true;
    }

    private static boolean checkCoordinatesForPlayer(List<Coordinate> coordinatesToCheckForPlayer, List<Road> roads, int playerId) {
        //One of these must contain a road owned by the player
        for (Coordinate c : coordinatesToCheckForPlayer) {
            Optional<Road> se = roads.stream().filter(s -> s.getCoordinate().equals(c)).findFirst();
            if (se.isPresent()) {
                if (se.get().getPlayerId() == playerId) {
                    return true;
                }
            }
        }
        return false;
    }
}
