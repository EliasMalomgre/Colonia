package kdg.colonia.gameService.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GameAndIntListDTO {
    private String gameId;
    private List<Integer> list;
}
