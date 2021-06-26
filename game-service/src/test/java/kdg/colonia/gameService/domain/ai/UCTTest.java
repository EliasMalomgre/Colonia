package kdg.colonia.gameService.domain.ai;

import kdg.colonia.gameService.config.game.AiConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UCTTest {

    @Autowired
    AiConfig aiConfig;

    @Autowired
    UCT uct;

    @ParameterizedTest
    @CsvSource({"1,0", "0,1", "1,1"})
    public void correctUCTCalculationNoVisits(int winScore, int totalVisits) {
        assertEquals(Integer.MAX_VALUE, uct.uctValue(totalVisits, winScore,0));
    }

    @ParameterizedTest
    @CsvSource({"10,2,1", "0,5,3", "10,10,1"})
    public void correctUCTCalculation(int winScore, int totalVisits, int nodeVisits) {
        assertEquals(((double) winScore / (double) nodeVisits)
                + aiConfig.getLearningRate() * Math.sqrt(Math.log(totalVisits) / (double) nodeVisits), uct.uctValue(totalVisits,winScore,nodeVisits));
    }

    @Test
    public void correctlyChosenBestChild() {
        Node parent = new Node();
        parent.getState().setVisitCount(10);
        parent.getState().setWinScore(50);

        for (int i = 0; i < 5; i++) {
            Node child = new Node();
            child.getState().setVisitCount(i + 1);
            child.getState().setWinScore(i * 5);
            child.setParent(parent);
            parent.getChildArray().add(child);
        }

        Node bestNode = uct.findBestNodeWithUCT(parent);
        assertEquals(4, parent.getChildArray().indexOf(bestNode));
    }
}