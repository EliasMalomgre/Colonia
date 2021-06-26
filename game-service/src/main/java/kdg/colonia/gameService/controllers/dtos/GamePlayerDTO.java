package kdg.colonia.gameService.controllers.dtos;

public class GamePlayerDTO {
    public final String gameId;
    public final int playerId;

    public GamePlayerDTO(String gameId, int playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }
}
