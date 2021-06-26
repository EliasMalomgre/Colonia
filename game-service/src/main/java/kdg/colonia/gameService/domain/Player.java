package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.devCard.ProgressCard;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@Slf4j
public class Player {
    //number from 1-4, also decides the play order
    private int playerId;
    private String userId;
    private int victoryPointsAmount;
    private boolean isAI;

    //resources
    private HashMap<Resource, Integer> resources;

    //player owned items
    private ArrayList<ProgressCard> newCards; //cards bought this turn
    private ArrayList<ProgressCard> cards; //playable cards
    private ArrayList<ProgressCard> playedCards; //already played cards
    private ArrayList<Achievement> achievements;
    private ArrayList<PlayerAction> remainingActions;

    //Constructor for normal players
    public Player() {
        this.newCards = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.playedCards = new ArrayList<>();
        this.achievements = new ArrayList<>();
        this.remainingActions = new ArrayList<>();

        this.resources = new HashMap<>();
        this.resources.put(Resource.BRICK, 0);
        this.resources.put(Resource.GRAIN, 0);
        this.resources.put(Resource.WOOL, 0);
        this.resources.put(Resource.LUMBER, 0);
        this.resources.put(Resource.ORE, 0);

        this.victoryPointsAmount = 0;
    }

    public Player(int playerId, String userId, boolean isAI) {
        this();
        this.playerId = playerId;
        this.userId = userId;
        this.isAI = isAI;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param player    the player to copy
     */
    public Player(Player player) {
        this.playerId = player.getPlayerId();
        this.userId = player.getUserId();
        this.victoryPointsAmount = player.getVictoryPointsAmount();
        this.isAI = player.isAI();
        this.resources = new HashMap<>(player.getResources());
        this.newCards = player.getNewCards().stream().map(ProgressCard::new).collect(Collectors.toCollection(ArrayList::new));
        this.cards = player.getCards().stream().map(ProgressCard::new).collect(Collectors.toCollection(ArrayList::new));
        this.playedCards = player.getPlayedCards().stream().map(ProgressCard::new).collect(Collectors.toCollection(ArrayList::new));
        this.achievements = new ArrayList<>(player.getAchievements());
        this.remainingActions = new ArrayList<>(player.getRemainingActions());
    }

    public int getBricks() {
        return resources.get(Resource.BRICK);
    }

    public int getGrain() {
        return resources.get(Resource.GRAIN);
    }

    public int getLumber() {
        return resources.get(Resource.LUMBER);
    }

    public int getOre() {
        return resources.get(Resource.ORE);
    }

    public int getWool() {
        return resources.get(Resource.WOOL);
    }

    public int getResourcesTotal() {
        return this.resources.values().stream().reduce(0, Integer::sum);
    }

    /**
     * This method returns the resource map as a list
     *
     * @return a list of the resources
     */
    public List<Resource> getResourceList() {
        List<Resource> resourceList = new ArrayList<>();

        //Converts the resource map into a list
        for (Resource resource: resources.keySet()) {
            for (int i = 0; i < resources.get(resource); i++) {
                resourceList.add(resource);
            }
        }

        return resourceList;
    }


    /**
     * This method should be called at the start of the each turn to move the cards bought last turn to the playable cards.
     */
    public void moveNewCards() {
        this.cards.addAll(this.newCards);
        this.newCards = new ArrayList<>();
    }

    /**
     * This method is used to give resources to a play
     *
     * @param bricks amount of bricks to be removed
     * @param grain  amount of grain to be removed
     * @param lumber amount of lumber to be removed
     * @param ore    amount of ore to be removed
     * @param wool   amount of wool to be removed
     */
    public void addResources(int bricks, int grain, int lumber, int ore, int wool) {
        if (bricks >= 0
                && grain >= 0
                && lumber >= 0
                && ore >= 0
                && wool >= 0
        ) {
            this.resources.replace(Resource.BRICK, getBricks() + bricks);
            this.resources.replace(Resource.GRAIN, getGrain() + grain);
            this.resources.replace(Resource.LUMBER, getLumber() + lumber);
            this.resources.replace(Resource.ORE, getOre() + ore);
            this.resources.replace(Resource.WOOL, getWool() + wool);
        } else {
            log.error("Cancelled addResources, negative values are not allowed, use removeResources instead which has built-in non-zero checks!");
            throw new IllegalArgumentException("Negative values not permitted, use removeResources instead!");
        }
    }

    /**
     * This method is used to give resources to a player based on a list
     *
     * @param resources a list of resources that have to be added
     */
    public void addResources(List<Resource> resources) {
        resources.forEach(resource -> this.resources.replace(resource, this.resources.get(resource) + 1));
    }

    /**
     * This methods is used to give resources to a player based on a map
     *
     * @param resources map of resources that have to be added
     */
    public void addResources(Map<Resource, Integer> resources) {
        for (Resource resource : resources.keySet()) {
            this.resources.replace(resource, this.resources.get(resource) + resources.get(resource));
        }
    }

    /**
     * this method is used to give a user multiple resources of the same type
     *
     * @param resource the resource to be given
     * @param amount   the amount to be given
     */
    public void addResources(Resource resource, int amount) {
        resources.replace(resource, resources.get(resource) + amount);
    }

    /**
     * The method is used to remove resources from the player, checks if player has correct amount of resources to begin with
     *
     * @param bricks amount of bricks to be removed
     * @param grain  amount of grain to be removed
     * @param lumber amount of lumber to be removed
     * @param ore    amount of ore to be removed
     * @param wool   amount of wool to be removed
     * @return whether removeResources succeeded or not
     */
    public boolean removeResources(int bricks, int grain, int lumber, int ore, int wool) {
        if (bricks <= getBricks()
                && grain <= getGrain()
                && lumber <= getLumber()
                && ore <= getOre()
                && wool <= getWool()
        ) {
            this.resources.replace(Resource.BRICK, getBricks() - bricks);
            this.resources.replace(Resource.GRAIN, getGrain() - grain);
            this.resources.replace(Resource.LUMBER, getLumber() - lumber);
            this.resources.replace(Resource.ORE, getOre() - ore);
            this.resources.replace(Resource.WOOL, getWool() - wool);

            return true;
        } else {
            log.warn("Cancelled removeResources, the player had insufficient resources");
            return false;
        }
    }

    /**
     * This method is used to remove resources from a player based on a map
     *
     * @param resources a map of resources
     * @return true if successful
     */
    public boolean removeResources(Map<Resource, Integer> resources) {
        if (!hasEnoughResources(resources)) {
            return false;
        }
        for (Resource resource : resources.keySet()) {
            removeResources(resource, resources.get(resource));
        }
        return true;
    }

    /**
     * This method is used to check if a player has enough resources to remove the amounts in a map
     *
     * @param resources a map of resources
     * @return true if possible
     */
    public boolean hasEnoughResources(Map<Resource, Integer> resources) {
        for (Resource resource : resources.keySet()) {
            if (this.resources.get(resource) < resources.get(resource)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The method is used to remove a single resource from the player, checks if player has correct amount of resources to begin with
     *
     * @param resource the resource that is removed
     * @return whether removeResource succeeded or not
     */
    public boolean removeResources(Resource resource) {
        if(!resource.equals(Resource.NOTHING)) {
            int currentSubTotal = this.resources.get(resource);
            if (currentSubTotal > 0) {
                this.resources.replace(resource, --currentSubTotal);
                return true;
            }
        }
        return false;
    }

    /**
     * The method is used to remove resources from the player, checks if player has correct amount of resources to begin with
     *
     * @param resources a list of all resources that have to be removed, example: [WOOL, WOOL, LUMBER]
     * @return whether removeResources succeeded or not
     */
    public boolean removeResources(List<Resource> resources) {
        int[] removedResources = new int[5];

        for (Resource resource : resources) {
            switch (resource) {
                case BRICK:
                    removedResources[0]++;
                    break;
                case GRAIN:
                    removedResources[1]++;
                    break;
                case LUMBER:
                    removedResources[2]++;
                    break;
                case ORE:
                    removedResources[3]++;
                    break;
                case WOOL:
                    removedResources[4]++;
                    break;
            }
        }

        return removeResources(removedResources[0], removedResources[1], removedResources[2], removedResources[3], removedResources[4]);
    }

    /**
     * removes a defined amount of resources from a single type
     *
     * @param resource the resource type to be removed
     * @param amount   the amount of the chosen type to be removed
     * @return true if successful
     */
    public boolean removeResources(Resource resource, int amount) {
        if (resources.get(resource) >= amount) {
            resources.replace(resource, resources.get(resource) - amount);
            return true;
        }
        return false;
    }

    /**
     * This method removes a certain resource from the player's resources.
     *
     * @param resource the resource to be removed
     * @return the amount of removed resources
     */
    public int removeForMonopoly(Resource resource) {
        int amount = resources.get(resource);
        resources.replace(resource, 0);
        return amount;
    }

    /**
     * Increases the victoryPoints for a player
     *
     * @param victoryPoints the amount of points to be added
     */
    public void increaseVictoryPoints(int victoryPoints){
        victoryPointsAmount = victoryPointsAmount + victoryPoints;
    }

    /**
     * Decreases the victoryPoints for a player
     *
     * @param victoryPoints the amount of points to be removed
     */
    public void decreaseVictoryPoints(int victoryPoints){
        victoryPointsAmount = victoryPointsAmount - victoryPoints;
    }
}
