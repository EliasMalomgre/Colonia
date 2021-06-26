
import { store } from '../store'

export function getLoggedInUser(){
    return JSON.parse(localStorage.getItem('user'));
}



export function getLoggedInPlayer(){
    var user = getLoggedInUser()
    return store.getters.getGamePlayerByUserId(user.id)
}

export function getAIName(userId){
    if(userId=='AI1'){
        return "Terminator"
    }
    if(userId=='AI2'){
        return "GLaDOS"
    }
    if(userId=='AI3'){
        return "Monte Carlo"
    }
    if(userId=='AI4'){
        return "PizzaGod"
    }

}



