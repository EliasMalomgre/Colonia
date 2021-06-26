package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Settlement {
    private Coordinate coordinate;
    private int playerId;
    private boolean isCity = false;

    public Settlement(Coordinate coordinate,int playerId) {
        this.coordinate=coordinate;
        this.playerId=playerId;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param settlement    the settlement to copy
     */
    public Settlement(Settlement settlement) {
        this.coordinate = settlement.getCoordinate();
        this.playerId = settlement.getPlayerId();
        this.isCity = settlement.isCity();
    }
}
