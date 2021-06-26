import UserService from '../../services/user.service'

const state = {
    loggedInUser:null,
    invitations: null,
    friendRequests:null,
    unseenMessagesPresent: false,
    lobby: null
};
 const mutations = {
     setLoggedInUser(state, payload){
         state.loggedInUser=payload
     },
     setInvitations(state, payload){
         state.invitations=payload
     },
     deleteInvite(state, payload){
        state.invitations = state.invitations.filter( inv => { inv.id !== payload})
     },
     setFriendRequests(state,payload){
         state.friendRequests=payload
     },
     setUnseenMessagesPresent(state, payload) {
        state.unseenMessagesPresent = payload
    },
    setLobby(state, payload){
        state.lobby = payload
    }
}
const actions = {
    getLoggedInUserData({ commit }) {
        return UserService.getLoggedInUser()

        .then(result => {
            commit('setLoggedInUser', result)
        })
   },
   getInvitations({commit}){
    return UserService.getInvitations()
    .then(result => {
        commit('setInvitations', result)
    })},
    getFriendRequests({commit}){
        return UserService.getFriendRequests()
        .then(result=>{
            commit('setFriendRequests',result)
        })
    },
    changeUnseenMessagesPresent({ commit }, payload) {
        commit('setUnseenMessagesPresent', payload)
    },
    deleteInvitation({ commit }, payload){
        UserService.declineInvitation(payload)
        .then(() => {

            commit('deleteInvite', payload)
        })        
    },
    //set complete lobby object at creation
    setLobby ({ commit }, payload){
        commit('setLobby', payload)

    },
    //update lobby as more users are joining
    updateLobby({ commit }, payload){
        UserService.getLobby(payload)
        .then(result => {
            commit('setLobby', result)
        })
    }
};
const getters = {
    getLoggedInUser(state) {
        return state.loggedInUser
    },
    getLoggedInUserFriends() {
        return state.loggedInUser.friends
    },
    getInvitations(state) {
        return state.invitations
    },
    getFriendRequests(){
        return state.friendRequests
    },
    getUnseenMessagesPresent(state) {
        return state.unseenMessagesPresent

    },
    getLobby(state){
        return state.lobby
    }
};

export default {
    state, getters, mutations, actions
}

