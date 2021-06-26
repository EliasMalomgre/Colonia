package kdg.colonia.gameService.domain.coordinates;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Slf4j
public class Coordinate {
    private int x;
    private int y;
    private int z;
    private CardDir cardDir;
    private Direction direction;

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Coordinate)) {
            return false;
        }
        // typecast o to Coordinate so that we can compare data members
        Coordinate c = (Coordinate) o;
        // Compare the data members and return accordingly
        return x == c.x
                && y == c.y
                && z == c.z
                && cardDir == c.cardDir
                && direction == c.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, cardDir, direction);
    }

    public Coordinate() {
    }

    /**
     * Constructor for a tile coordinate
     *
     * @param x the X-value of the coordinate (top-left to bottom-right)
     * @param y the Y-value of the coordinate (top-right to bottom-left)
     * @param z the Z-value of the coordinate (horizontal axis)
     */
    public Coordinate(int x, int y, int z) {
        this(x, y, z, CardDir.NONE, Direction.NONE);
    }

    /**
     * constructor for a road coordinate
     *
     * @param x       the X-value of the coordinate (top-left to bottom-right)
     * @param y       the Y-value of the coordinate (top-right to bottom-left)
     * @param z       the Z-value of the coordinate (horizontal axis)
     * @param cardDir the cardinal direction which defines the horizontal side of the hex
     */
    public Coordinate(int x, int y, int z, CardDir cardDir) {
        this(x, y, z, cardDir, Direction.NONE);
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param coordinate the coordinate to copy
     */
    public Coordinate(Coordinate coordinate) {
        this.x = coordinate.getX();
        this.y = coordinate.getY();
        this.z = coordinate.getZ();
        this.direction = coordinate.getDirection();
        this.cardDir = coordinate.getCardDir();
    }

    /**
     * constructor for a settlement coordinate
     *
     * @param x         the X-value of the coordinate (top-left to bottom-right)
     * @param y         the Y-value of the coordinate (top-right to bottom-left)
     * @param z         the Z-value of the coordinate (horizontal axis)
     * @param direction the direction which defines the tip of the hex
     */
    public Coordinate(int x, int y, int z, Direction direction) {
        this(x, y, z, CardDir.NONE, direction);
    }

    //this constructor is for the directional functions (see below) and shouldn't be used by users
    private Coordinate(int x, int y, int z, CardDir cardDir, Direction direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cardDir = cardDir;
        this.direction = direction;
        if (coordinateCheck()) {
            throw new IllegalArgumentException(String.format("The given coordinate is invalid for: x: %d y: %d z: %d", this.x, this.y, this.z));
        } else if ((cardDir != null && cardDir != CardDir.NONE) && (direction != null && direction != Direction.NONE)) {
            throw new IllegalArgumentException(String.format("The given coordinate is invalid as it contains both a cardinal direction and a direction: carDir: %s direction: %s", cardDir, direction));
        }
    }

    /**
     * The sum of all coordinates should always be 0, otherwise the coordinate is invalid
     *
     * @return true if the coordinate is valid
     */
    private boolean coordinateCheck() {
        return this.x + this.y + this.z != 0;
    }

    //directional functions

    /**
     * This function returns the coordinate of the tile to the right of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the right, with or without cardinal direction and/or direction
     */
    public Coordinate getRightTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x + 1, this.y - 1, this.z, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the tile to the left of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the left, with or without cardinal direction and/or direction
     */
    public Coordinate getLeftTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x - 1, this.y + 1, this.z, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the tile to the top-left of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the top-left, with or without cardinal direction and/or direction
     */
    public Coordinate getTopLeftTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x, this.y + 1, this.z - 1, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the tile to the top-right of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the top-right, with or without cardinal direction and/or direction
     */
    public Coordinate getTopRightTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x + 1, this.y, this.z - 1, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the tile to the bottom-left of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the bottom-left, with or without cardinal direction and/or direction
     */
    public Coordinate getBottomLeftTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x - 1, this.y, this.z + 1, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the tile to the bottom-right of the tile of the given coordinate
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile to the bottom-right, with or without cardinal direction and/or direction
     */
    public Coordinate getBottomRightTile(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x, this.y - 1, this.z + 1, cardDir, direction);
    }

    /**
     * This function returns the coordinate of the given tile, you can use this to add or change the cardinal direction and/or the direction
     *
     * @param cardDir   cardinal direction to add to the coordinate, can be null
     * @param direction direction to add to the coordinate, can be null
     * @return the coordinate of the tile, with or without cardinal direction and/or direction
     */
    public Coordinate getCurrentTileCoord(CardDir cardDir, Direction direction) {
        if (cardDir == null) cardDir = CardDir.NONE;
        if (direction == null) direction = Direction.NONE;
        return new Coordinate(this.x, this.y, this.z, cardDir, direction);
    }

    /**
     * This function returns all vertex coordinates of the tile coordinate. This is used for settlements.
     *
     * @return list of coordinates for all the vertices of the tile
     */
    public List<Coordinate> calculateVertices() {
        if (cardDir != CardDir.NONE || direction != Direction.NONE) {
            log.warn("getVerticesForTile was called for none-tile coordinate");
        }

        List<Coordinate> vertices = new ArrayList<>();
        vertices.add(new Coordinate(x, y, z, Direction.LEFT)); //pos 6 -> one to the left of top most corner
        vertices.add(new Coordinate(x, y, z, Direction.TOP)); //pos 1 -> top most corner of the hexagon, go clockwise
        vertices.add(new Coordinate(x + 1, y - 1, z, Direction.LEFT)); //pos 2
        vertices.add(new Coordinate(x, y - 1, z + 1, Direction.TOP)); //pos 3
        vertices.add(new Coordinate(x, y - 1, z + 1, Direction.LEFT)); //pos 4
        vertices.add(new Coordinate(x - 1, y, z + 1, Direction.TOP)); //pos 5
        return vertices;
    }

    /**
     * This function returns all edge coordinates of the tile coordinate. This is used for roads.
     *
     * @return list of coordinates for all the edges of the tile
     */
    public List<Coordinate> calculateEdges() {
        if (cardDir != CardDir.NONE || direction != Direction.NONE) {
            log.warn("getEdgesForTile was called for none-tile coordinate");
        }

        List<Coordinate> edges = new ArrayList<>();
        edges.add(new Coordinate(x, y, z, CardDir.WEST));
        edges.add(new Coordinate(x, y, z, CardDir.NORTH_WEST));
        edges.add(new Coordinate(x, y, z, CardDir.NORTH_EAST));
        edges.add(new Coordinate(x + 1, y - 1, z, CardDir.WEST));
        edges.add(new Coordinate(x, y - 1, z + 1, CardDir.NORTH_WEST));
        edges.add(new Coordinate(x - 1, y, z + 1, CardDir.NORTH_EAST));
        return edges;
    }

    //Returns all the edge coordinates around a road
    public List<Coordinate> calculateConnectedEdgesForEdge() {
        List<Coordinate> coordinatesToCheck = new ArrayList<>();

        switch (getCardDir()) {
            case NONE:
                log.warn("Not an edge coordinate, no edge coordinates have been returned");
                break;
            case WEST:
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_WEST, null)); //top-right
                coordinatesToCheck.add(getBottomLeftTile(CardDir.NORTH_EAST, null)); //bottom-right
                coordinatesToCheck.add(getBottomLeftTile(CardDir.NORTH_WEST, null)); //bottom-left
                coordinatesToCheck.add(getLeftTile(CardDir.NORTH_EAST, null)); //top-left
                break;
            case NORTH_WEST:
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_EAST, null)); //right
                coordinatesToCheck.add(getTopRightTile(CardDir.WEST, null)); //top
                coordinatesToCheck.add(getLeftTile(CardDir.NORTH_EAST, null)); //left
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.WEST, null)); //bottom
                break;
            case NORTH_EAST:
                coordinatesToCheck.add(getRightTile(CardDir.NORTH_WEST, null)); //right
                coordinatesToCheck.add(getRightTile(CardDir.WEST, null)); //bottom
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_WEST, null)); //left
                coordinatesToCheck.add(getTopRightTile(CardDir.WEST, null)); //top
                break;
        }

        return coordinatesToCheck;
    }

    //Returns both vertex coordinates around a road
    public List<Coordinate> calculateConnectedVerticesForEdge() {
        List<Coordinate> coordinatesToCheck = new ArrayList<>();

        switch (getCardDir()) {
            case NONE:
                log.warn("Not an edge coordinate, no vertex coordinates have been returned");
                break;
            case WEST:
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.LEFT)); //top
                coordinatesToCheck.add(getBottomLeftTile(null, Direction.TOP)); //bottom
                break;
            case NORTH_WEST:
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.TOP)); //right (top)
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.LEFT)); //left
                break;
            case NORTH_EAST:
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.TOP)); //top
                coordinatesToCheck.add(getRightTile(null, Direction.LEFT)); //right
                break;
        }

        return coordinatesToCheck;
    }

    public List<Coordinate> calculateConnectedEdgesForVertex() {
        List<Coordinate> coordinatesToCheck = new ArrayList<>();

        switch (getDirection()) {
            case NONE:
                log.warn("Not a vertex coordinate, no edge coordinates have been returned");
                break;
            case TOP:
                coordinatesToCheck.add(getTopRightTile(CardDir.WEST, null));
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_WEST, null));
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_EAST, null));
                break;
            case LEFT:
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.WEST, null));
                coordinatesToCheck.add(getCurrentTileCoord(CardDir.NORTH_WEST, null));
                coordinatesToCheck.add(getLeftTile(CardDir.NORTH_EAST, null));
                break;
        }

        return coordinatesToCheck;
    }

    public List<Coordinate> calculateConnectedVerticesForVertex() {
        List<Coordinate> coordinatesToCheck = new ArrayList<>();

        switch (getDirection()) {
            case NONE:
                log.warn("Not a vertex coordinate, no edge coordinates have been returned");
                break;
            case TOP:
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.LEFT)); //Left-lower point
                coordinatesToCheck.add(getTopRightTile(null, Direction.LEFT)); //Upper point
                coordinatesToCheck.add(getRightTile(null, Direction.LEFT)); //Right-lower point
                break;
            case LEFT:
                coordinatesToCheck.add(getCurrentTileCoord(null, Direction.TOP)); //Right-upper point
                coordinatesToCheck.add(getBottomLeftTile(null, Direction.TOP)); //Down point
                coordinatesToCheck.add(getLeftTile(null, Direction.TOP)); //Left-upper point
                break;
        }

        return coordinatesToCheck;
    }

    public List<Coordinate> tilesForVertex() {
        List<Coordinate> tileCoordinates = new ArrayList<>();
        Coordinate copyCoordinate = new Coordinate(this);

        switch (getDirection()) {
            case NONE:
                log.warn("Not a vertex coordinate, no edge coordinates have been returned");
                break;
            case TOP:
                tileCoordinates.add(copyCoordinate.getCurrentTileCoord( CardDir.NONE,  Direction.NONE));
                tileCoordinates.add(copyCoordinate.getTopRightTile( CardDir.NONE,  Direction.NONE));
                tileCoordinates.add(copyCoordinate.getTopLeftTile( CardDir.NONE, Direction.NONE));
                break;
            case LEFT:
                tileCoordinates.add(copyCoordinate.getCurrentTileCoord( CardDir.NONE, Direction.NONE));
                tileCoordinates.add(copyCoordinate.getLeftTile( CardDir.NONE, Direction.NONE));
                tileCoordinates.add(copyCoordinate.getTopLeftTile( CardDir.NONE, Direction.NONE));
                break;
        }

        return tileCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(")
                .append(x)
                .append(",")
                .append(y)
                .append(",")
                .append(z);

        if (!cardDir.equals(CardDir.NONE)){
            sb.append(",CRD:");
            switch (cardDir){
                case WEST:
                    sb.append("W");
                    break;
                case NORTH_WEST:
                    sb.append("NW");
                    break;
                case NORTH_EAST:
                    sb.append("NE");
                    break;
            }
        }
        else if (!direction.equals(Direction.NONE)){
            sb.append(",DIR:");
            switch (direction){
                case TOP:
                    sb.append("T");
                    break;
                case LEFT:
                    sb.append("L");
                    break;
            }
        }

        sb.append(")");

        return sb.toString();
    }
}
