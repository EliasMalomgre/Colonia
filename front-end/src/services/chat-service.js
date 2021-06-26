import axios from 'axios'
import { store } from '../store'
import authHeader from './auth-header'
import { getLoggedInUser } from './player-methods'
import {negativeNotify} from './notifications'

const API_URL = 'http://localhost:4001/api/chat/';

class ChatService {
    getChatByGameId(id) {
        return axios.get(API_URL + "getChat?gameId=" + id, authHeader())
            .then(result => {
                return result.data
            })
            
    }

    sendMessage(message) {
        var player = getLoggedInUser()
        var gameId = store.getters.getCurrentGame.id
        return axios.post(API_URL + "newMessage",
            {
                gameId: gameId,
                playerId: player.id,
                playerName: player.username,
                message: message
            }, authHeader())
            .then(result => {
                return result.data
            })
            .catch(() => {
                negativeNotify("Message could not be send")
            })
    }

    createChat(gameId){
        return axios.get(API_URL + "createChat?gameId="+gameId
        , authHeader())
            .then(result => {
                return result.data
            })
            .catch(() => {
                negativeNotify("The game chat could not be created. Better luck next time!")
            })
    }
    }

export default new ChatService();
