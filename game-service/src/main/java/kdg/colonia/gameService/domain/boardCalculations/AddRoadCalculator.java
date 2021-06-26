package kdg.colonia.gameService.domain.boardCalculations;

import kdg.colonia.gameService.domain.Road;
import kdg.colonia.gameService.domain.Settlement;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class calculates whether the placement of a road is valid.
 * There should be at least one city or road adjacent to the to be placed road.
 */
public class AddRoadCalculator {

    /**
     * Calculate if a player is allowed to place a road at a specific coordinate
     *
     * @param playerId    the player to check
     * @param coordinate  the coordinate to check
     * @param roads       all the roads on the board
     * @param settlements all the settlements on the board
     * @return a boolean whether it is allowed to place the road at the coordinate
     */
    public static boolean calculatePossiblePlacement(int playerId, Coordinate coordinate, List<Road> roads, List<Settlement> settlements) {
        //See if side has another settlement/road of the same player next to it
        if (roads.stream().anyMatch(r -> r.getCoordinate().equals(coordinate))) {
            return false;
        }

        if (coordinate.getCardDir() != CardDir.NONE) {

            List<Settlement> playerSettlements = settlements.stream().filter(s -> s.getPlayerId() == playerId).collect(Collectors.toList());

            //Settlements/cities
            for (Coordinate settlementCoord : coordinate.calculateConnectedVerticesForEdge()) {
                if (playerSettlements.stream().anyMatch(s -> s.getCoordinate().equals(settlementCoord))) {
                    return true;
                }
            }

            List<Road> playerRoads = roads.stream().filter(r -> r.getPlayerId() == playerId).collect(Collectors.toList());
            //Roads
            for (Coordinate roadCoord : coordinate.calculateConnectedEdgesForEdge()) {
                if (playerRoads.stream().anyMatch(r -> r.getCoordinate().equals(roadCoord))) {
                    return true;
                }
            }
        }
        //When coordinate is not an edge
        return false;
    }

    /**
     * Calculates all the placements where a player is allowed to build a road
     *
     * @param playerId           the player to check
     * @param roads              all the roads on the board
     * @param settlements        all the settlements on the board
     * @param isInitialPlacement whether it is during the starting phase
     * @return a list with all the possible coordinates
     */
    public static List<Coordinate> calculateAllPossiblePlacements(int playerId, List<Road> roads, List<Settlement> settlements, boolean isInitialPlacement) {
        List<Coordinate> possibleCoordinates = new ArrayList<>();

        if (isInitialPlacement) {
            List<Settlement> playerSettlements = settlements.stream().filter(settlement -> settlement.getPlayerId() == playerId).collect(Collectors.toList());
            Set<Coordinate> playerRoadsCoordinates = roads.stream().filter(road -> road.getPlayerId() == playerId).map(Road::getCoordinate).collect(Collectors.toSet());

            for (Settlement settlement : playerSettlements) {
                Coordinate settlementCoordinate = settlement.getCoordinate();

                //Get all the coordinates for roads around a settlement
                List<Coordinate> coordinatesToCheck = settlementCoordinate.calculateConnectedEdgesForVertex();

                //Checks if there are roads around the settlement
                if (coordinatesToCheck.stream().noneMatch(playerRoadsCoordinates::contains)) {
                    possibleCoordinates.addAll(coordinatesToCheck);
                    possibleCoordinates.removeIf(AddRoadCalculator::isWaterRoad);
                    return possibleCoordinates;
                }
            }
        }

        //Checks around each road if there is an empty coordinate available
        roads.stream().filter(road -> road.getPlayerId() == playerId).forEach(road -> {
            Coordinate coordinate = road.getCoordinate();
            List<Coordinate> coordinatesToCheck = new ArrayList<>();

            if (coordinate.getCardDir() != CardDir.NONE) {
                coordinatesToCheck = coordinate.calculateConnectedEdgesForEdge();
            }

            coordinatesToCheck.forEach(c -> {
                if (roads.stream().noneMatch(r -> r.getCoordinate().equals(c))) {
                    if (!possibleCoordinates.contains(c) && !isWaterRoad(c)) {
                        possibleCoordinates.add(c);
                    }
                }
            });
        });

        return possibleCoordinates;
    }

    private static boolean isWaterRoad(Coordinate roadCoordinate) {
        //it can never be a water road if the coordinate doesn't contain a 3 or -3
        if (Math.abs(roadCoordinate.getX()) != 3 && Math.abs(roadCoordinate.getY()) != 3 && Math.abs(roadCoordinate.getZ()) != 3) {
            return false;
        }
        //it is always a water road if z = -3 or y = 3
        if (roadCoordinate.getZ() == -3 || roadCoordinate.getY() == 3) {
            return true;
        }

        //specific cases

        if (roadCoordinate.getCardDir().equals(CardDir.WEST) && (roadCoordinate.getZ() == 3 || roadCoordinate.getX() == -3)) {
            return true;
        }

        if (roadCoordinate.getCardDir().equals(CardDir.NORTH_WEST) && (roadCoordinate.getX() == 3 || roadCoordinate.getX() == -3)) {
            return true;
        }

        if (roadCoordinate.getCardDir().equals(CardDir.NORTH_EAST) && (roadCoordinate.getY() == -3 || roadCoordinate.getX() == 3)) {
            return true;
        }

        return false;
    }
}
