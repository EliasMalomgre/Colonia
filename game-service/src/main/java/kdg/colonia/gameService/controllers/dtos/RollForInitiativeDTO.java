package kdg.colonia.gameService.controllers.dtos;

public class RollForInitiativeDTO {
    public final String gameId;
    public final String userId;

    public RollForInitiativeDTO(String gameId, String userId) {
        this.gameId = gameId;
        this.userId = userId;
    }
}
