<template>
  <div>
    <q-drawer
      v-model="drawer"
      show-if-above
      :mini="miniState"
      @mouseover="miniState = false"
      @mouseout="miniState = true"
      mini-to-overlay
      :width="250"
      :breakpoint="500"
      bordered
      content-class="bg-grey-3"
    >
      <q-scroll-area class="fit">
        <q-list padding>
          <q-item
            clickable
            v-ripple
            @click="tradedialog = true"
            :disable="!tradeAllowed"
          >
            <q-item-section avatar>
              <q-icon name="store" />
            </q-item-section>

            <q-item-section> Trade with a player </q-item-section>
          </q-item>

          <q-item
            clickable
            v-ripple
            @click="bankTrade()"
            :disable="!tradeAllowed"
          >
            <q-item-section avatar>
              <q-icon name="euro" />
            </q-item-section>

            <q-item-section> Trade with the bank </q-item-section>
          </q-item>

          <q-separator />

          <q-item
            clickable
            v-ripple
            :disable="!rollDiceAllowed && !initialRollAllowed"
            :active="rollDiceAllowed || initialRollAllowed"
            @click="rollDice()"
          >
            <q-item-section avatar>
              <q-icon name="o_casino" />
            </q-item-section>

            <q-item-section> Dice </q-item-section>
          </q-item>

          <q-separator />

          <q-item
            clickable
            v-ripple
            @click="activateBuild()"
            :disable="!buildingAllowed"
          >
            <q-item-section avatar>
              <q-icon name="build" />
            </q-item-section>

            <q-item-section> Build / View build pricing </q-item-section>
          </q-item>

          <q-item
            clickable
            v-ripple
            @click="buyCard()"
            :disable="!buyCardAllowed"
          >
            <q-item-section avatar>
              <q-icon name="style" />
            </q-item-section>

            <q-item-section> Buy development card </q-item-section>
          </q-item>
          <q-separator />

          <q-item clickable v-ripple @click="$emit('show-chat')">
            <q-item-section avatar>
              <q-badge v-if="unseenMessagesPresent" color="orange" floating>
                ∞
              </q-badge>
              <q-icon name="o_question_answer" />
            </q-item-section>

            <q-item-section> Chat </q-item-section>
          </q-item>

          <q-separator />

          <q-item
            clickable
            v-ripple
            @click="endTurn()"
            :disable="!endTurnAllowed"
          >
            <q-item-section avatar>
              <q-icon name="cancel" />
            </q-item-section>

            <q-item-section> End turn </q-item-section>
          </q-item>

          <q-separator />

          <q-item v-if="isHost" clickable v-ripple @click="pause()">
            <q-item-section avatar>
              <q-icon name="mdi-pause" />
            </q-item-section>

            <q-item-section> Pause game </q-item-section>
          </q-item>


            <q-separator />
            <q-item v-if="isHost" clickable v-ripple @click="endGameEarly()">
              <q-item-section avatar>
                <q-icon name="mdi-stop" />
              </q-item-section>

              <q-item-section> Stop game </q-item-section>
            </q-item>
          <q-separator />
        </q-list>
      </q-scroll-area>
    </q-drawer>
    <transition name="bounce">
      <Dice
        :firstDice="rolledDices[0]"
        :secondDice="rolledDices[1]"
        v-show="showDice"
      />
    </transition>

    <BuildingCostDialog
      :dialog="buildingcostdialog"
      @dialog-hide="disableBuildingOptions()"
    />
    <NewTradeDialog :dialog="tradedialog" @trade-close="tradedialog = false" />
  </div>
</template>
<style lang="scss">
.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.5);
  }
  100% {
    transform: scale(1);
  }
}
</style>
<script>
import { mapActions } from "vuex";
import BuildingCostDialog from "./actiondrawercomponents/BuildingCostDialog";
import Dice from "./actiondrawercomponents/Dice";
import NewTradeDialog from "./actiondrawercomponents/NewTradeDialog";
import GameService from "../services/game.service";
import CardService from "../services/card.service";
import BankTradeDialogVue from "./actiondrawercomponents/BankTradeDialog.vue";

export default {
  name: "ActionsDrawer",
  components: {
    BuildingCostDialog,
    Dice,
    NewTradeDialog,
  },
  data() {
    return {
      rolledDices: [],
      showDice: false,
      drawer: false,
      miniState: true,
      buildingcostdialog: false,
      tradedialog: false,
    };
  },
  computed: {
    isHost() {
      var hostId = this.$store.getters.getGameHostId;
      if (
        hostId != null &&
        hostId != undefined &&
        hostId == JSON.parse(localStorage.getItem("user")).id
      ) {
        return true;
      }

      return false;
    },
    rollDiceAllowed() {
      return this.$store.getters.getRollDiceAllowed;
    },
    initialRollAllowed() {
      return this.$store.getters.getInitialRollAllowed;
    },
    buildingAllowed() {
      return this.$store.getters.getBuildingAllowed;
    },
    buyCardAllowed() {
      return this.$store.getters.getBuyCardAllowed;
    },
    tradeAllowed() {
      return this.$store.getters.getTradeAllowed;
    },
    endTurnAllowed() {
      return this.$store.getters.getEndTurnAllowed;
    },
    unseenMessagesPresent() {
      return this.$store.getters.getUnseenMessagesPresent;
    },
  },
  methods: {
    ...mapActions([
      "updateShowSettlementTargets",
      "updateShowRoadTargets",
      "loadPossibleSettlementPlacementsForCurrentPlayer",
      "loadPossibleRoadPlacementsForCurrentPlayer",
    ]),
    rollDice() {
      if (this.initialRollAllowed) {
        GameService.initialRoll().then((result) => {
          this.rolledDices = result;
          this.showDice = true;
          //show for 3 sec
          setTimeout(() => (this.showDice = false), 3000);
          this.$emit("reload-game-data");
        });
      } else {
        GameService.rollDice().then((result) => {
          this.rolledDices = result;
          this.showDice = true;
          //show for 3 sec
          setTimeout(() => (this.showDice = false), 3000);
        });
      }
    },
    buyCard() {
      CardService.buyCard();
    },
    bankTrade() {
      this.$q.dialog({
        component: BankTradeDialogVue,
        parent: this,
      });
    },
    endTurn() {
      GameService.endTurn();
      //so player will be notified when it is his turn again
      this.$store.dispatch("updatePlayerNotifiedTurn", false);
    },
    activateBuild() {
      this.loadPossibleSettlementPlacementsForCurrentPlayer();
      this.loadPossibleRoadPlacementsForCurrentPlayer();
      this.updateShowSettlementTargets(true);
      this.updateShowRoadTargets(true);
      this.buildingcostdialog = true;
    },
    disableBuildingOptions() {
      this.buildingcostdialog = false;
      this.updateShowSettlementTargets(false);
      this.updateShowRoadTargets(false);
    },
    pause() {
      this.$q
        .dialog({
          title: "Pause game",
          message: "Are you sure you want to pause the game?",
          ok: {
            label: "Pause Game",
          },
          cancel: {
            label: "Go Back",
          },
          persistent: true,
        })
        .onOk(() => {
          GameService.pause()
        });
    },
    endGameEarly() {
      this.$q
        .dialog({
          title: "End game",
          message: "Are you sure you want to end the game?",
          ok: {
            label: "end game",
          },
          cancel: {
            label: "Go Back",
          },
          persistent: true,
        })
        .onOk(() => {
          GameService.stopEarly().then(() => {
            this.$emit("ended-early");
            this.$router.push("/");
          });
        });
    },
  },
};
</script>