package kdg.colonia.userService.repository;

import kdg.colonia.userService.models.Lobby;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LobbyRepository extends MongoRepository<Lobby,String>
{
}
