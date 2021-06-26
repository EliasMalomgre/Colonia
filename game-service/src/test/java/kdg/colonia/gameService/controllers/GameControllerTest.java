package kdg.colonia.gameService.controllers;

import io.cucumber.java.bs.A;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.GameState;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameService gameService;
    @MockBean
    private GameRepository gameRepository;

    @Test
    public void GetResourcesForPlayerInExistingGame() throws Exception
    {
        Player player=new Player();
        player.setPlayerId(1);
        player.addResources(2,2,1,1,1);
        Game game=new Game();
        game.setPlayers(new ArrayList<>());
        game.getPlayers().add(player);
        game.setGameState(GameState.ACTIVE);

        given(gameRepository.findById(any())).willReturn(java.util.Optional.of(game));

        this.mockMvc.perform(get("/getResourcesForPlayer?gameId=test&playerId="+player.getPlayerId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void GetResourcesForPlayerInNonExistingGame() throws Exception
    {
        given(gameRepository.findById(any())).willReturn(java.util.Optional.empty());

        this.mockMvc.perform(get("/getResourcesForPlayer?gameId=test&playerId=0"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}