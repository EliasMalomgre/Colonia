package kdg.colonia.gameService.services.gameInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CostObject
{
    private final String objName;
    private final int lumberCost;
    private final int grainCost;
    private final int brickCost;
    private final int woolCost;
    private final int oreCost;
}
