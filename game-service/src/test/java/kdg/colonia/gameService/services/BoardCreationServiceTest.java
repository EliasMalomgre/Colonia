package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.BoardCreationConfig;
import kdg.colonia.gameService.domain.Board;
import kdg.colonia.gameService.domain.Harbour;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.tiles.TileType;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardCreationServiceTest {
    @Autowired
    BoardCreationService boardCreationService;
    @Autowired
    BoardCreationConfig boardCreationConfig;

    Board board;

    @BeforeEach
    public void Before() {
        this.board = boardCreationService.generate();
    }

    /**
     * tests if the correct amount of tiles of each type have been generated
     */
    @Test
    public void testTileTypes() {
        int normalTiles = boardCreationConfig.getFieldAmount() + boardCreationConfig.getForestAmount() + boardCreationConfig.getPlainsAmount() + boardCreationConfig.getHillsAmount() + boardCreationConfig.getMountainAmount() + boardCreationConfig.getDesertAmount();
        int waterTiles = boardCreationConfig.getTotalAmountOfTiles() - normalTiles;
        assertEquals(waterTiles, board.getTiles().stream().filter(t -> t.getTileType() == TileType.WATER).count());
        assertEquals(boardCreationConfig.getPlainsAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.PLAINS).count());
        assertEquals(boardCreationConfig.getForestAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.FOREST).count());
        assertEquals(boardCreationConfig.getHillsAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.HILLS).count());
        assertEquals(boardCreationConfig.getMountainAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.MOUNTAINS).count());
        assertEquals(boardCreationConfig.getFieldAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.FIELD).count());
        assertEquals(boardCreationConfig.getDesertAmount(), board.getTiles().stream().filter(t -> t.getTileType() == TileType.DESERT).count());
        assertEquals(boardCreationConfig.getTotalAmountOfTiles(), board.getTiles().size());
    }

    /**
     * tests if all the numbers have been assigned to the correct type of tile.
     * also checks if the correct amount of each number has been assigned.
     */
    @Test
    public void testTileNumbers() {
        for (Tile tile : board.getTiles()) {
            if (tile.getTileType() == TileType.WATER || tile.getTileType() == TileType.DESERT) {
                assertEquals(0, tile.getNumber());
            } else {
                assertNotEquals(0, tile.getNumber());
            }
        }

        assertEquals(boardCreationConfig.getTwoAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 2).count());
        assertEquals(boardCreationConfig.getThreeAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 3).count());
        assertEquals(boardCreationConfig.getFourAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 4).count());
        assertEquals(boardCreationConfig.getFiveAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 5).count());
        assertEquals(boardCreationConfig.getSixAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 6).count());
        assertEquals(boardCreationConfig.getEightAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 8).count());
        assertEquals(boardCreationConfig.getNineAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 9).count());
        assertEquals(boardCreationConfig.getTenAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 10).count());
        assertEquals(boardCreationConfig.getElevenAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 11).count());
        assertEquals(boardCreationConfig.getTwelveAmount(), board.getTiles().stream().filter(t -> t.getNumber() == 12).count());
    }

    /**
     * checks if the correct amount of each type of harbour have been generated
     */
    @Test
    public void testHarbours() {
        assertEquals(boardCreationConfig.getGenericHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.NOTHING).count());
        assertEquals(boardCreationConfig.getWoolHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.WOOL).count());
        assertEquals(boardCreationConfig.getBrickHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.BRICK).count());
        assertEquals(boardCreationConfig.getLumberHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.LUMBER).count());
        assertEquals(boardCreationConfig.getGrainHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.GRAIN).count());
        assertEquals(boardCreationConfig.getOreHarbourAmount(), board.getHarbours().stream().filter(h -> h.getResource() == Resource.ORE).count());
    }

    /**
     * checks first, middle and last coordinate
     */
    @Test
    public void coordinateCheck() {
        assertEquals(new Coordinate(0, 3, -3), board.getTiles().get(0).getCoordinate());
        assertEquals(new Coordinate(0, 0, 0), board.getTiles().get(18).getCoordinate());
        assertEquals(new Coordinate(0, -3, 3), board.getTiles().get(board.getTiles().size() - 1).getCoordinate());
    }

    /**
     * prints the generated board and harbours
     */
    @Test
    public void visualTest() {
        for (Tile tile : board.getTiles()) {
            System.out.printf("%d %s - %d -  [%d,%d,%d]\n", tile.getIndex(), tile.getTileType().toString(), tile.getNumber(), tile.getCoordinate().getX(), tile.getCoordinate().getY(), tile.getCoordinate().getZ());
        }
        System.out.println("\n\n");
        for (Harbour harbour : board.getHarbours()) {
            System.out.printf("%s - %d:1 - [%d,%d,%d,%s]\n", harbour.getResource().toString(), harbour.getRatio(), harbour.getCoordinate().getX(), harbour.getCoordinate().getY(), harbour.getCoordinate().getZ(), harbour.getCoordinate().getCardDir());
        }
    }
}