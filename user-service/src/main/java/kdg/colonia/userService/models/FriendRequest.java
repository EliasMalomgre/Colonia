package kdg.colonia.userService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
public class FriendRequest
{
    @Id
    private String id;
    private String askingUserId;
    private String receivingUserId;

    public FriendRequest(){}
    public FriendRequest(String askingUserId, String receivingUserId)
    {
        this.askingUserId = askingUserId;
        this.receivingUserId = receivingUserId;
    }
}
