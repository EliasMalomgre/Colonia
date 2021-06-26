package kdg.colonia.chatService.repositories;

import kdg.colonia.chatService.domain.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Chat findByGameId(String gameId);
}
