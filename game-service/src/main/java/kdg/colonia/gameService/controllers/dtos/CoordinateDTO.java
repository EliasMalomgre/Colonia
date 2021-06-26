package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.coordinates.Coordinate;

public class CoordinateDTO {
    public final String gameId;
    public final int playerId;
    public final Coordinate coordinate;

    public CoordinateDTO(String gameId, int playerId, Coordinate coordinate) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.coordinate = coordinate;
    }
}
