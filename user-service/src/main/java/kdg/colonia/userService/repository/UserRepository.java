package kdg.colonia.userService.repository;

import kdg.colonia.userService.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>
{
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
    Optional<User> findByValidateUserToken(String token);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
