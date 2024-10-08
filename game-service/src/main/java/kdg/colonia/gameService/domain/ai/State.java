package kdg.colonia.gameService.domain.ai;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.ai.actions.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class State {
    private Game game;
    private Action action;
    private int playerNo;
    private int visitCount;
    private double winScore;
    private double probability;

    public State(State state) {
        this.game = new Game(state.getGame());
        this.playerNo = state.getPlayerNo();
        this.visitCount = state.getVisitCount();
        this.winScore = state.getWinScore();
        this.probability = 0;
        this.action = state.getAction();
    }

    public State(Game game, int playerNo, Action action){
        this.game = game;
        this.action = action;
        this.playerNo = playerNo;
    }

    public State(Game game, int playerNo) {
        this(game, playerNo, null);
    }

    public void incrementVisit() {
        visitCount++;
    }

    public void addScore(double winScore) {
        this.winScore += winScore;
    }
}
