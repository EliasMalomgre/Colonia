package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.devCard.ProgressCardType;

public class PlayCardDTO {
    public final String gameId;
    public final int playerId;
    public final ProgressCardType cardType;

    public PlayCardDTO(String gameId, int playerId, ProgressCardType cardType) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.cardType = cardType;
    }
}
