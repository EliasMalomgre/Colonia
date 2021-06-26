package kdg.colonia.chatService.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    private String id;
    private String gameId;
    private long amountOfMessages;

    private List<ChatMessage> chatMessages;

    public Chat(String gameId) {
        this.gameId = gameId;
        this.chatMessages = new LinkedList<>();
    }
}
