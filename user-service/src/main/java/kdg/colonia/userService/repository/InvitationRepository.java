package kdg.colonia.userService.repository;

import kdg.colonia.userService.models.Invitation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvitationRepository extends MongoRepository<Invitation,String>
{
    public List<Invitation> getInvitationsByInvitedUserId(String userId);
}
