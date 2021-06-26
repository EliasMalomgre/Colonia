package kdg.colonia.gameService.stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReceivedResourcesStepdefs {
    
    private Player player;
    
    @Given("The player exists")
    public void thePlayerExists() {
        player = new Player();
    }

    @When("The player receives {string}, {string}, {string}, {string} resource cards")
    public void thePlayerReceivesResourceCards(String r1, String r2, String r3, String r4) {
        List<Resource> resources = Stream.of(r1, r2, r3, r4).map(Resource::valueOf).collect(Collectors.toList());
        player.addResources(resources);
    }

    @Then("The player received {string}, {string}, {string}, {string} resource cards")
    public void thePlayerReceivedResourceCards(String r1, String r2, String r3, String r4) {
        List<Resource> resources = Stream.of(r1, r2, r3, r4).map(Resource::valueOf).collect(Collectors.toList());

        Assertions.assertEquals(player.getBricks(), resources.stream().filter(resource -> resource == Resource.BRICK).count());
        Assertions.assertEquals(player.getGrain(), resources.stream().filter(resource -> resource == Resource.GRAIN).count());
        Assertions.assertEquals(player.getLumber(), resources.stream().filter(resource -> resource == Resource.LUMBER).count());
        Assertions.assertEquals(player.getOre(), resources.stream().filter(resource -> resource == Resource.ORE).count());
        Assertions.assertEquals(player.getWool(), resources.stream().filter(resource -> resource == Resource.WOOL).count());

    }

    @When("The player receives {string}, {string}, {string} resource cards")
    public void thePlayerReceivesResourceCards(String r1, String r2, String r3) {
        List<Resource> resources = Stream.of(r1, r2, r3).map(Resource::valueOf).collect(Collectors.toList());
        player.addResources(resources);
    }

    @Then("The player received  {string}, {string}, {string} resource cards")
    public void thePlayerReceivedResourceCards(String r1, String r2, String r3) {
        List<Resource> resources = Stream.of(r1, r2, r3).map(Resource::valueOf).collect(Collectors.toList());

        Assertions.assertEquals(player.getBricks(), resources.stream().filter(resource -> resource == Resource.BRICK).count());
        Assertions.assertEquals(player.getGrain(), resources.stream().filter(resource -> resource == Resource.GRAIN).count());
        Assertions.assertNotEquals(player.getLumber(), resources.stream().filter(resource -> resource == Resource.LUMBER).count());
        Assertions.assertNotEquals(player.getOre(), resources.stream().filter(resource -> resource == Resource.ORE).count());
        Assertions.assertEquals(player.getWool(), resources.stream().filter(resource -> resource == Resource.WOOL).count());
    }
}
