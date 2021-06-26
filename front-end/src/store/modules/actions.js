import { getLoggedInPlayer } from '../../services/player-methods'
import { Dialog } from 'quasar'




const state = {
    moveRobberAllowed: false,
    initialRoll: false,
    rollDiceAllowed: false,
    playCardAllowed: false,
    buildingAllowed: false,
    buyCardAllowed: false,
    tradeAllowed: false,
    initialPhase: false,
    endTurnAllowed: false,
    isTurn: false,
    playerHasBeenNotifed: false,

    hasToSteal: false,
    stealFromOpponents: null,


    showRoadTargets: true,
    showSettlementTargets: false

};
const mutations = {
    setMoveRobberAllowed(state, payload) {
        state.moveRobberAllowed = payload
    },

    setRollDiceAllowed(state, payload) {
        state.rollDiceAllowed = payload
    },
    setPlayCardAllowed(state, payload) {
        state.playCardAllowed = payload
    },
    setBuyCardAllowed(state, payload) {
        state.buyCardAllowed = payload
    },
    setBuildingAllowed(state, payload) {
        state.buildingAllowed = payload
    },
    setInitialDice(state, payload) {
        state.initialRoll = payload

    },
    setShowRoadTargets(state, payload) {
        state.showRoadTargets = payload
    },
    setShowSettlementTargets(state, payload) {
        state.showSettlementTargets = payload
    },
    setInitialPhase(state, payload) {
        state.initialPhase = payload
    },
    setIsTurn(state, payload) {
        state.isTurn = payload
    },
    setEndTurnAllowed(state, payload) {
        state.endTurnAllowed = payload
    },
    setTradeAllowed(state, payload){
        state.tradeAllowed = payload
    },
    setHasToSteal(state, payload){
        state.hasToSteal = payload
    },
    setStealOpponents(state, payload){
        state.stealFromOpponents = payload
    },
    setPlayerNotifiedTurn(state, payload){
        state.playerHasBeenNotifed = payload
    },

};
const actions = {
    updateMoveRobberAllowed({ commit }, payload) {
        commit('setMoveRobberAllowed', payload)
    },
    updateShowRoadTargets({ commit }, payload) {
        commit('setShowRoadTargets', payload)
    },
    updateShowSettlementTargets({ commit }, payload) {
        commit('setShowSettlementTargets', payload)
    },
    updateStealDialogData({ commit }, payload){
        commit('setStealOpponents', payload)
        commit('setHasToSteal', true)
        
    },
    getIsTurn({ commit, rootGetters }) {
        var currPlayer = rootGetters.getCurrentPlayer

        if (getLoggedInPlayer().playerId == currPlayer) {
            commit('setIsTurn', true)
        }else{
            commit('setIsTurn', false)
            commit('setEndTurnAllowed', false)
        }   
    },
    updatePlayerNotifiedTurn({ commit}, payload){
        commit('setPlayerNotifiedTurn', payload)

    },


    setPlayableActions({ commit, dispatch }) {
        var userActions = getLoggedInPlayer().remainingActions
        dispatch('getIsTurn')


            commit('setShowSettlementTargets', false)
            commit('setShowRoadTargets', false)
        

        //Initial Dice
        if (userActions.includes('INITROLL')) {
            Dialog.create({
                title: 'Initial rolls',
                message: 'It is time for the initial rolls! This will determine the order in which you play.'
                    + ' Click the dice button on your left to roll.'
            })
            commit('setInitialDice', true)
        } else {
            commit('setInitialDice', false)
        }

        //Roll Dice
        (userActions.includes('ROLL')) ? commit('setRollDiceAllowed', true) : commit('setRollDiceAllowed', false)


        //Initial phase (1&2)
        if (userActions.includes('INITIAL1') || userActions.includes('INITIAL2')) {

            commit('setInitialPhase', true)
            Dialog.create({
                title: 'Your turn to build!',
                message: 'You can now build one settlement and one road!'
            })

            dispatch('loadPossibleSettlementPlacementsForCurrentPlayer')
            dispatch('loadPossibleRoadPlacementsForCurrentPlayer')

            commit('setShowSettlementTargets', true)
            commit('setShowRoadTargets', false)
        } else {
            commit('setInitialPhase', false)
        }

        //End turn
        if(userActions.includes('END_TURN') ){
            commit('setEndTurnAllowed', true)
        }else{
            commit('setEndTurnAllowed', false)
        }

        //Play Card
        if (userActions.includes('PLAY_CARD')) {
            commit('setPlayCardAllowed', true)
        } else {
            commit('setPlayCardAllowed', false)
        }

        //Buy Card
        if (userActions.includes('BUY')) {
            commit('setBuyCardAllowed', true)
        } else {
            commit('setBuyCardAllowed', false)
        }

        //Build
        if (userActions.includes('BUILD')) {
            commit('setBuildingAllowed', true)
        } else {
            commit('setBuildingAllowed', false)
        }


        if (userActions.includes('MOVE_ROBBER')) {
            commit('setMoveRobberAllowed', true)
            Dialog.create({
                title: 'You get to move the robber!',
                message: 'Click on the tile you want to move the robber to. Remember that you get to steal a resource from a player who has a settlement next to that tile.'
              })
            
        } else{
            commit('setMoveRobberAllowed', false)
        }

        if (userActions.includes('TRADE')) {
            commit('setTradeAllowed', true)
        } else{
            commit('setTradeAllowed', false)
        }

    }



};

const getters = {
    getMoveRobberAllowed(state) {
        return state.moveRobberAllowed
    },
    getRollDiceAllowed(state) {
        return state.rollDiceAllowed
    },
    getPlayCardAllowed(state) {
        return state.playCardAllowed
    },
    getBuildingAllowed(state) {
        return state.buildingAllowed
    },
    getBuyCardAllowed(state) {
        return state.buyCardAllowed
    },
    getTradeAllowed(state) {
        return state.tradeAllowed
    },
    getInitialRollAllowed(state) {
        return state.initialRoll
    },
    getShowRoadTargets(state) {
        return state.showRoadTargets
    },
    getShowSettlementTargets(state) {
        return state.showSettlementTargets
    },
    getInitialPhase(state) {
        return state.initialPhase

    },
    getIsTurn(state) {
        return state.isTurn
    },
    getEndTurnAllowed(state){
        return state.endTurnAllowed
    },
    getDiscardResourcesNecessary(){
        return state.discardResources
    },
    getHasToSteal(state){
        return state.hasToSteal
    },
    getStealOpponents(state){
        return state.stealFromOpponents
    },
    getPlayerHasBeenNotified(state){
        return state.playerHasBeenNotifed
    }
};

export default {
    state, getters, mutations, actions
}

