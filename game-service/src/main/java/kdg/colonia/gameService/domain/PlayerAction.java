package kdg.colonia.gameService.domain;

/**
 * Actions that the player has to complete before the turn is allowed to end
 * This includes:
 * ROLL -> The player hasn't rolled the dice yet
 * MOVEROBBER -> The player is required to move the robber to a new location
 * DISCARDRESOURCES -> The player is required to discard half of his total resources
 * */
public enum PlayerAction {
    //standard turns
    ROLL, MOVE_ROBBER, DISCARD_RESOURCES, PLAY_CARD, BUILD, BUY, STEAL, TRADE, END_TURN,
    //initial
    INITROLL, INITIAL1, INITIAL2,
    //progress cards
    YOP, MONOPOLY, ROAD_BUILDING
}
