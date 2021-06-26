<template>
  <q-card class="q-ma-sm playerCard" flat bordered>
    <q-item>
      <q-item-section avatar>
        <q-avatar size="xl">
          <img
            v-if="!player.ai"
            src="https://cdn.quasar.dev/img/boy-avatar.png"
            :class="getClass()"
          />
          <img
            v-if="player.ai"
            src="@/assets/cute-robot.jpg"
            :class="getClass()"
          />
        </q-avatar>
      </q-item-section>

      <q-item-section>
        <q-item-label class="text-weight-bold" :class="getPlayerColor()">{{ username }}</q-item-label>
        <q-item-label caption v-if="player.ai"> AI </q-item-label>
      </q-item-section>
      <q-item-section class="column items-center" side>
        <q-icon color="accent" name="mdi-crown" />
        <div class="q-mr-sm text-bold text-accent text-subtitle1">
          {{ player.victoryPointsAmount }}
        </div>
      </q-item-section>
    </q-item>

    <q-separator />

    <q-card-section vertical>
      <div v-show="isLoggedInUser()">
        <q-card-section>
          <ResourceChip
            v-for="resource in getResourceTypes()"
            v-bind:key="resource"
            :resourceType="resource"
            :amount="player.resources[resource]"
          />
        </q-card-section>
        <q-separator horizontal />
      </div>

      <q-card-section>
        <q-item>
          <q-item-section v-if="this.player.achievements.length > 0">
            <q-item-label>Achievements:</q-item-label>
            <div v-if="this.player.achievements.includes('LARGEST_ARMY')">
              <q-icon
                name="military_tech"
                class="text-lime-10"
                style="font-size: 2em"
              />
              Largest Army
            </div>
            <div v-if="this.player.achievements.includes('LONGEST_ROAD')">
              <q-icon
                name="o_construction"
                class="text-blue-grey-8"
                style="font-size: 2em"
              />
              Longest Road
            </div>
          </q-item-section>
        </q-item>
      </q-card-section>

      <div v-if="isLoggedInUser() && this.player.cards.length > 0">
        <q-separator horizontal />
        <q-card-section class="q-mb-md q-ml-lg">
          <CardStack :cards="knightCards" />
          <CardStack :cards="victoryCards" />
          <CardStack :cards="monopolyCards" />
          <CardStack :cards="roadbuildingCards" />
          <CardStack :cards="yopCards" />
        </q-card-section>
      </div>
    </q-card-section>
  </q-card>
</template>
<style scoped lang="scss">
q-card-section {
  padding: 8px;
}

.q-card__section .q-card__section--vert {
  padding: 8px;
}

.playerCard {
  width: 100%;
}

.playerOne {
  color: $player1;
}

.playerTwo {
  color: $player2;
}

.playerThree {
  color: $player3;
}

.playerFour {
  color: $player4;
}

.glowing-border {
  border: 4px solid #dadada;
  border-radius: 7px;
  outline: none;
  border-color: #9ecaed;
  box-shadow: 0 0 10px #9ecaed;
}
</style>
<script>
import { mapGetters } from "vuex";
import ResourceChip from "../ResourceChip";
import CardStack from "./CardStack";
import { getLoggedInUser, getAIName } from "../../services/player-methods";
import UserService from "../../services/user.service";
export default {
  name: "Player",
  props: {
    player: Object,
  },
  data() {
    return {
      username: String,
    };
  },
  components: {
    ResourceChip,
    CardStack,
  },
  computed: {
    knightCards() {
      return this.player.cards.filter((c) => c.cardType == "KNIGHT");
    },
    victoryCards() {
      return this.player.cards.filter((c) => c.cardType == "VICTORY_POINT");
    },
    monopolyCards() {
      return this.player.cards.filter((c) => c.cardType == "MONOPOLY");
    },
    roadbuildingCards() {
      return this.player.cards.filter((c) => c.cardType == "ROAD_BUILDING");
    },
    yopCards() {
      return this.player.cards.filter((c) => c.cardType == "YEAR_OF_PLENTY");
    },
  },
  created() {
    if (this.player.ai) {
      this.username = getAIName(this.player.userId);
    } else {
      UserService.getUsername(this.player.userId).then(
        (result) => (this.username = result)
      );
    }
  },
  methods: {
    ...mapGetters(["getResourceTypes"]),
    isLoggedInUser() {
      var user = getLoggedInUser();
      if (user != null && user.id == this.player.userId) {
        return true;
      }
      return false;
    },
    getClass() {
      if (
        this.$store.getters.getCurrentPlayer.playerId == this.player.playerId
      ) {
        return "glowing-border";
      } else return " ";
    },
    getPlayerColor() {
      if (this.player.playerId == 1) {
        return "playerOne";
      }
      if (this.player.playerId == 2) {
        return "playerTwo";
      }
      if (this.player.playerId == 3) {
        return "playerThree";
      }
      if (this.player.playerId == 4) {
        return "playerFour";
      }
    },
  },
};
</script>