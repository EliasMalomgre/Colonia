# Integratie Project 2 - 2020-2021 - Game Service - Colonia
Colonia is a version of 'Settlers of Catan' reimagined by a group of students at the Karel de Grote University College.

## Colonia
 - Arthur de Craemer
 - Daphne Deckers
 - Elias Malomgré
 - Louis Reyns
 - Tim Schelpe
 - Vink Van den Bosch

## Introduction
This project houses all game related logic: starting Settlers of Catan, multiplayer, storing games and all AI logic.

## Used technologies
The project is written entirely in Java and utilizes the Spring framework. As database we use MongoDB, hosted on the cloud through MongoDB Atlas. The credentials are stored in the application.properties file.

## Project usage
The main service of the project is the GameService. All incoming traffic will pass through Gamecontroller, into GameService. GameService will delegate to designated services and either return through REST or the sockets. GameService also initiates the communication with the AI. GameLogicService contains logic that has been extracted from GameService which allows the AI to avoid spamming sockets, logs and general outgoing traffic. All tests for GameLogicService are covered by the GameService tests since these have a near 1-1 mapping.

The AI utilizes the Monte Carlo Tree Search algorithm, this logic and the one for building the decision tree can be found in the MonteCarloService. This uses AIService to obtain domain information about the game. AIService determines all legal Actions and creates Action Tokens, these contain the logic required for the AI to perform the corresponding moves. To test the AI you can play a game against some of them, the actions the AI makes will be logged in the GameService console. You can also enable an experimental features in the properties to let AI's play against each other, while you spectate, do be warned, this is experimental.

### Warning
There is a known bug in early versions of java 11 with TLS 1.3, this produces a handshake exception with the mongoDB database.
We recommend running the project on java version 11.0.9, earlier versions will likely break the application or produce unexpected errors.

## Settings
The project has several configurations you can alter, these can be found in ./src/main/resources/application.properties.

We advise using the default values, as bad settings can and will lead to unintended behaviour and potential crashes.
Here are a couple settings that are safe to adjust:

	game.victoryPointsWin : Configures the required amount of victory points to conclude the game
	game.resourceGainSettlement : Configures the amount of resources gotten from tiles surrounding a settlement
	game.bonusIfCity : Grants a bonus on top of the regular resourceGain, when the settlement has been upgraded to a city

	game.attemptsBeforeAutoDiscard : How often 'timeBetweenAttempts' seconds are waited, before the AI takes over the DiscardResources action from a human player
	game.timeBetweenAttempts : How long we wait inbetween attempts

We advise against editing the board settings, unless adjustments are minor. Totals should be maintained at all times.

	deck.knightAmount : The amount of Knight cards available
	deck.yopAmount : The amount of Year of Plenty cards available 
	deck.monopolyAmount : The amount of Monopoly cards available
	deck.roadBuildingAmount : The amount of Road Building cards available
	deck.victoryAmount : The amount of Victory Point cards available

The following settings adjust the costs of a certain purchases 

	card.* :
	settlement.* :
	city.* :
	road.* :

Here are some tweaks for the AI

	ai.winScore : Score granted to a branch when the current player wins at the end
	ai.useOtherMetrics : Whether the following three settings are used
	ai.victoryPointScore : The bonus branch-score per victory point
	ai.vpLeaderBonus : The bonus branch-score when player is in the lead
	ai.longestRoadBonus : The bonus branch-score when the player holds the longest-road

	ai.learningRate : A value that describes how long the AI explores, before exploitation
	ai.searchDepth : How many actions "deep" the AI goes before starting backpropagation
	ai.simulationTime : How long the AI is allowed to simulate before returning results
	ai.reductionFewActions : By how much the simulationTime is divided if fewer than 'fewActions' are available 
	ai.fewActions : If fewer than this amount of actions the simulationTime is reduced.
	ai.onlyRandomMoves : If true doesn't simulate, but picks a random legal action
	ai.useChanceNodes : Use a special ActionNode that uses probabilities
	ai.randomDiscards : Whether to use a single random discard, or add all possible legalaction forks

Experimental UNSTABLE features:

	game.onlyAIGameExperimental : Makes all players AI's, this allows you to sit back, relax and spectate.

