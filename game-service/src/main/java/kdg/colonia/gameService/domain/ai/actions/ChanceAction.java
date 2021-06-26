package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChanceAction implements Action {
    private final double probability;

    @Override
    public Game performAction(Game game) {
        return null;
    }
}
