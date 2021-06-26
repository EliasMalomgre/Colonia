package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.boardCalculations.AddRoadCalculator;
import kdg.colonia.gameService.domain.boardCalculations.AddSettlementCalculator;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
public class Board {
    private ArrayList<Tile> tiles;
    private ArrayList<Harbour> harbours;
    private ArrayList<Road> roads;
    private ArrayList<Settlement> settlements;
    private Tile robberTile;

    public Board() {
        tiles = new ArrayList<>();
        harbours = new ArrayList<>();
        roads = new ArrayList<>();
        settlements = new ArrayList<>();
        robberTile = new Tile();
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param board the board to copy
     */
    public Board(Board board) {
        this.tiles = board.getTiles().stream().map(Tile::new).collect(Collectors.toCollection(ArrayList::new));
        this.harbours = board.getHarbours().stream().map(Harbour::new).collect(Collectors.toCollection(ArrayList::new));
        this.roads = board.getRoads().stream().map(Road::new).collect(Collectors.toCollection(ArrayList::new));
        this.settlements = board.getSettlements().stream().map(Settlement::new).collect(Collectors.toCollection(ArrayList::new));
        this.robberTile = new Tile(board.getRobberTile());
    }

    /**
     * This method gets all settlements surrounding a certain tile
     * @param coordinateOfTile the coordinate of the tile we want to inspect
     * @return settlements found around the tile
     */
    public List<Settlement> getSettlementsForTile(Coordinate coordinateOfTile){
        List<Settlement> foundSettlements = new ArrayList<>();
        for (Coordinate coordinate : coordinateOfTile.calculateVertices()) {
            settlements.stream().filter(s -> s.getCoordinate().equals(coordinate)).findFirst().ifPresent(foundSettlements::add);
        }
        return foundSettlements;
    }

    public Tile getTileForCoordinate(Coordinate coordinateOfTile){
        return tiles.stream().filter(t -> t.getCoordinate().equals(coordinateOfTile)).findFirst().orElse(null);
    }

    /**
     * This method adds a settlement to a certain coordinate, linked to a player
     * @param coordinate the coordinate where the settlement will be place
     * @param playerId the player who is building the settlement
     * @return whether the build has succeeded
     */
    public boolean addSettlement(Coordinate coordinate, int playerId){
        //Check if placement is valid following the catan rules.
        if(!AddSettlementCalculator.calculatePossiblePlacement(playerId, coordinate,settlements, roads, isInitialPhase(playerId))) return false;
        //Create settlement for player.
        Settlement newSettlement = new Settlement(coordinate,playerId);
        settlements.add(newSettlement);
        return true;
    }

    public boolean addRoad(Coordinate coordinate, int playerId){
        if (!AddRoadCalculator.calculatePossiblePlacement(playerId, coordinate, roads, settlements)) return false;
        Road newRoad = new Road(coordinate, playerId);
        roads.add(newRoad);
        return true;
    }

    /**
     * checks if the player is in the initialisation phase of the game.
     * this is determined by whether the player already owns 2 roads.
     * @param playerId the playerId in the game
     * @return true if the player is still in init phase, otherwise false
     */
    public boolean isInitialPhase(int playerId){
        return this.getRoads().stream().filter(s -> s.getPlayerId() == playerId).count() < 2;
    }

    /**
     * This method adds a settlement to a certain coordinate, linked to a player
     * @param coordinate the coordinate where the settlement will be upgraded
     * @param playerId the player who is upgrading the settlement
     * @return whether upgrade has succeeded
     */
    public boolean upgradeSettlement(Coordinate coordinate, int playerId){
        //Check if a settlement exists on this location
        Settlement settlement = settlements.stream().filter(s->s.getCoordinate().equals(coordinate)&&s.getPlayerId()==playerId)
                .findFirst().orElse(null);
        if(settlement==null){
            return false;
        }
        //Upgrade settlement to city
        settlement.setCity(true);
        return true;
    }

    public List<Tile> getTilesAroundSettlement(Coordinate coordinate) {
        List<Tile> tilesAroundSettlement = new ArrayList<>();

        List<Coordinate> tileCoordinates = coordinate.tilesForVertex();

        tiles.forEach(tile -> {
            if (tileCoordinates.contains(tile.getCoordinate())){
                tilesAroundSettlement.add(tile);
            }
        });

        return tilesAroundSettlement;
    }
}
