export default function authHeader() {
    let user = JSON.parse(localStorage.getItem('user'));
  
    if (user && user.token) {
      let config = {
        headers: {
          'Authorization': "Bearer "+ user.token,
        'UserId': user.id,
        'Content-Type': "application/json",
        'Access-Control-Allow-Origin': "*"
        }
      }
      return config
    } else {
      return {};
    }
  }
