package kdg.colonia.gameService.domain.boardCalculations;

import kdg.colonia.gameService.domain.Road;
import kdg.colonia.gameService.domain.Settlement;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddRoadCalculatorTest {

    List<Settlement> settlements;
    List<Road> roads;

    @BeforeEach
    public void init(){
        settlements = new ArrayList<>();
        roads = new ArrayList<>();
        Settlement settlementP1 = new Settlement(new Coordinate(-1,0,1, Direction.TOP),1);
        Road roadP1 = new Road(new Coordinate(0,0,0, CardDir.WEST),1);
        Road road2P1 = new Road(new Coordinate(-1,0,1,CardDir.NORTH_WEST),1);
        Settlement settlementP2 = new Settlement(new Coordinate(1,0,-1, Direction.LEFT),2);
        settlements.add(settlementP1);
        settlements.add(settlementP2);
        roads.add(roadP1);
        roads.add(road2P1);
    }

    //Test settlements
    @Test
    public void validRoadPlacementWest(){
        Coordinate coordinateToTest = new Coordinate(1,0,-1, CardDir.WEST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementWest(){
        Coordinate coordinateToTest = new Coordinate(0,1,-1, CardDir.WEST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    @Test
    public void validRoadPlacementNW(){
        Coordinate coordinateToTest = new Coordinate(1,0,-1, CardDir.NORTH_WEST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementNW(){
        Coordinate coordinateToTest = new Coordinate(0,1,-1, CardDir.NORTH_WEST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    @Test
    public void validRoadPlacementNE(){
        Coordinate coordinateToTest = new Coordinate(0,1,-1, CardDir.NORTH_EAST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementNE(){
        Coordinate coordinateToTest = new Coordinate(1,0,-1, CardDir.NORTH_EAST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(2,coordinateToTest,roads,settlements));
    }

    //Test roads
    @Test
    public void validRoadPlacementNWRoad(){
        Coordinate coordinateToTest = new Coordinate(0,0,0, CardDir.NORTH_WEST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementNWRoad(){
        Coordinate coordinateToTest = new Coordinate(1,0,-1, CardDir.NORTH_WEST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    @Test
    public void validRoadPlacementNERoad(){
        Coordinate coordinateToTest = new Coordinate(-1,1,0, CardDir.NORTH_EAST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementNERoad(){
        Coordinate coordinateToTest = new Coordinate(0,1,-1, CardDir.NORTH_EAST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    @Test
    public void validRoadPlacementWRoad(){
        Coordinate coordinateToTest = new Coordinate(-1,0,1, CardDir.WEST);
        assertTrue(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    @Test
    public void invalidRoadPlacementWRoad(){
        Coordinate coordinateToTest = new Coordinate(1,0,-1, CardDir.WEST);
        assertFalse(AddRoadCalculator.calculatePossiblePlacement(1,coordinateToTest,roads,settlements));
    }

    //Test all valid road coordinates
    //Not starting phase
    @Test
    public void validPossiblePlacements(){
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(0,0,0, CardDir.NORTH_WEST));
        possibleCoordinates.add(new Coordinate(-1, 0,1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-1,1,0, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-2,1,1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-1, 0,1, CardDir.WEST));

        List<Coordinate> possiblePlacements = AddRoadCalculator.calculateAllPossiblePlacements(1, roads, settlements, false);

        assertTrue(possibleCoordinates.containsAll(possiblePlacements));
        assertEquals(5, possiblePlacements.size());
    }

    @Test
    public void nonValidPossiblePlacements(){
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(-1,2,-1, CardDir.NORTH_WEST));
        possibleCoordinates.add(new Coordinate(0, 1,-1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-1,0,1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-2,1,1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(1, 1,-2, CardDir.WEST));

        List<Coordinate> possiblePlacements = AddRoadCalculator.calculateAllPossiblePlacements(1, roads, settlements, false);

        assertFalse(possibleCoordinates.containsAll(possiblePlacements));
        assertNotEquals(6, possiblePlacements.size());
    }

    //Not starting phase
    @Test
    public void validPossiblePlacementsStartingPhase(){
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(1,0,-1, CardDir.NORTH_WEST));
        possibleCoordinates.add(new Coordinate(1, 0,-1, CardDir.WEST));
        possibleCoordinates.add(new Coordinate(-0,1,-1, CardDir.NORTH_EAST));

        List<Coordinate> possiblePlacements = AddRoadCalculator.calculateAllPossiblePlacements(2, roads, settlements, true);

        assertTrue(possibleCoordinates.containsAll(possiblePlacements));
        assertEquals(3, possiblePlacements.size());
    }

    @Test
    public void nonValidPossiblePlacementsStartingPhase(){
        List<Coordinate> possibleCoordinates = new ArrayList<>();
        possibleCoordinates.add(new Coordinate(-1,2,-1, CardDir.NORTH_WEST));
        possibleCoordinates.add(new Coordinate(0, 1,-1, CardDir.NORTH_EAST));
        possibleCoordinates.add(new Coordinate(-1,0,1, CardDir.NORTH_EAST));


        List<Coordinate> possiblePlacements = AddRoadCalculator.calculateAllPossiblePlacements(2, roads, settlements, true);

        assertFalse(possibleCoordinates.containsAll(possiblePlacements));
        assertNotEquals(6, possiblePlacements.size());

    }


}