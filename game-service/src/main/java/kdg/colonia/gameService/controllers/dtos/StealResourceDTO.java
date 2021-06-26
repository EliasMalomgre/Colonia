package kdg.colonia.gameService.controllers.dtos;

public class StealResourceDTO {
    public final String gameId;
    public final int playerId;
    public final int playerIdToStealFrom;

    public StealResourceDTO(String gameId, int playerId, int playerIdToStealFrom) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerIdToStealFrom = playerIdToStealFrom;
    }
}
