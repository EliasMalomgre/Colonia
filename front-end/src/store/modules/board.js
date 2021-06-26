import GameService from '../../services/game.service'

const state = {
    resourceTypes:["BRICK",
    "ORE",
    "LUMBER",
    "WOOL",
    "GRAIN"],
   currentGame: null,
   buildingCosts: null,
   possibleSettlementPlacements: [ ],
   possibleRoadPlacements: [ ]
};
 const mutations = {
    setCurrentGame(state, payload){
        state.currentGame=payload
    },
    setBuildingCosts(state, payload){
      state.buildingCosts=payload
    },
    updateRobberTile(state, payload){
      state.currentGame.board.robberTile.coordinate = payload
    },
    updatePossibleRoadPlacements(state, payload){
      state.possibleRoadPlacements=payload
    },
    updatePossibleSettlementPlacements(state, payload){
      state.possibleSettlementPlacements=payload
    }
};
const actions = {
    loadCurrentGame({commit}, id){
         return GameService.getGameById(id)
         .then(result => { 
             commit('setCurrentGame', result)
         })
    },

    loadBuildingCosts({commit}){
      return GameService.getBuildingCosts()
         .then(result => {
             commit('setBuildingCosts', result)
         })
    },
    
    //id is optional, after initial loading game
    loadPossibleRoadPlacementsForCurrentPlayer({commit}, id){
      return GameService.getPossibleRoadPlacements(id)
         .then(result => {
             commit('updatePossibleRoadPlacements', result)
         })

    },
    //id is optional, after initial loading game
    loadPossibleSettlementPlacementsForCurrentPlayer({commit}, id){
      return GameService.getPossibleSettlementPlacements(id)
         .then(result => {
             commit('updatePossibleSettlementPlacements', result)
         })

    }

}; 
const getters = {
    getGameHostId(state){
      if(state.currentGame!= null){
        return state.currentGame.hostId;
      } return null
    },
    getCurrentGame(state){
      if(state.currentGame!= null){
        return state.currentGame
      } return null
      },
      getTilesByZ(state){
        return(zCoord) => {
          if(state.currentGame!== null){
            return state.currentGame.board.tiles.filter((tile) => {
            return tile.coordinate.z == zCoord
          })
          } return null
          
        }
      },
      getHarbourForTile(state){
          return (xCoord, yCoord, zCoord) => {
            if(state.currentGame!= null){
              return state.currentGame.board.harbours.find((harbour)=> {
                  return (harbour.coordinate.x== xCoord 
                  && harbour.coordinate.y== yCoord
                  && harbour.coordinate.z== zCoord)
              })
            } return null
          }
      },
      isRobberTile(state){
        return (xCoord, yCoord, zCoord) => {
          if(state.currentGame!== null){
            if(state.currentGame.board.robberTile.coordinate.x == xCoord 
                && state.currentGame.board.robberTile.coordinate.y == yCoord
                && state.currentGame.board.robberTile.coordinate.z == zCoord){
                    return true 
                }else return false
              }return null
        }
      },
      getGamePlayerByPlayerId(state){
        return (id) => {
          if(state.currentGame!= null){
            return state.currentGame.players.find((player)=> {
                return (player.playerId==id)
            })
          }return null
        }
      },
      getGamePlayerByUserId(state){
        return (id) => {
          if(state.currentGame!== null){
            return state.currentGame.players.find((player)=> {
                return (player.userId==id)
            })
          }return null
        }
      },
      getPlayers(state){
        if(state.currentGame!== null){
        return state.currentGame.players;
        }return null
      },
      getResourceTypes(state){       
          return state.resourceTypes;
      },
      getBuildingCosts(state){       
        return state.buildingCosts;
    },
    getPossibleRoadPlacements(state){
      return state.possibleRoadPlacements

    },
    getRoadTargetsForTile(){
      return (xCoord, yCoord, zCoord) => {
        if(state.possibleRoadPlacements!= null){
          return state.possibleRoadPlacements.filter((target)=> {
              return (target.x== xCoord 
              && target.y== yCoord
              && target.z== zCoord)
          })
        } return null
      }
    },
    getPossibleSettlementPlacements(state){
      return state.possibleSettlementPlacements

    },
    getSettlementTargetsForTile(){
      return (xCoord, yCoord, zCoord) => {
        if(state.possibleSettlementPlacements.length>0){
          return state.possibleSettlementPlacements.filter((target)=> {
              return (target.x== xCoord 
              && target.y== yCoord
              && target.z== zCoord)
          })
        } else return null
          
        
      }
    },
    getSettlementsForTile(state){
      return (xCoord, yCoord, zCoord) => {
        if(state.currentGame.board.settlements.length>0){
          return state.currentGame.board.settlements.filter((settlement)=> {
              return (settlement.coordinate.x== xCoord 
              && settlement.coordinate.y== yCoord
              && settlement.coordinate.z== zCoord)
          })
        } else return null
          
        
      }

    },
    getRoadsForTile(state){
      return (xCoord, yCoord, zCoord) => {
        if(state.currentGame.board.roads.length>0){
          return state.currentGame.board.roads.filter((settlement)=> {
              return (settlement.coordinate.x== xCoord 
              && settlement.coordinate.y== yCoord
              && settlement.coordinate.z== zCoord)
          })
        } else return null
          
        
      }

    },
    getCurrentPlayer(state){
      return state.currentGame.currentPlayer
    },

    getTradeRequest(state){
      if(state.currentGame!=null){
        return state.currentGame.tradeRequest
      }
      
    }
   
};

export default {
    state,getters, mutations, actions
  }
  
