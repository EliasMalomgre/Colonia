<template>
  <q-dialog v-model="dialog" persistent>
    <q-card>
      <q-card-section>
        <div class="text-h6">New Trade</div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
        <div class="text-h6">To offer</div>

        <q-card-section horizontal>
          <div
            v-bind:key="resource"
            class="row"
            no-wrap
            v-for="resource in this.getResourceTypes()"
          >
            <ResourceChip
              :resourceType="resource"
              :amount="getResourceVal(resource, 'offer')"
            />

            <PlusMinusButton
              :isMin="getResourceVal(resource, 'offer') < 1"
              :isMax="isMax(resource)"
              @add="addResource(resource, 'offer')"
              @remove="removeResource(resource, 'offer')"
            />
          </div>
        </q-card-section>
        <div class="text-h6">To receive</div>

        <q-card-section horizontal>
          <div
            v-bind:key="resource"
            class="row"
            no-wrap
            v-for="resource in this.getResourceTypes()"
          >
            <ResourceChip
              :resourceType="resource"
              :amount="getResourceVal(resource, 'receive')"
            />

            <PlusMinusButton
              :isMin="getResourceVal(resource, 'receive') < 1"
              :isMax="false"
              @add="addResource(resource, 'receive')"
              @remove="removeResource(resource, 'receive')"
            />
          </div>
        </q-card-section>

        <q-card-section vertical>
          <div class="text-h6">Trade against</div>
          <q-select
            class="q-ma-sm"
            v-model="playerToTrade"
            :options="options"
            label="Opponent"
          />
        </q-card-section>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn
          flat
          color="negative"
          label="close"
          @click="closeDialog()"
        />
        <q-btn flat label="send request" :disable="playerToTrade==null" @click="startTrade()" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<style lang="scss" scoped>
.q-card__section .q-card__section--vert {
  padding: 4px;
}
</style>
<script>
import { mapGetters } from "vuex";
import ResourceChip from "../ResourceChip";
import PlusMinusButton from "./PlusMinusButton.vue";
import GameService from "../../services/game.service";
import { getLoggedInPlayer, getAIName } from "../../services/player-methods";
import UserService from "../../services/user.service";

export default {
  name: "NewTradeDialog",
  components: {
    ResourceChip,
    PlusMinusButton,
  },
  props: {
    dialog: Boolean,
  },
  created() {
    this.toReceive = {
      BRICK: 0,
      ORE: 0,
      LUMBER: 0,
      WOOL: 0,
      GRAIN: 0,
    };
    this.toOffer = {
      BRICK: 0,
      ORE: 0,
      LUMBER: 0,
      WOOL: 0,
      GRAIN: 0,
    };
    this.createOptions();
  },
  data() {
    return {
      toReceive: Object,
      toOffer: Object,
      playerToTrade: null,
      options: Array,
    };
  },
  computed : {
    player () {
      return getLoggedInPlayer()
    },
    players () {
      return this.getPlayers()
    }
  },
  watch : {
    players (){
      this.createOptions();
    }

  },
  methods: {
    ...mapGetters(["getResourceTypes", "getPlayers"]),
    getPlayerById(id) {
      var player = this.$store.getters.getGamePlayerByPlayerId(id);
      if (player !== null) {
        return player;
      }
      return null;
    },
    getResourceVal(resource, obj) {
      if (obj == "offer") {
        return this.toOffer[resource];
      }
      if (obj == "receive") {
        return this.toReceive[resource];
      }
    },
    addResource(resource, obj) {
      if (obj == "offer") {
        this.toOffer[resource] += 1;
      }
      if (obj == "receive") {
        this.toReceive[resource] += 1;
      }
    },
    removeResource(resource, obj) {
      if (obj == "offer") {
        this.toOffer[resource] -= 1;
      }
      if (obj == "receive") {
        this.toReceive[resource] -= 1;
      }
    },
    createOptions() {
      var options = [];
      if (this.players !== null) {
        this.players.forEach((player) => {
          if(player.ai){
            options.push({
                label: getAIName(player.userId),
                value: player.playerId
              });
          }
          else{
          UserService.getUsername(player.userId).then((name) => {
            if (
              this.player != null &&
              player.playerId != this.player.playerId
            ) {
              options.push({
                label: name,
                value: player.playerId,
              });
            }
          });
          }
        });
      }
      this.options = options;

      return options;
    },
    isMax(resource) {
      if (this.player !== null) {
        var current = this.toOffer[resource];
        var max = this.player.resources[resource];
        if (current >= max) {
          return true;
        } else {
          return false;
        }
      }
    },
    closeDialog(){
      this.resetDialogValues();
      this.$emit('trade-close')

    },
    startTrade() {
      GameService.startTrade(
        this.toOffer,
        this.toReceive,
        this.playerToTrade.value
      );
      this.resetDialogValues();
      this.$emit('trade-close')
    },
    resetDialogValues(){
      this.toReceive = {
      BRICK: 0,
      ORE: 0,
      LUMBER: 0,
      WOOL: 0,
      GRAIN: 0,
    };
    this.toOffer = {
      BRICK: 0,
      ORE: 0,
      LUMBER: 0,
      WOOL: 0,
      GRAIN: 0,
    };
    this.playerToTrade=null;

    }
  },
};
</script>