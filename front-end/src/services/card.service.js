import axios from 'axios'
import { store } from '../store'
import authHeader from './auth-header'
import { getLoggedInPlayer } from './player-methods'
import { negativeNotify } from './notifications'
import { Dialog } from 'quasar'

const API_URL = 'http://localhost:4001/api/games/';

class CardService {
  buyCard() {
    var player = getLoggedInPlayer()
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "buyCard",
      {
        gameId: gameId,
        playerId: player.playerId
      }, authHeader())
      .then(result => {
        if(result.data!= null && result.data != undefined && result.data){
          Dialog.create({
          title: 'New Card!',
          message: 'You have just bought a new '+ result.data.cardType+ ' card! Starting from your next turn, the card will be visible in your data. You can play the card by clicking on it.'
        })
        }else{
          Dialog.create({
            title: 'Not enough resources',
            message: 'You do not have enough resource to buy a card ... You can try again later!'
          })
          
        }
        
      })
      .catch(() => {

        negativeNotify("Something went wrong buying a card. Maybe refresh the page and try again?")
      })
  }

  playCard(card) {
    console.log("sending playCard...")
    var player = getLoggedInPlayer()
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "playCard",
      {
        gameId: gameId,
        playerId: player.playerId,
        cardType: card //card.cardType
      }, authHeader())
      .then(result => {
        console.log("succes")
        console.log(result.data)
        return result.data

      })
      .catch(err => {
        console.log(err)
        negativeNotify("Error playing card: try again later")
      })
  }

  playYearOfPlenty(res1, res2) {
    console.log("sending year of plenty...")
    var player = getLoggedInPlayer()
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "yearOfPlenty",
      {
        gameId: gameId,
        playerId: player.playerId,
        resource1: res1,
        resource2: res2
      }, authHeader())
      .then(result => {
        console.log("succes")
        console.log(result.data)

      })
      .catch(err => {
        console.log(err)
        negativeNotify("Something went wrong playing the year of plenty card")
      })
  }

  playMonopoly(res) {
    console.log("sending monopoly...")
    var player = getLoggedInPlayer()
    var gameId = store.getters.getCurrentGame.id
    return axios.post(API_URL + "monopoly",
      {
        gameId: gameId,
        playerId: player.playerId,
        resource: res
      }, authHeader())
      .catch(() => {
        negativeNotify("Monopoly card could not be played. Try again later.")
      })
  }



}

export default new CardService();