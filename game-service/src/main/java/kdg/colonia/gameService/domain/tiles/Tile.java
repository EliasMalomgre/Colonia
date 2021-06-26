package kdg.colonia.gameService.domain.tiles;

import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.Resource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tile {
    private int index;
    private Coordinate coordinate;
    private int number;
    private TileType tileType;

    public Tile() {
        this.coordinate = new Coordinate();
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param tile  the tile to copy
     */
    public Tile(Tile tile) {
        this.index = tile.getIndex();
        this.coordinate = tile.getCoordinate();
        this.number = tile.getNumber();
        this.tileType = tile.getTileType();
    }

    public Resource getResourceType(){
        switch (this.tileType){
            case FIELD:
                return Resource.GRAIN;
            case HILLS:
                return Resource.BRICK;
            case FOREST:
                return Resource.LUMBER;
            case PLAINS:
                return Resource.WOOL;
            case MOUNTAINS:
                return Resource.ORE;
            case DESERT:
            case WATER:
                return Resource.NOTHING;
        }
        //this shouldn't happen
        return Resource.NOTHING;
    }

    @Override
    public String toString() {
        return this.getIndex() + " " + this.tileType.toString() +  " " + this.getNumber() + " " + coordinate;
    }
}
