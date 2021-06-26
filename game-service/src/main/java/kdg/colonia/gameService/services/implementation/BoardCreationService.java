package kdg.colonia.gameService.services.implementation;

import kdg.colonia.gameService.config.game.BoardCreationConfig;
import kdg.colonia.gameService.domain.Board;
import kdg.colonia.gameService.domain.Harbour;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.domain.tiles.TileType;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.services.IBoardCreationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class BoardCreationService implements IBoardCreationService {
    private final BoardCreationConfig boardCreationConfig;

    /**
     * This method will generate a random board based on the values in application.properties.
     * The amount of each tile can be modified as long as the total stays the same.
     * There should always be 9 harbours, the amount of each type of harbour is customisable.
     * The water tiles and locations of harbours are not customisable.
     *
     * @return a playable board.
     */
    @Override
    public Board generate() {
        Board board = new Board();
        ArrayList<Tile> tilesToAdd = new ArrayList<>();
        ArrayList<Integer> numbersToAssign = new ArrayList<>();
        ArrayList<Harbour> harboursToAdd = new ArrayList<>();
        ArrayList<Coordinate> harbourCoordsToAssign = new ArrayList<>();

        //Create the correct amount tiles from each type
        for (int i = 0; i < boardCreationConfig.getFieldAmount(); i++) {
            Tile field = new Tile();
            field.setTileType(TileType.FIELD);
            tilesToAdd.add(field);
        }

        for (int i = 0; i < boardCreationConfig.getForestAmount(); i++) {
            Tile forest = new Tile();
            forest.setTileType(TileType.FOREST);
            tilesToAdd.add(forest);
        }

        for (int i = 0; i < boardCreationConfig.getPlainsAmount(); i++) {
            Tile plains = new Tile();
            plains.setTileType(TileType.PLAINS);
            tilesToAdd.add(plains);
        }

        for (int i = 0; i < boardCreationConfig.getHillsAmount(); i++) {
            Tile hills = new Tile();
            hills.setTileType(TileType.HILLS);
            tilesToAdd.add(hills);
        }

        for (int i = 0; i < boardCreationConfig.getMountainAmount(); i++) {
            Tile mountains = new Tile();
            mountains.setTileType(TileType.MOUNTAINS);
            tilesToAdd.add(mountains);
        }

        for (int i = 0; i < boardCreationConfig.getDesertAmount(); i++) {
            Tile desert = new Tile();
            desert.setTileType(TileType.DESERT);
            tilesToAdd.add(desert);
        }

        //Create the correct amount of number tokens for tiles
        for (int i = 0; i < boardCreationConfig.getTwoAmount(); i++) {
            numbersToAssign.add(2);
        }

        for (int i = 0; i < boardCreationConfig.getThreeAmount(); i++) {
            numbersToAssign.add(3);
        }

        for (int i = 0; i < boardCreationConfig.getFourAmount(); i++) {
            numbersToAssign.add(4);
        }

        for (int i = 0; i < boardCreationConfig.getFiveAmount(); i++) {
            numbersToAssign.add(5);
        }

        for (int i = 0; i < boardCreationConfig.getSixAmount(); i++) {
            numbersToAssign.add(6);
        }

        for (int i = 0; i < boardCreationConfig.getEightAmount(); i++) {
            numbersToAssign.add(8);
        }

        for (int i = 0; i < boardCreationConfig.getNineAmount(); i++) {
            numbersToAssign.add(9);
        }

        for (int i = 0; i < boardCreationConfig.getTenAmount(); i++) {
            numbersToAssign.add(10);
        }

        for (int i = 0; i < boardCreationConfig.getElevenAmount(); i++) {
            numbersToAssign.add(11);
        }

        for (int i = 0; i < boardCreationConfig.getTwelveAmount(); i++) {
            numbersToAssign.add(12);
        }

        //Radomize tiles and number tokens
        Collections.shuffle(numbersToAssign);
        Collections.shuffle(tilesToAdd);

        //Put all tile on the board

        //top row
        for (int x = 0, y = 3, i = 0; x <= 3; x++, y--, i++) {
            //z coord stays the same in a row
            int z = -3;

            Tile waterTile = new Tile();
            waterTile.setTileType(TileType.WATER);
            waterTile.setNumber(0);
            waterTile.setCoordinate(new Coordinate(x, y, z));
            waterTile.setIndex(i);
            board.getTiles().add(waterTile);
        }

        //board center
        {
            //these numbers will adapt for each tile, moving right
            int x = -1;
            int y = 3;
            int z = -2;
            //these numbers serve to reset the coords to the start of the row
            int xBase = -1;
            int yBase = 3;
            int zBase = -2;

            //index: the index of the tile (unique number)
            //x, y, z: coordinate
            //i: index in the tilesToAdd list
            //number: index in the numbersToAssign list
            for (int index = 4, i = 0, number = 0; index < 33; index++) {
                //left water tile
                if ((y == 3 && x != -3) || (x == -3 && y != 3) || (y == 3 && x == -3 && z == 0)) {
                    Tile waterTile = new Tile();
                    waterTile.setTileType(TileType.WATER);
                    waterTile.setNumber(0);
                    waterTile.setCoordinate(new Coordinate(x, y, z));
                    waterTile.setIndex(index);
                    board.getTiles().add(waterTile);
                    //move right
                    x++;
                    y--;
                }
                //right water tile
                else if ((x == 3 && y != -3) || (x != 3 && y == -3) || (x == 3 && y == -3 && z == 0)) {
                    Tile waterTile = new Tile();
                    waterTile.setTileType(TileType.WATER);
                    waterTile.setNumber(0);
                    waterTile.setCoordinate(new Coordinate(x, y, z));
                    waterTile.setIndex(index);
                    board.getTiles().add(waterTile);
                    //top half of the board
                    if (y != -3) {
                        xBase = xBase - 1;
                        zBase = zBase + 1;

                        x = xBase;
                        y = yBase;
                        z = zBase;
                    }
                    //bottom half of the board
                    else {
                        yBase = yBase - 1;
                        zBase = zBase + 1;

                        x = xBase;
                        y = yBase;
                        z = zBase;
                    }
                }
                //resource tile
                else {
                    Tile tile = tilesToAdd.get(i);
                    if (tile.getTileType() != TileType.DESERT) {
                        tile.setNumber(numbersToAssign.get(number));
                        number++;
                    }else{
                        board.setRobberTile(tile);
                    }
                    tile.setCoordinate(new Coordinate(x, y, z));
                    tile.setIndex(index);
                    board.getTiles().add(tile);
                    i++;
                    //move right
                    x++;
                    y--;
                }
            }
        }

        //bottom row
        for (int x = -3, y = 0, i = 33; x <= 0; x++, y--, i++) {
            //z coord stays the same in a row
            int z = 3;

            Tile waterTile = new Tile();
            waterTile.setTileType(TileType.WATER);
            waterTile.setNumber(0);
            waterTile.setCoordinate(new Coordinate(x, y, z));
            waterTile.setIndex(i);
            board.getTiles().add(waterTile);
        }

        //HARBOURS

        //Configure all harbours
        for (int i = 0; i < boardCreationConfig.getWoolHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(2);
            harbour.setResource(Resource.WOOL);
            harboursToAdd.add(harbour);
        }
        for (int i = 0; i < boardCreationConfig.getBrickHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(2);
            harbour.setResource(Resource.BRICK);
            harboursToAdd.add(harbour);
        }
        for (int i = 0; i < boardCreationConfig.getLumberHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(2);
            harbour.setResource(Resource.LUMBER);
            harboursToAdd.add(harbour);
        }
        for (int i = 0; i < boardCreationConfig.getOreHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(2);
            harbour.setResource(Resource.ORE);
            harboursToAdd.add(harbour);
        }
        for (int i = 0; i < boardCreationConfig.getGrainHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(2);
            harbour.setResource(Resource.GRAIN);
            harboursToAdd.add(harbour);
        }
        for (int i = 0; i < boardCreationConfig.getGenericHarbourAmount(); i++) {
            Harbour harbour = new Harbour();
            harbour.setRatio(3);
            harbour.setResource(Resource.NOTHING);
            harboursToAdd.add(harbour);
        }

        //Create harbour coordinates
        harbourCoordsToAssign.add(new Coordinate(0, 2, -2, CardDir.NORTH_WEST));
        harbourCoordsToAssign.add(new Coordinate(1, 1, -2, CardDir.NORTH_WEST));
        harbourCoordsToAssign.add(new Coordinate(2, -1,-1, CardDir.NORTH_EAST));
        harbourCoordsToAssign.add(new Coordinate(3, -3,0, CardDir.WEST));
        harbourCoordsToAssign.add(new Coordinate(1, -3,2, CardDir.NORTH_WEST));
        harbourCoordsToAssign.add(new Coordinate(-1, -2,3, CardDir.NORTH_WEST));
        harbourCoordsToAssign.add(new Coordinate(-3, 0,3, CardDir.NORTH_EAST));
        harbourCoordsToAssign.add(new Coordinate(-2, 1,1, CardDir.WEST));
        harbourCoordsToAssign.add(new Coordinate(-1, 2,-1, CardDir.WEST));

        //Randomize placements
        Collections.shuffle(harbourCoordsToAssign);
        Collections.shuffle(harboursToAdd);

        //Place harbours
        for (int i = 0; i < 9; i++) {
            Harbour harbour = harboursToAdd.get(i);
            harbour.setCoordinate(harbourCoordsToAssign.get(i));
            board.getHarbours().add(harbour);
        }

        return board;
    }
}
