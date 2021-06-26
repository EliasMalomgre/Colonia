import Vue from 'vue'
import Vuex from 'vuex' 
import board from './modules/board'
import user from './modules/user'
import actions from './modules/actions'
import { auth } from './modules/auth.module';

Vue.use(Vuex)

export const store = new Vuex.Store({
    state: {
    },
    mutations: { 
    },
    actions: {
    },
    getters: { },
    modules:{
      board,
      user,
      actions,
      auth
    }
  })
  