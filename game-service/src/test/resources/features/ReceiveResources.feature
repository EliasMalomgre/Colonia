Feature: Player receives resource cards

  Scenario: Player receives multiple resource cards
    Given The player exists
    When The player receives "LUMBER", "ORE", "WOOL", "WOOL" resource cards
    Then The player received "LUMBER", "ORE", "WOOL", "WOOL" resource cards


  Scenario: Player receives wrong resource cards
    Given The player exists
    When The player receives "LUMBER", "WOOL", "WOOL" resource cards
    Then The player received  "ORE", "WOOL", "WOOL" resource cards