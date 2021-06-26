package kdg.colonia.chatService.controllers.dtos;

import lombok.Getter;

@Getter
public class NewMessageDTO {
    private String gameId;
    private String playerId;
    private String playerName;
    private String message;
}
