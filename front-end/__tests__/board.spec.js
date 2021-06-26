import { createLocalVue, shallowMount } from '@vue/test-utils'
import Board from './../src/components/Board.vue'
import Tile from './../src/components/gameobjects/Tile.vue'
import Vuex from "vuex"


const localVue = createLocalVue()
localVue.use(Vuex)

// to run tests => npm test

//Local Vuex store to for testing
const store = new Vuex.Store({
    state: {
        resourceTypes: ["BRICK",
            "ORE",
            "LUMBER",
            "WOOL",
            "GRAIN"],
        currentGame: {
            "id": "5ff5bb711ebf3f5efdd59d45",
            "players": [{ "playerId": 2, "userId": "5fe7553340d87110ec51cfc5", "victoryPointsAmount": 2, "resources": { "ORE": 0, "BRICK": 0, "WOOL": 0, "GRAIN": 0, "LUMBER": 0 }, "newCards": [], "cards": [], "playedCards": [], "achievements": [], "remainingActions": [], "resourcesTotal": 0, "lumber": 0, "wool": 0, "ore": 0, "ai": false, "bricks": 0, "grain": 0 },
            { "playerId": 1, "userId": "5fd1df9b770a39399925e9d8", "victoryPointsAmount": 2, "resources": { "ORE": 0, "BRICK": 0, "WOOL": 0, "GRAIN": 0, "LUMBER": 0 }, "newCards": [], "cards": [], "playedCards": [], "achievements": [], "remainingActions": ["ROLL", "PLAY_CARD"], "resourcesTotal": 0, "lumber": 0, "wool": 0, "ore": 0, "ai": false, "bricks": 0, "grain": 0 }],
            "board": { "tiles": [{ "index": 0, "coordinate": { "x": 0, "y": 3, "z": -3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 1, "coordinate": { "x": 1, "y": 2, "z": -3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 2, "coordinate": { "x": 2, "y": 1, "z": -3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 3, "coordinate": { "x": 3, "y": 0, "z": -3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 4, "coordinate": { "x": -1, "y": 3, "z": -2, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" },
             { "index": 5, "coordinate": { "x": 0, "y": 2, "z": -2, "cardDir": "NONE", "direction": "NONE" }, "number": 11, "tileType": "FIELD", "resourceType": "GRAIN" }, { "index": 6, "coordinate": { "x": 1, "y": 1, "z": -2, "cardDir": "NONE", "direction": "NONE" }, "number": 5, "tileType": "FOREST", "resourceType": "LUMBER" }, { "index": 7, "coordinate": { "x": 2, "y": 0, "z": -2, "cardDir": "NONE", "direction": "NONE" }, "number": 9, "tileType": "HILLS", "resourceType": "BRICK" }, { "index": 8, "coordinate": { "x": 3, "y": -1, "z": -2, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 9, "coordinate": { "x": -2, "y": 3, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, 
             { "index": 10, "coordinate": { "x": -1, "y": 2, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 3, "tileType": "MOUNTAINS", "resourceType": "ORE" }, { "index": 11, "coordinate": { "x": 0, "y": 1, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 10, "tileType": "PLAINS", "resourceType": "WOOL" }, { "index": 12, "coordinate": { "x": 1, "y": 0, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 2, "tileType": "MOUNTAINS", "resourceType": "ORE" }, { "index": 13, "coordinate": { "x": 2, "y": -1, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 4, "tileType": "HILLS", "resourceType": "BRICK" }, { "index": 14, "coordinate": { "x": 3, "y": -2, "z": -1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" },
              { "index": 15, "coordinate": { "x": -3, "y": 3, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 16, "coordinate": { "x": -2, "y": 2, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 9, "tileType": "FOREST", "resourceType": "LUMBER" }, { "index": 17, "coordinate": { "x": -1, "y": 1, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 8, "tileType": "MOUNTAINS", "resourceType": "ORE" }, { "index": 18, "coordinate": { "x": 0, "y": 0, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 3, "tileType": "FOREST", "resourceType": "LUMBER" }, { "index": 19, "coordinate": { "x": 1, "y": -1, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 12, "tileType": "PLAINS", "resourceType": "WOOL" }, 
              { "index": 20, "coordinate": { "x": 2, "y": -2, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 10, "tileType": "PLAINS", "resourceType": "WOOL" }, { "index": 21, "coordinate": { "x": 3, "y": -3, "z": 0, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 22, "coordinate": { "x": -3, "y": 2, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 23, "coordinate": { "x": -2, "y": 1, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 4, "tileType": "FIELD", "resourceType": "GRAIN" }, { "index": 24, "coordinate": { "x": -1, "y": 0, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 11, "tileType": "PLAINS", "resourceType": "WOOL" }, 
              { "index": 25, "coordinate": { "x": 0, "y": -1, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "DESERT", "resourceType": "NOTHING" }, { "index": 26, "coordinate": { "x": 1, "y": -2, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 8, "tileType": "FIELD", "resourceType": "GRAIN" }, { "index": 27, "coordinate": { "x": 2, "y": -3, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 28, "coordinate": { "x": -3, "y": 1, "z": 2, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 29, "coordinate": { "x": -2, "y": 0, "z": 2, "cardDir": "NONE", "direction": "NONE" }, "number": 5, "tileType": "FIELD", "resourceType": "GRAIN" }, 
              { "index": 30, "coordinate": { "x": -1, "y": -1, "z": 2, "cardDir": "NONE", "direction": "NONE" }, "number": 6, "tileType": "FOREST", "resourceType": "LUMBER" }, { "index": 31, "coordinate": { "x": 0, "y": -2, "z": 2, "cardDir": "NONE", "direction": "NONE" }, "number": 6, "tileType": "HILLS", "resourceType": "BRICK" }, { "index": 32, "coordinate": { "x": 1, "y": -3, "z": 2, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 33, "coordinate": { "x": -3, "y": 0, "z": 3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 34, "coordinate": { "x": -2, "y": -1, "z": 3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, 
              { "index": 35, "coordinate": { "x": -1, "y": -2, "z": 3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }, { "index": 36, "coordinate": { "x": 0, "y": -3, "z": 3, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "WATER", "resourceType": "NOTHING" }], 
              "harbours": [{ "coordinate": { "x": 0, "y": 2, "z": -2, "cardDir": "NORTH_WEST", "direction": "NONE" }, "ratio": 2, "resource": "LUMBER", "accessCoordinates": [{ "x": 0, "y": 2, "z": -2, "cardDir": "NONE", "direction": "TOP" }, { "x": 0, "y": 2, "z": -2, "cardDir": "NONE", "direction": "LEFT" }] }, { "coordinate": { "x": 1, "y": -3, "z": 2, "cardDir": "NORTH_WEST", "direction": "NONE" }, "ratio": 2, "resource": "WOOL", "accessCoordinates": [{ "x": 1, "y": -3, "z": 2, "cardDir": "NONE", "direction": "TOP" }, { "x": 1, "y": -3, "z": 2, "cardDir": "NONE", "direction": "LEFT" }] }, { "coordinate": { "x": -1, "y": -2, "z": 3, "cardDir": "NORTH_WEST", "direction": "NONE" }, "ratio": 2, "resource": "BRICK", "accessCoordinates": [{ "x": -1, "y": -2, "z": 3, "cardDir": "NONE", "direction": "TOP" }, { "x": -1, "y": -2, "z": 3, "cardDir": "NONE", "direction": "LEFT" }] }, { "coordinate": { "x": 1, "y": 1, "z": -2, "cardDir": "NORTH_WEST", "direction": "NONE" }, "ratio": 3, "resource": "NOTHING", "accessCoordinates": [{ "x": 1, "y": 1, "z": -2, "cardDir": "NONE", "direction": "TOP" }, { "x": 1, "y": 1, "z": -2, "cardDir": "NONE", "direction": "LEFT" }] }, { "coordinate": { "x": 3, "y": -3, "z": 0, "cardDir": "WEST", "direction": "NONE" }, "ratio": 3, "resource": "NOTHING", "accessCoordinates": [{ "x": 3, "y": -3, "z": 0, "cardDir": "NONE", "direction": "LEFT" }, { "x": 2, "y": -3, "z": 1, "cardDir": "NONE", "direction": "TOP" }] }, { "coordinate": { "x": -1, "y": 2, "z": -1, "cardDir": "WEST", "direction": "NONE" }, "ratio": 2, "resource": "ORE", "accessCoordinates": [{ "x": -1, "y": 2, "z": -1, "cardDir": "NONE", "direction": "LEFT" }, { "x": -2, "y": 2, "z": 0, "cardDir": "NONE", "direction": "TOP" }] }, { "coordinate": { "x": -2, "y": 1, "z": 1, "cardDir": "WEST", "direction": "NONE" }, "ratio": 3, "resource": "NOTHING", "accessCoordinates": [{ "x": -2, "y": 1, "z": 1, "cardDir": "NONE", "direction": "LEFT" }, { "x": -3, "y": 1, "z": 2, "cardDir": "NONE", "direction": "TOP" }] }, { "coordinate": { "x": -3, "y": 0, "z": 3, "cardDir": "NORTH_EAST", "direction": "NONE" }, "ratio": 2, "resource": "GRAIN", "accessCoordinates": [{ "x": -3, "y": 0, "z": 3, "cardDir": "NONE", "direction": "TOP" }, { "x": -2, "y": -1, "z": 3, "cardDir": "NONE", "direction": "LEFT" }] }, { "coordinate": { "x": 2, "y": -1, "z": -1, "cardDir": "NORTH_EAST", "direction": "NONE" }, "ratio": 3, "resource": "NOTHING", "accessCoordinates": [{ "x": 2, "y": -1, "z": -1, "cardDir": "NONE", "direction": "TOP" }, { "x": 3, "y": -2, "z": -1, "cardDir": "NONE", "direction": "LEFT" }] }], "roads": [{ "coordinate": { "x": 0, "y": 1, "z": -1, "cardDir": "NORTH_EAST", "direction": "NONE" }, "playerId": 1 }, { "coordinate": { "x": 1, "y": -1, "z": 0, "cardDir": "NORTH_EAST", "direction": "NONE" }, "playerId": 2 }, { "coordinate": { "x": -2, "y": 1, "z": 1, "cardDir": "WEST", "direction": "NONE" }, "playerId": 2 }, { "coordinate": { "x": 1, "y": 0, "z": -1, "cardDir": "NORTH_WEST", "direction": "NONE" }, "playerId": 1 }], 
              "settlements": [{ "coordinate": { "x": 0, "y": 1, "z": -1, "cardDir": "NONE", "direction": "TOP" }, "playerId": 1, "city": false }, { "coordinate": { "x": 1, "y": -1, "z": 0, "cardDir": "NONE", "direction": "TOP" }, "playerId": 2, "city": false }, { "coordinate": { "x": -2, "y": 1, "z": 1, "cardDir": "NONE", "direction": "LEFT" }, "playerId": 2, "city": false }, { "coordinate": { "x": 1, "y": 0, "z": -1, "cardDir": "NONE", "direction": "TOP" }, "playerId": 1, "city": false }], "robberTile": { "index": 25, "coordinate": { "x": 0, "y": -1, "z": 1, "cardDir": "NONE", "direction": "NONE" }, "number": 0, "tileType": "DESERT", "resourceType": "NOTHING" } }, 
              "initialRolls": { "5fe7553340d87110ec51cfc5": 4, "5fd1df9b770a39399925e9d8": 8 }, "currentPlayerId": 1, "hostId": "5fe7553340d87110ec51cfc5", "gameState": "ACTIVE", "playerIdWithLongestRoad": 0, "playerWithLargestArmy": 0, "tradeRequest": null, "cardPile": [{ "cardType": "KNIGHT" }, { "cardType": "YEAR_OF_PLENTY" }, { "cardType": "KNIGHT" }, { "cardType": "MONOPOLY" }, { "cardType": "KNIGHT" }, { "cardType": "KNIGHT" }, { "cardType": "KNIGHT" }, { "cardType": "VICTORY_POINT" }, { "cardType": "ROAD_BUILDING" }, { "cardType": "KNIGHT" }, { "cardType": "VICTORY_POINT" }, { "cardType": "KNIGHT" }, { "cardType": "KNIGHT" }, { "cardType": "VICTORY_POINT" }, { "cardType": "KNIGHT" }, { "cardType": "VICTORY_POINT" }, { "cardType": "KNIGHT" }, { "cardType": "YEAR_OF_PLENTY" }, { "cardType": "KNIGHT" }, { "cardType": "KNIGHT" }, { "cardType": "MONOPOLY" }, { "cardType": "KNIGHT" }, { "cardType": "KNIGHT" }, { "cardType": "VICTORY_POINT" }, { "cardType": "ROAD_BUILDING" }], "currentPlayer": { "playerId": 1, "userId": "5fd1df9b770a39399925e9d8", "victoryPointsAmount": 2, "resources": { "ORE": 0, "BRICK": 0, "WOOL": 0, "GRAIN": 0, "LUMBER": 0 }, "newCards": [], "cards": [], "playedCards": [], "achievements": [], "remainingActions": ["ROLL", "PLAY_CARD"], "resourcesTotal": 0, "lumber": 0, "wool": 0, "ore": 0, "ai": false, "bricks": 0, "grain": 0 }
        },
        buildingCosts: null,
        possibleSettlementPlacements: [],
        possibleRoadPlacements: [],


    },
    getters: {
        getGameHostId(state) {
            if (state.currentGame != null) {
                return state.currentGame.hostId;
            } return null
        },
        getCurrentGame(state) {
            if (state.currentGame != null) {
                return state.currentGame
            } return null
        },
        getTilesByZ(state) {
            return (zCoord) => {
                if (state.currentGame !== null) {
                    return state.currentGame.board.tiles.filter((tile) => {
                        return tile.coordinate.z == zCoord
                    })
                } return null

            }
        },
        getHarbourForTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.currentGame != null) {
                    return state.currentGame.board.harbours.find((harbour) => {
                        return (harbour.coordinate.x == xCoord
                            && harbour.coordinate.y == yCoord
                            && harbour.coordinate.z == zCoord)
                    })
                } return null
            }
        },
        isRobberTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.currentGame !== null) {
                    if (state.currentGame.board.robberTile.coordinate.x == xCoord
                        && state.currentGame.board.robberTile.coordinate.y == yCoord
                        && state.currentGame.board.robberTile.coordinate.z == zCoord) {
                        return true
                    } else return false
                } return null
            }
        },
        getGamePlayerByPlayerId(state) {
            return (id) => {
                if (state.currentGame != null) {
                    return state.currentGame.players.find((player) => {
                        return (player.playerId == id)
                    })
                } return null
            }
        },
        getGamePlayerByUserId(state) {
            return (id) => {
                if (state.currentGame !== null) {
                    return state.currentGame.players.find((player) => {
                        return (player.userId == id)
                    })
                } return null
            }
        },
        getPlayers(state) {
            if (state.currentGame !== null) {
                return state.currentGame.players;
            } return null
        },
        getResourceTypes(state) {
            return state.resourceTypes;
        },
        getBuildingCosts(state) {
            return state.buildingCosts;
        },
        getPossibleRoadPlacements(state) {
            return state.possibleRoadPlacements

        },
        getRoadTargetsForTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.possibleRoadPlacements != null) {
                    return state.possibleRoadPlacements.filter((target) => {
                        return (target.x == xCoord
                            && target.y == yCoord
                            && target.z == zCoord)
                    })
                } return null
            }
        },
        getPossibleSettlementPlacements(state) {
            return state.possibleSettlementPlacements

        },
        getSettlementTargetsForTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.possibleSettlementPlacements.length > 0) {
                    return state.possibleSettlementPlacements.filter((target) => {
                        return (target.x == xCoord
                            && target.y == yCoord
                            && target.z == zCoord)
                    })
                } else return null


            }
        },
        getSettlementsForTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.currentGame.board.settlements.length > 0) {
                    return state.currentGame.board.settlements.filter((settlement) => {
                        return (settlement.coordinate.x == xCoord
                            && settlement.coordinate.y == yCoord
                            && settlement.coordinate.z == zCoord)
                    })
                } else return null


            }

        },
        getRoadsForTile(state) {
            return (xCoord, yCoord, zCoord) => {
                if (state.currentGame.board.roads.length > 0) {
                    return state.currentGame.board.roads.filter((settlement) => {
                        return (settlement.coordinate.x == xCoord
                            && settlement.coordinate.y == yCoord
                            && settlement.coordinate.z == zCoord)
                    })
                } else return null


            }

        },
        getCurrentPlayer(state) {
            return state.currentGame.currentPlayer
        },

        getTradeRequest(state) {
            if (state.currentGame != null) {
                return state.currentGame.tradeRequest
            }

        }

    }



})

//TESTS

describe('Board', () => {
    // Check that data has been added, is returned as function
    it('has data', () => {
        expect(typeof Board.data).toBe('function')
    })
})

describe('Mounted Board', () => {
    //create wrapper to test on
    const wrapper = shallowMount(Board, {
        store,
        localVue
    })

    test('is a Vue instance', () => {
        expect(wrapper.isVueInstance()).toBeTruthy()
    })
    it('expect wrapper to have a boardcontainer class', () => {
        expect(wrapper.classes()).toContain('boardcontainer')
    })

    it('expect wrapper to have 41 tile components (37 tiles + 4 spacers)', () => {
        const tiles = wrapper.findAllComponents(Tile)
        expect(tiles).toHaveLength(41)
    })
})