package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDTO
{
    private String gameId;
    private int asking;
    private int receiving;
    private Map<Resource,Integer> toSend;
    private Map<Resource, Integer> toReceive;
}
