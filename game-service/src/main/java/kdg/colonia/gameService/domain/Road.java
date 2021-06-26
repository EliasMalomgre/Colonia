package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Road {
    private Coordinate coordinate;
    private int playerId;

    public Road(Coordinate coordinate, int playerId) {
        this.coordinate = coordinate;
        this.playerId = playerId;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param road  the road to copy
     */
    public Road(Road road) {
        this.coordinate = road.getCoordinate();
        this.playerId = road.getPlayerId();
    }
}
