package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeWithBankDTO {
    String gameId;
    int playerId;
    Resource from;
    Resource to;
}
