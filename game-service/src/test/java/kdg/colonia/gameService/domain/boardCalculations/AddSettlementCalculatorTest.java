package kdg.colonia.gameService.domain.boardCalculations;

import kdg.colonia.gameService.domain.Road;
import kdg.colonia.gameService.domain.Settlement;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AddSettlementCalculatorTest {

    List<Settlement> settlements;
    List<Road> roads;

    @Autowired
    BoardCreationService boardCreationService;

    @BeforeEach
    public void init(){
        settlements = new ArrayList<>();
        roads = new ArrayList<>();
        Settlement settlementP1 = new Settlement(new Coordinate(-1,0,1, Direction.TOP),1);
        Road roadP1 = new Road(new Coordinate(0,0,0, CardDir.WEST),1);
        Road road2P1 = new Road(new Coordinate(-1,0,1,CardDir.NORTH_WEST),1);
        Settlement settlementP2 = new Settlement(new Coordinate(1,0,-1, Direction.LEFT),2);
        Road roadP2 = new Road(new Coordinate(1,-1,0,CardDir.WEST),2);
        settlements.add(settlementP1);
        settlements.add(settlementP2);
        roads.add(roadP1);
        roads.add(road2P1);
        roads.add(roadP2);
    }

    //Left
    @Test
    public void validPlacementL(){
        Coordinate coordinateToTest = new Coordinate(1,-1,0, Direction.LEFT);
        assertTrue(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    @Test
    public void invalidPlacementLTooCloseToSettlement(){
        Coordinate coordinateToTest = new Coordinate(0,0,0,Direction.LEFT);
        assertFalse(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    @Test
    public void invalidPlacementLNoAdjacentRoad(){
        Coordinate coordinateToTest = new Coordinate(0,1,-1, Direction.LEFT);
        assertFalse(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    //Top
    @Test
    public void validPlacementT(){
        Coordinate coordinateToTest = new Coordinate(0,-1,1, Direction.TOP);
        assertTrue(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    @Test
    public void invalidPlacementTTooCloseToSettlement(){
        Coordinate coordinateToTest = new Coordinate(0,0,0,Direction.TOP);
        assertFalse(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    @Test
    public void invalidPlacementTNoAdjacentRoad(){
        Coordinate coordinateToTest = new Coordinate(1,-1,0, Direction.TOP);
        assertFalse(AddSettlementCalculator.calculatePossiblePlacement(2,coordinateToTest,settlements,roads, false));
    }

    //Get all settlement placements
    //Not starting phase

    @Test
    public void validSettlementPlacements(){
        List<Coordinate> validSettlementPlacements = new ArrayList<>();
        validSettlementPlacements.add(new Coordinate(1,-1,0, Direction.LEFT));
        validSettlementPlacements.add(new Coordinate(0,-1,1, Direction.TOP));

        assertEquals(validSettlementPlacements, AddSettlementCalculator.getAllPossibleSettlementPlacements(2, settlements, roads, null, false));
    }

    @Test
    public void invalidSettlementPlacements(){
        List<Coordinate> validSettlementPlacements = new ArrayList<>();
        validSettlementPlacements.add(new Coordinate(1,-1,0, Direction.LEFT));
        validSettlementPlacements.add(new Coordinate(1,-2,1, Direction.TOP));

        assertNotEquals(validSettlementPlacements, AddSettlementCalculator.getAllPossibleSettlementPlacements(2, settlements, roads, null,false));
    }

    //Starting phase
    @Test
    public void validSettlementPlacementsStartingPhase(){
        List<Tile> tiles = boardCreationService.generate().getTiles();
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(-2,1,1,Direction.TOP));
        possibleCoordinates.add(new Coordinate(0,-3,3,Direction.TOP));
        possibleCoordinates.add(new Coordinate(2,-1,-1,Direction.LEFT));
        possibleCoordinates.add(new Coordinate(-1,1,0,Direction.LEFT));

        List<Coordinate> invalidCoordinates = new ArrayList<>();
        invalidCoordinates.add(new Coordinate(-1,0,1,Direction.TOP));
        invalidCoordinates.add(new Coordinate(-1,0,1,Direction.LEFT));
        invalidCoordinates.add(new Coordinate(1,0,-1,Direction.LEFT));
        invalidCoordinates.add(new Coordinate(1,0,-1,Direction.TOP));

        List<Coordinate> possiblePlacements = AddSettlementCalculator.getAllPossibleSettlementPlacements(2, settlements, roads, tiles, true);
        assertEquals(46, possiblePlacements.size());
        assertTrue(possiblePlacements.containsAll(possibleCoordinates));
        assertFalse(possiblePlacements.containsAll(invalidCoordinates));
    }

    @Test
    public void invalidSettlementPlacementsStartingPhase(){
        List<Tile> tiles = boardCreationService.generate().getTiles();
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(-2,1,1,Direction.TOP));
        possibleCoordinates.add(new Coordinate(0,-3,3,Direction.TOP));
        possibleCoordinates.add(new Coordinate(2,-1,-1,Direction.LEFT));
        possibleCoordinates.add(new Coordinate(1,0,-1,Direction.TOP));

        List<Coordinate> invalidCoordinates = new ArrayList<>();
        invalidCoordinates.add(new Coordinate(-1,0,1,Direction.TOP));
        invalidCoordinates.add(new Coordinate(-1,0,1,Direction.LEFT));
        invalidCoordinates.add(new Coordinate(1,0,-1,Direction.LEFT));
        invalidCoordinates.add(new Coordinate(-1,1,0,Direction.LEFT));


        List<Coordinate> possiblePlacements = AddSettlementCalculator.getAllPossibleSettlementPlacements(2, settlements, roads, tiles, true);
        assertNotEquals(48, possiblePlacements.size());
        assertFalse(possiblePlacements.containsAll(possibleCoordinates));
        assertFalse(possiblePlacements.containsAll(invalidCoordinates));
    }
    //Get all valid city placements
    @Test
    public void validCityPlacements(){
        List<Coordinate> validPlacements =  new ArrayList<>();
        validPlacements.add(new Coordinate(-1,0,1, Direction.TOP));
        assertEquals(validPlacements, AddSettlementCalculator.getValidCityPlacements(1, settlements));
    }

    @Test
    public void invalidCityPlacements(){
        List<Coordinate> invalidPlacements =  new ArrayList<>();
        invalidPlacements.add(new Coordinate(-1,0,1, Direction.TOP));
        assertNotEquals(invalidPlacements, AddSettlementCalculator.getValidCityPlacements(2, settlements));
    }

}