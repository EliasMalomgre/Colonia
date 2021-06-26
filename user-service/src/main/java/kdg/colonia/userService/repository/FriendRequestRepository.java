package kdg.colonia.userService.repository;

import kdg.colonia.userService.models.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendRequestRepository extends MongoRepository<FriendRequest,String>
{
    public List<FriendRequest> getFriendRequestByReceivingUserId(String receivingUserId);
}
