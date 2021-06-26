import axios from 'axios'
import { store } from '../store'
import authHeader from './auth-header'
import { getLoggedInPlayer, getLoggedInUser } from './player-methods'
import { negativeNotify } from './notifications'
import { Dialog } from 'quasar'


const API_URL = 'http://localhost:4001/api/games/';

class GameService {
  createGame(userids, amountOfAIs,hostId) {
    return axios.post(API_URL + "createGame",
      {
        userIds: userids,
        amountOfAIs: amountOfAIs,
        userIdOfHost:hostId
      }, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Error creating game")
      })

  }

  getGameById(id) {
    return axios.get(API_URL + "getGame?gameId=" + id, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("We cannot find this game right now, try again later.")
      })
  }

  getResources() {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()
    return axios.get(API_URL + "getResourcesForPlayer?gameId=" + gameId + "&playerId=" + player.playerId, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Resources could not be found")
      })
  }

  getPossibleSettlementPlacements(id) {
    var gameId
    if (id != null && id !== undefined) {
      gameId = id
    } else {
      gameId = store.getters.getCurrentGame.id
    }

    var player = getLoggedInPlayer()
    return axios.get(API_URL + "possibleSettlementPlacements?gameId=" + gameId + "&playerId=" + player.playerId
      , authHeader())
      .then(result => {
        return result.data.possibleSettlementCoordinates
      })
      .catch(() => {
        negativeNotify("Oh no! We cannot currently find the possible building spots! Maybe try again later")
      })

  }

  getPossibleRoadPlacements(id) {
    var gameId
    if (id !== null && id !== undefined) {
      gameId = id
    } else {
      gameId = store.getters.getCurrentGame.id
    }
    var player = getLoggedInPlayer()

    return axios.get(API_URL + "possibleRoadPlacements?gameId=" + gameId + "&playerId=" + player.playerId
      , authHeader())
      .then(result => {
        return result.data.possibleRoadCoordinates
      })
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("Oopsie, we cannot retreive possible road positions. Maybe try builing the next turn!")
      })

  }

  rollDice() {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()
    return axios.post(API_URL + "rollDice",
      {
        gameId: gameId,
        playerId: player.playerId
      }, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Yikes! You rolled the dice so hard we seem to have lost them! Maybe refresh and try again, a bit softer this time yeah?")
      })
  }

  getGamesOverview(filter){
    var user = getLoggedInUser()
    return axios.get(API_URL + "getGamesOverview?userId=" + user.id+"&filter="+filter, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("We cannot retrieve your past games. Maybe it's just time to start a new one?")
      })
  }

  resumeGame(gameId){
    return axios.get(API_URL + "resumeGame?gameId="+gameId, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Game could not be resumed right now. You can always start a new one?")
      })
  }

  initialRoll() {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()
    return axios.post(API_URL + "rollForInitiative",
      {
        gameId: gameId,
        userId: player.userId
      }, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        this.negativeNotify("Oh no! Illegal diceroll! Please refresh and try again")
      })
  }
  pause(){
    var gameId = store.getters.getCurrentGame.id
    return axios.get(API_URL + "pauseGame?gameId="+gameId,authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        this.negativeNotify("Game could not be paused.... Guess you will just have to keep playing then?")
      })
  }

  stopEarly(){
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL+"endGameEarly?gameId="+gameId,{},authHeader())
                .then(result=>{
                  return result.data
                })
                .catch(()=>{
                  negativeNotify("We could not stop this game early... Maybe it's better this way, how else would you find out who wins?");
                })
  }

  endTurn() {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()

    return axios.post(API_URL + "endTurn",
      {
        gameId: gameId,
        playerId: player.playerId
      }, authHeader())
      .catch(() => {
        negativeNotify("We could not end your turn. Maybe there are still some important unplayed actions?")
      })

  }

  build(coordinate) {
    var player = getLoggedInPlayer()
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "build",
      {
        gameId: gameId,
        playerId: player.playerId,
        coordinate: {
          x: coordinate.x,
          y: coordinate.y,
          z: coordinate.z,
          cardDir: coordinate.cardDir,
          direction: coordinate.direction
        }
      }, authHeader())
      .then(() => {
        //set build options back to false
        store.dispatch("updateShowSettlementTargets", false)
        if (!store.getters.getInitialPhase) {
          store.dispatch("updateShowRoadTargets", false)
        } else {
          store.commit('setShowRoadTargets', true)
          store.dispatch("loadPossibleRoadPlacementsForCurrentPlayer", gameId)
          store.commit('setInitialPhase', false)
        }

        // reload game
        store.dispatch("loadCurrentGame", gameId)
      })
      .catch(() => {
        negativeNotify("Housemarket crashed! No building possible! Try again next turn (or after a refresh if you are impatient)")
      })
  }


  upgradeSettlement(coordinate) {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()

    return axios.post(API_URL + "upgradeSettlement",
      {
        gameId: gameId,
        playerId: player.playerId,
        coordinate: {
          x: coordinate.x,
          y: coordinate.y,
          z: coordinate.z,
          cardDir: coordinate.cardDir,
          direction: coordinate.direction
        }
      }, authHeader())
      .catch(() => {
        negativeNotify("Not enough resources to upgrade")
      })
  }

  getBuildingCosts() {
    return axios.get(API_URL + "getBuildingCosts", authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Building costs could not be found")
      })
  }

  moveRobber(coordinate) {
    
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()

    return axios.post(API_URL + "moveRobber",
      {
        gameId: gameId,
        playerId: player.playerId,
        coordinate: {
          x: coordinate.x,
          y: coordinate.y,
          z: coordinate.z,
          cardDir: coordinate.cardDir,
          direction: coordinate.direction
        }
      }, authHeader())
      .then(result => {
        store.commit('updateRobberTile', coordinate)
        if(result.data==null || result.data.length==0){
          Dialog.create({
            title: 'Robber moved',
            message: 'The robber has been moved! Unfortunately, there are no possible players for you to steal from.'
          }).onDismiss(() => {
            store.dispatch("loadCurrentGame", gameId)
            store.dispatch("setPlayableActions")
          })
        }else{
          store.dispatch("updateStealDialogData", result.data)
        }
      })
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("The robber does not wish to move right now. Try again later.")
      })
  }

  stealResources(id){
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()
    return axios.post(API_URL + "stealResource",
      {
        gameId: gameId,
        playerId: player.playerId,
        playerIdToStealFrom: id

      }, authHeader())
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("Something went wrong stealing a resource... Try to be a bit sneakier next time, yeah?")
      })
  }

  discardResources(resources) {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()
    return axios.post(API_URL + "discardResources",
      {
        gameId: gameId,
        playerId: player.playerId,
        discardedResources: resources

      }, authHeader())
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("Unable to discard resources... Lucky you!")
      })

  }


  startTrade(toOffer, toReceive, receivingPlayerId) {
    var gameId = store.getters.getCurrentGame.id
    var player = getLoggedInPlayer()

    return axios.post(API_URL + "startTrade",
      {
        gameId: gameId,
        asking: player.playerId,
        receiving: receivingPlayerId,
        toSend: toOffer,
        toReceive: toReceive
      }, authHeader())
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("We could not start this trade. Try again sometime later")
      })

  }


  acceptTrade(tradeId) {
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "acceptTrade",
      {
        gameId: gameId,
        tradeId: tradeId
      }, authHeader())
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("Not enough resources to trade!")
      })


  }
}

export default new GameService();

