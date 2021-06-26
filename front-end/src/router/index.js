import Vue from 'vue'
import VueRouter from 'vue-router'
import Game from "../views/Game"
import Login from "../views/Login"
import Register from "../views/Register"
import Home from "../views/Home"
import Lobby from "../views/Lobby"
import RequestPasswordReset from "../views/RequestPasswordReset"
import ResetPassword from "../views/ResetPassword"
import ConfirmAccount from "../views/ConfirmAccount"
import MyAccount from "../views/MyAccount"


Vue.use(VueRouter)


const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/game/:id',
    name: 'Game',
    component: Game,
    props: true
  },
  {
    path: '/lobby/:id',
    name: 'Lobby',
    component: Lobby,
    props: true
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    props: route => ({ invitation: route.query.invitation })

  },
  {
    path: '/requestPasswordReset',
    name: 'RequestPasswordReset',
    component: RequestPasswordReset
  },
  {
    path: '/resetPassword',
    name: 'ResetPassword',
    component: ResetPassword,
    props: route => ({ token: route.query.token })
    },
    {
      path: '/activateAccount',
      name: 'ConfirmAccount',
      component: ConfirmAccount,
      props: route => ({ token: route.query.token })
    },
    {
      path: '/myAccount',
      name: 'MyAccount',
      component: MyAccount
    },
]

const router = new VueRouter({
  routes
})

//SECURED ROUTING
router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register','/requestPasswordReset','/resetPassword','/activateAccount'];
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('user');

  // trying to access a restricted page + not logged in
  // redirect to login page
  if (authRequired && !loggedIn) {
    next('/login');
  } else {
    next();
  }
});

export default router
