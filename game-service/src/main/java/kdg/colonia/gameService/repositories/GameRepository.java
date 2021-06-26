package kdg.colonia.gameService.repositories;

import kdg.colonia.gameService.domain.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game,String>
{
}
