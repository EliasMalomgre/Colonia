package kdg.colonia.chatService.services;

import kdg.colonia.chatService.controllers.dtos.RESTToSocketServerController;
import kdg.colonia.chatService.domain.Chat;
import kdg.colonia.chatService.domain.ChatMessage;
import kdg.colonia.chatService.repositories.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatRepository chatRepository;
    private final RESTToSocketServerController restToSocketServerController;

    public Chat createChat(String gameId){
        Chat chat = new Chat(gameId);
        chat.setAmountOfMessages(0);
        return chatRepository.save(chat);
    }

    public Chat getChat(String gameId){
        return validateGameID(gameId);
    }

    public boolean newMessage(String gameId, String playerId, String playerName, String message){
        Chat chat = validateGameID(gameId);
        ChatMessage chatMessage = new ChatMessage(playerId, playerName, message);
        chatMessage.setChatNumber(chat.getAmountOfMessages());
        chat.getChatMessages().add(chatMessage);
        chat.setAmountOfMessages(chat.getAmountOfMessages()+1);
        chat.getChatMessages().sort(Comparator.comparing(ChatMessage::getTimeSent));
        chatRepository.save(chat);
        restToSocketServerController.sendNewMessageNotice(gameId);
        return true;
    }

    private Chat validateGameID(String gameId){
        Chat chat =  chatRepository.findByGameId(gameId);
        if (chat==null){
            String err = String.format("Game with id[%s] could not be found", gameId);
            log.error(err);
            throw new IllegalArgumentException(err);
        }
        return chat;
    }
}
