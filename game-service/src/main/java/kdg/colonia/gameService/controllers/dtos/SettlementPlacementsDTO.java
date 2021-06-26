package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SettlementPlacementsDTO {
    List<Coordinate> possibleSettlementCoordinates;
}
