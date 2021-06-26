package kdg.colonia.gameService.domain.ai;

import kdg.colonia.gameService.domain.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NodeTest {

    Node node;

    @BeforeEach
    public void setup() {
        node = new Node();
        node.setChildArray(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            Node tempnode = new Node();
            State state = new State(new Game(), 1);
            state.setWinScore(i);
            tempnode.setState(state);
            node.getChildArray().add(tempnode);
        }

        Collections.shuffle(node.getChildArray());
    }

    @Test
    public void selectMaximumCorrectly() {
        Node max = node.getChildWithMaxScore();
        assertEquals(4, max.getState().getWinScore());
    }

    @Test
    public void selectMaximumWrongly() {
        Node max = node.getChildWithMaxScore();
        assertNotEquals(5, max.getState().getWinScore());
    }

}