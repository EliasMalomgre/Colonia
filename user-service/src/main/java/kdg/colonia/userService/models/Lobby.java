package kdg.colonia.userService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "lobbies")
@AllArgsConstructor
@Getter
@Setter
public class Lobby
{
    @Id
    private String id;
    private LobbyUser host;
    private List<LobbyUser> lobbyUsers;
    private int amountOfAI;
    private int amountOfHuman;
    public Lobby(){
        this.lobbyUsers=new ArrayList<>();
    }

}
