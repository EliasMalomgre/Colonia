package kdg.colonia.gameService.domain.ai;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.ai.actions.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimulationTimeStruct {
    private final Long time;
    private final Game game;
    private final Action action;
}
