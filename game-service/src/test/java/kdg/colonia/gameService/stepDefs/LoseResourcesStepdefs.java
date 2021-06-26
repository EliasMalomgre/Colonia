package kdg.colonia.gameService.stepDefs;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoseResourcesStepdefs {

    private Player player;

    @Given("the player exists")
    public void thePlayerExists() {
        player = new Player();
    }

    @And("the player has {string}, {string}, {string}, {string} resource cards")
    public void thePlayerHasResourceCards(String r1, String r2, String r3, String r4) {
        List<Resource> resources = Stream.of(r1, r2, r3, r4).map(Resource::valueOf).collect(Collectors.toList());
        player.addResources(resources);

    }

    @When("the player loses {string}, {string} resource cards")
    public void thePlayerLosesResourceCards(String r1, String r2) {
        List<Resource> resources = Stream.of(r1, r2).map(Resource::valueOf).collect(Collectors.toList());
        player.removeResources(resources);
    }

    @Then("the player has {string}, {string}")
    public void thePlayerHas(String r1, String r2) {
        List<Resource> resources = Stream.of(r1, r2).map(Resource::valueOf).collect(Collectors.toList());
        Assertions.assertEquals(player.getBricks(), resources.stream().filter(resource -> resource == Resource.BRICK).count());
        Assertions.assertEquals(player.getGrain(), resources.stream().filter(resource -> resource == Resource.GRAIN).count());
        Assertions.assertEquals(player.getLumber(), resources.stream().filter(resource -> resource == Resource.LUMBER).count());
        Assertions.assertEquals(player.getOre(), resources.stream().filter(resource -> resource == Resource.ORE).count());
        Assertions.assertEquals(player.getWool(), resources.stream().filter(resource -> resource == Resource.WOOL).count());
    }



    @And("the player has {string}, {string}, {string} resource cards")
    public void thePlayerHasResourceCards(String r1, String r2, String r3) {
        List<Resource> resources = Stream.of(r1, r2, r3).map(Resource::valueOf).collect(Collectors.toList());
        player.addResources(resources);

    }

    @When("the player loses {string}, resource cards")
    public void thePlayerLosesResourceCards(String r1) {
        List<Resource> resources = Stream.of(r1).map(Resource::valueOf).collect(Collectors.toList());
        player.removeResources(resources);
    }

    @Then("the player has {string}")
    public void thePlayerHas(String r1) {
        List<Resource> resources = Stream.of(r1).map(Resource::valueOf).collect(Collectors.toList());
        Assertions.assertNotEquals(player.getBricks(), resources.stream().filter(resource -> resource == Resource.BRICK).count());
        Assertions.assertEquals(player.getGrain(), resources.stream().filter(resource -> resource == Resource.GRAIN).count());
        Assertions.assertEquals(player.getLumber(), resources.stream().filter(resource -> resource == Resource.LUMBER).count());
        Assertions.assertNotEquals(player.getOre(), resources.stream().filter(resource -> resource == Resource.ORE).count());
        Assertions.assertNotEquals(player.getWool(), resources.stream().filter(resource -> resource == Resource.WOOL).count());
    }
}
