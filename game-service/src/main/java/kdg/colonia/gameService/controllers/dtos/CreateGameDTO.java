package kdg.colonia.gameService.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreateGameDTO {
    List<String> userIds;
    int amountOfAIs;
    String userIdOfHost;
}
