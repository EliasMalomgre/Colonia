Feature: Player loses resources

  Scenario: Player loses resources
    Given the player exists
    And the player has "WOOL", "WOOL", "BRICK", "ORE" resource cards
    When the player loses "WOOL", "ORE" resource cards
    Then the player has "BRICK", "WOOL"

  Scenario: Player loses wrong resources
    Given the player exists
    And the player has "WOOL", "BRICK", "ORE" resource cards
    When the player loses "WOOL", resource cards
    Then the player has "WOOL"
