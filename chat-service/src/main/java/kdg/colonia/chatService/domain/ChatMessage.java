package kdg.colonia.chatService.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private long chatNumber;
    private String playerId;
    private String playerName;
    private LocalDateTime timeSent;
    private String message;

    public ChatMessage(String playerId, String playerName, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.message = message;
        this.timeSent = LocalDateTime.now();
    }
}
