import axios from 'axios';
axios.interceptors.response.use( (response) => {
  // Return a successful response back to the calling service
  return response;
}, (error) => {
  // Return any error which is not due to authentication back to the calling service
  if (error.response.status !== 401) {
    return new Promise((resolve, reject) => {
      reject(error);
    });
  }
  else{
    //unauthorized
    //Delete token from localStorage
    localStorage.removeItem("user");
    location.href = '/#/login';
  }
});
import { store } from '../store'
import authHeader from './auth-header';
import {negativeNotify} from './notifications'


const API_URL = 'http://localhost:4001/api/user/';

class UserService {
  getLoggedInUser(){
      return axios.get(API_URL + "getDataForLoggedInUser"
        , authHeader())
        .then(result => {
          return result.data
        })
  }

  createLobby(lobbyObj){
    return axios.post(API_URL + "createLobby",
     lobbyObj, authHeader())
      .then(result => {
        return result.data
      })
      .catch(() => {
        negativeNotify("Lobby could not be created. Try refreshing the page")
      })
  }

  getLobby(lobbyId){
    return axios.get(API_URL + "getLobby?lobbyId="+lobbyId, authHeader())
    .then(result => {
      return result.data
    })
    .catch(() => {
      negativeNotify("We could not find the lobby.")
    })
    
  }

  getInvitations(){
    return axios.get(API_URL + "getInvitations", authHeader())
    .then(result => {
      return result.data
    })
    .catch(() => {
      negativeNotify("We cannot find you invitations right now. Try refreshing the page")
    })
  }

  sendFriendRequest(username){
    return axios.post(API_URL+"sendFriendRequest?usernameOfFriend="+username,{},authHeader())
    .then(result=>{
      return result.data;
    }).catch(()=>negativeNotify("Friendrequest could not be send"))
  }
   getFriendRequests(){
    return axios.get(API_URL+"getFriendRequests",authHeader())
      .then(result=>{
        return result.data;
      }).catch(()=>
        negativeNotify("Couldn't get friend request"))
  }
  acceptFriendRequest(id){
    return axios.post(API_URL+"acceptFriendRequest?friendRequestId="+id,{},authHeader())
            .then(result=>{return result.data})
            .catch(()=>{
              negativeNotify("Couldn't accept friend request");
            });
  }
  declineFriendRequest(id){
    return axios.post(API_URL+"declineFriendRequest?friendRequestId="+id,{},authHeader())
  }
  declineInvitation(invitationId){
    return axios.post(API_URL+"declineInvite?invitationId="+invitationId,{},authHeader())
    .then(result=>{
      return result.data;
    }).catch(()=>{

    negativeNotify("Something went wrong declining this invite.")});
  }
  acceptInvitation(invitationId){
    return axios.post(API_URL + "acceptInvite?invitationId="+invitationId,{}, authHeader())
    .then(result => {
      store.commit('deleteInvite', invitationId)
      return result.data
    })
    .catch( () => {
      negativeNotify("Something went wrong accepting your invitation")
    })
  }

  getUsername(userid){
    return axios.get(API_URL + "getUsername?userid="+userid, authHeader())
    .then(result => {
      return result.data
    })
    .catch(() =>{
      negativeNotify("Player's username could not be found")
      return "Player"
    })
  }

}
export default new UserService();

