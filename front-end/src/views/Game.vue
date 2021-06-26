<template>
  <div>
    <ActionsDrawer
      @reload-game-data="reloadGame()"
      @show-chat="chatDialog = true"
      @ended-early="gameTerminated()"
    />
    <div class="row">
      <Board class="col" id="board" />
      <PlayersColumn class="col" />
      <Chat
        class="col"
        :gameId="id"
        :dialog="chatDialog"
        @new-message="newMessage()"
        @close-chat="closeChat()"
      />
    </div>
  </div>
</template>
<style>
#board {
  min-width: 1000px;
  max-width: 1000px;
}
</style>

<script>
import { mapActions } from "vuex";
import Board from "@/components/Board.vue";
import ActionsDrawer from "@/components/ActionsDrawer.vue";
import PlayersColumn from "@/components/PlayersColumn.vue";
import Chat from "@/components/actiondrawercomponents/Chat.vue";
import { getLoggedInPlayer } from "../services/player-methods";
import DiscardResourcesDialogVue from "../components/gameobjects/DiscardResourcesDialog.vue";
import TradeProposalDialogVue from "../components/gameobjects/TradeProposalDialog.vue";
import StealResourcesDialogVue from "../components/gameobjects/cardactions/StealResourcesDialog.vue";
import gameService from "../services/game.service";
import userService from '../services/user.service';

const io = require("socket.io-client");
const socket = io("http://localhost:8082/game", {
  reconnectionDelayMax: 10000,
});

export default {
  name: "Game",
  props: {
    id: String,
  },
  components: {
    Board,
    ActionsDrawer,
    PlayersColumn,
    Chat,
  },
  data() {
    return {
      chatDialog: false,
    };
  },
  computed: {
    stealDialog() {
      return this.$store.getters.getHasToSteal;
    },
    opponentsToStealFrom() {
      return this.$store.getters.getStealOpponents;
    },
    playerHasBeenNotifiedTurn(){
      return this.$store.getters.getPlayerHasBeenNotified
    },
    currentPlayer () {
      return this.$store.getters.getCurrentPlayer
    }
  },
  watch: {
    stealDialog(newVal, oldVal) {
      if (newVal && !oldVal) {
        this.showStealDialog();
      }
    },
  },
  async created() {
    await this.loadCurrentGame(this.id);
    this.loadBuildingCosts();
    await this.setPlayableActions();
    await this.loadPossibleRoadPlacementsForCurrentPlayer(this.id);
    await this.loadPossibleSettlementPlacementsForCurrentPlayer(this.id);
    

    socket.connect();
    socket.emit("create", this.id);

    socket.on("reloadData", () => {
      this.reloadGame();
    });

    socket.on("newTradeRequest", (playerId) => {
      if (getLoggedInPlayer().playerId == playerId) {
        this.loadCurrentGame(this.id).then(() => {
          this.showTradeProposalDialog();
        });
      }
    });
    socket.on("newAchievement", (playerId, achievement) => {
      if (getLoggedInPlayer().playerId == playerId) {
        //decide text to show
        var achievementText = " ";
        if (achievement == "LARGEST_ARMY") {
          achievementText = "Largest Army";
        }
        if (achievement == "LONGEST_ROAD") {
          achievementText = "Longest Road";
        }
        //show dialog
        this.$q
          .dialog({
            title: "Congratulations!",
            message:
              "You have received the " +
              achievementText +
              " achievement! The achievement and victory points are being added to your data",
          })
          .onDismiss(() => {
            // refresh game data
            this.reloadGame();
          });
      }
    });
    socket.on("pauseGame", () => {
      this.$q.dialog({
        title: "Game paused",
        message:
          "The game was paused by the host." +
          " You won't be able to perform any actions until the host resumes this game.",
      }).onDismiss(() =>{
        this.deleteGameInStore();
        this.$router.push("/");

      })
    });
    socket.on("gameStoppedByHost", () => {
      var hostId = this.$store.getters.getGameHostId;
      if (getLoggedInPlayer().userId != hostId) {
        this.$q
          .dialog({
            title: "Game stopped",
            message:
              "The game was ended early by the host. You will be returned to your home page.",
          })
          .onDismiss(() => {
            this.deleteGameInStore();
            this.$router.push({ name: "Home" });
          });
      }
    });
    socket.on("refreshBoard", () => {
      this.reloadGame();
    });

    socket.on("discard", (list) => {
      if (list.includes(getLoggedInPlayer().playerId)) {
        this.$q.dialog({
          component: DiscardResourcesDialogVue,
          parent: this,
        });
      }
    });

    socket.on("rolledSeven", (playerId) => {
      if (getLoggedInPlayer().playerId == playerId) {
        this.updateMoveRobberAllowed(true);
        this.$q.dialog({
          title: "You rolled a 7!!",
          message:
            "You now get to move the robber. Click on the tile you want to move him to. Remember that you get to steal a resource from a player with a settlement besides the new tile.",
        });
      }
    });

    socket.on("endGame", (playerId) => {
      var userIdWinner = this.$store.getters.getGamePlayerByPlayerId(playerId).userId

      userService.getUsername(userIdWinner)
      .then(name => {

        this.$q
        .dialog({
          title: "Game ended",
          message: "The game ended! The winner is "+ name,
        })
        .onDismiss(() => {
          this.deleteGameInStore();
          this.$router.push({ name: "Home" });
        });
      })
    });
  },
  methods: {
    ...mapActions([
      "loadCurrentGame",
      "loadBuildingCosts",
      "loadPossibleRoadPlacementsForCurrentPlayer",
      "loadPossibleSettlementPlacementsForCurrentPlayer",
      "setPlayableActions",
      "changeUnseenMessagesPresent",
      "updateMoveRobberAllowed",
    ]),
    reloadGame() {
      this.loadCurrentGame(this.id).then(() => {
        this.setPlayableActions();
        this.notifyPlayerTurn();
      });
    },
    closeChat() {
      this.chatDialog = false;
      this.changeUnseenMessagesPresent(false);
    },
    newMessage() {
      this.changeUnseenMessagesPresent(true);
    },
    showStealDialog() {
      this.$q
        .dialog({
          component: StealResourcesDialogVue,
          parent: this,
        })
        .onOk((opponentId) => {
          gameService.stealResources(opponentId);
        })
        .onDismiss(() => {
          this.loadCurrentGame(this.id);
          this.setPlayableActions();
          this.$store.commit("setHasToSteal", false);
        });
    },
    gameTerminated() {
      this.deleteGameInStore()
      socket.emit("gameTerminated");
    },
    deleteGameInStore () {
      this.$store.commit("setCurrentGame", null);
    },
    notifyPlayerTurn () {
      if(!this.playerHasBeenNotifiedTurn && this.currentPlayer.playerId==getLoggedInPlayer().playerId){
        this.$q.notify({
        message: 'It is your turn!',
        color: 'secondary',
        actions: [
          { label: 'Dismiss', color: 'white', handler: () => { } }
        ]
      })
      this.$store.dispatch('updatePlayerNotifiedTurn', true)

      }

    },
    showTradeProposalDialog() {
      this.$q.dialog({
        component: TradeProposalDialogVue,
        parent: this,
      });
    },
  },
};
</script>
