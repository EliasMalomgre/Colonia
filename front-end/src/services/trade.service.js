import axios from 'axios'
import { store } from '../store'
import authHeader from './auth-header'
import { getLoggedInPlayer } from './player-methods'
import {negativeNotify} from './notifications'

const API_URL = 'http://localhost:4001/api/games/';

class TradeService {
  getTradeRatio(resource) {
    var player = getLoggedInPlayer()

    var gameId = store.getters.getCurrentGame.id
    return axios.get(API_URL + "getTradeRatio?gameId=" + gameId + "&playerId=" + player.playerId + "&resource=" + resource
      , authHeader())
      .then(result => {
        return result.data
      })
      //HANDLE ERROR => show to user
      .catch(() => {
        negativeNotify("We cannot seem to find the trade ratio for this resource.")
      })
  }

  tradeWithBank(resFrom, resTo) {
    var player = getLoggedInPlayer()

    var gameId = store.getters.getCurrentGame.id

    return axios.post(API_URL + "tradeWithBank",
      {
        gameId: gameId,
        playerId: player.playerId,
        from: resFrom,
        to: resTo
      }, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Oopsie... Something went wrong with your trade. Maybe refresh and try again!")
      })

  }


}

export default new TradeService();
