package kdg.colonia.chatService.controllers;

import kdg.colonia.chatService.controllers.dtos.NewMessageDTO;
import kdg.colonia.chatService.domain.Chat;
import kdg.colonia.chatService.services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @GetMapping("/createChat")
    public ResponseEntity<Chat> createChat(@RequestParam String gameId){
        return ResponseEntity.ok(chatService.createChat(gameId));
    }

    @GetMapping("/getChat")
    public ResponseEntity<Chat> getChat(String gameId){
        try{
            Chat chat = chatService.getChat(gameId);
            return ResponseEntity.ok(chat);
        } catch (Error e){
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/newMessage")
    public ResponseEntity<Boolean> newMessage(@RequestBody NewMessageDTO newMessageDTO){
        try {
            if (chatService.newMessage(newMessageDTO.getGameId(), newMessageDTO.getPlayerId(), newMessageDTO.getPlayerName(), newMessageDTO.getMessage())){
                return ResponseEntity.ok(true);
            }
            return ResponseEntity.status(500).body(false);
        } catch (Error e){
            return ResponseEntity.status(500).body(false);
        }
    }
}
