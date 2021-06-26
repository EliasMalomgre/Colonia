import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:4001/api/auth/';
const USERACTIONS_API_URL='http://localhost:4001/api/user/'

class AuthService {
  login(user) {
    return axios
      .post(API_URL + 'signin', {
        username: user.username,
        password: user.password
      })
      .then(response => {
        if (response.data.token) {
          localStorage.setItem('user', JSON.stringify(response.data));
        }

        return response.data;
      });
  }

  logout() {
    localStorage.removeItem('user');
  }

  register(user) {
    return axios.post(API_URL + 'signup', {
      username: user.username,
      email: user.email,
      password: user.password
    });
  }

  registerByInvite(registration) {
    return axios.post(API_URL + 'registerWithInvite?lobbyId='+registration.invite, {
      username: registration.user.username,
      email: registration.user.email,
      password: registration.user.password
    });
  }

  resetPassword(resetObject){
    return axios.post(USERACTIONS_API_URL + 'resetPassword', resetObject);
  }
  requestPasswordReset(email){
    return axios.post(USERACTIONS_API_URL + 'requestPasswordReset?email='+email);
  }
  activateAccount(activation){
    return axios.post(API_URL+'validateUser?token='+activation.token,{});
  }
  updateAccount(newData){
    return axios.post(USERACTIONS_API_URL+'updateUser',newData,authHeader())
  }
}

export default new AuthService();