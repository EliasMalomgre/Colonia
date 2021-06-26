package kdg.colonia.gameService.domain.coordinates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {
    Coordinate baseCoord;

    @BeforeEach
    void setUp(){
        baseCoord = new Coordinate(0,0,0);
    }

    @Test
    void getRightTile() {
        assertEquals(new Coordinate(1,-1,0),baseCoord.getRightTile(null,null));
        assertEquals(new Coordinate(1,-1,0, Direction.LEFT),baseCoord.getRightTile(null,Direction.LEFT));
        assertEquals(new Coordinate(1,-1,0, CardDir.WEST),baseCoord.getRightTile(CardDir.WEST,null));
    }

    @Test
    void getLeftTile() {
        assertEquals(new Coordinate(-1,1,0),baseCoord.getLeftTile(null,null));
        assertEquals(new Coordinate(-1,1,0, Direction.LEFT),baseCoord.getLeftTile(null,Direction.LEFT));
        assertEquals(new Coordinate(-1,1,0, CardDir.WEST),baseCoord.getLeftTile(CardDir.WEST,null));
    }

    @Test
    void getTopLeftTile() {
        assertEquals(new Coordinate(0,1,-1),baseCoord.getTopLeftTile(null,null));
        assertEquals(new Coordinate(0,1,-1, Direction.LEFT),baseCoord.getTopLeftTile(null,Direction.LEFT));
        assertEquals(new Coordinate(0,1,-1, CardDir.WEST),baseCoord.getTopLeftTile(CardDir.WEST,null));
    }

    @Test
    void getTopRightTile() {
        assertEquals(new Coordinate(1,0,-1),baseCoord.getTopRightTile(null,null));
        assertEquals(new Coordinate(1,0,-1, Direction.LEFT),baseCoord.getTopRightTile(null,Direction.LEFT));
        assertEquals(new Coordinate(1,0,-1, CardDir.WEST),baseCoord.getTopRightTile(CardDir.WEST,null));
    }

    @Test
    void getBottomLeftTile() {
        assertEquals(new Coordinate(-1,0,1),baseCoord.getBottomLeftTile(null,null));
        assertEquals(new Coordinate(-1,0,1, Direction.LEFT),baseCoord.getBottomLeftTile(null,Direction.LEFT));
        assertEquals(new Coordinate(-1,0,1, CardDir.WEST),baseCoord.getBottomLeftTile(CardDir.WEST,null));
    }

    @Test
    void getBottomRightTile() {
        assertEquals(new Coordinate(0,-1,1),baseCoord.getBottomRightTile(null,null));
        assertEquals(new Coordinate(0,-1,1, Direction.LEFT),baseCoord.getBottomRightTile(null,Direction.LEFT));
        assertEquals(new Coordinate(0,-1,1, CardDir.WEST),baseCoord.getBottomRightTile(CardDir.WEST,null));
    }

    @Test
    void getCurrentTileCoord() {
        assertEquals(new Coordinate(0,0,0),baseCoord.getCurrentTileCoord(null,null));
        assertEquals(new Coordinate(0,0,0, Direction.LEFT),baseCoord.getCurrentTileCoord(null,Direction.LEFT));
        assertEquals(new Coordinate(0,0,0, CardDir.WEST),baseCoord.getCurrentTileCoord(CardDir.WEST,null));
    }
}