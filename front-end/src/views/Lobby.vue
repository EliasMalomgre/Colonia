<template>
  <div class="window-height row justify-center items-center">
    <q-spinner color="primary" size="6em" />
    <div class="column">
      <div class="text-h5">
        Players who joined the lobby: {{ joinedPlayers }}
      </div>
      <div v-if="!playerIsHost">Waiting for the host to start the game ...</div>
      <div v-if="this.lobby != null && playerIsHost">
        Would you like to start the game?
      </div>
      <q-btn
        v-if="playerIsHost"
        @click="startGame"
        color="primary"
        label="Start game"
      />
    </div>
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
import GameService from "../services/game.service";
import { getLoggedInUser } from "../services/player-methods";
import ChatService from "../services/chat-service";
const io = require("socket.io-client");
const socket = io("http://localhost:8082/lobby", {
  reconnectionDelayMax: 10000,
});

export default {
  name: "Lobby",
  props: {
    id: String,
  },
  mounted() {
    socket.connect();
    socket.emit("create", this.id);
    socket.on("goToGame", (data) => {
      //Route to game
      this.$router.push({ name: "Game", params: { id: data } });
    });
    socket.on("userAmountChanged", (data) => {
      this.joinedPlayers = data;
      this.$store.dispatch("updateLobby", this.id);
    });
  },
  created () {
    if(this.lobby != null && this.lobby.amountOfHuman==0){
      this.startGame()
    }
  },
  beforeDestroy() {
    socket.disconnect();
  },
  data() {
    return {
      joinedPlayers: 0,
    };
  },
  computed: {
    lobby() {
      return this.$store.getters.getLobby;
    },
    playerIsHost() {
      if (this.lobby == null) return false;
      return this.lobby.host.id == getLoggedInUser().id;
    },
  },
  methods: {
    startGame() {
      var userIds = new Array();
      userIds.push(this.lobby.host.id);

      this.lobby.lobbyUsers.forEach((user) => {
        userIds.push(user.id);
      });

      //create game
      GameService.createGame(userIds, this.lobby.amountOfAI, this.lobby.host.id).then(
        (result) => {
          socket.emit("startGame", result.id);
          ChatService.createChat(result.id);
          this.$router.push({ name: "Game", params: { id: result.id } });
          this.$store.dispatch('setLobby', null)
        }
      );
    },
  },
};
</script>
