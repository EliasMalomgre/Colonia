package kdg.colonia.gameService.config.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    //metrics to decide best branch with
    private int winScore;
    private boolean useOtherMetrics;
    private int victoryPointScore;
    private int vpLeaderBonus;
    private int longestRoadBonus;

    //settings to alter monte carlo algorithm
    private long simulationTime;
    private double learningRate;
    private int ongoingGame;
    private int searchDepth;
    private boolean lessTimeFewActions;
    private long fewActionsSimulationTime;
    private int fewActions;
    private boolean longerSimulationInitial;
    private long initialSettlementSimulationTime;
    private long initialRoadSimulationTime;
    private boolean onlyRandomMoves;
    private boolean useChanceNodes;
    private boolean useGroupNodes;
    private boolean randomDiscards;

    //settings used to prevent heap dumps
    private boolean garbageCollectStartSimulation;
    private boolean garbageCollectEndSimulation;
    private boolean garbageCollectMidSimulation;
    private int garbageCollectFrequency;
    private int waitTimeMidSimulation;
    private int waitTimeBetweenMoves;
}
