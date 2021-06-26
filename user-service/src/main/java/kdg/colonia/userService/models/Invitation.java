package kdg.colonia.userService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "invitations")
public class Invitation
{
    @Id
    private String id;
    private String lobbyId;
    private String invitedUserId;

    public Invitation(String lobbyId, String invitedUserId)
    {
        this.lobbyId = lobbyId;
        this.invitedUserId = invitedUserId;
    }
}
