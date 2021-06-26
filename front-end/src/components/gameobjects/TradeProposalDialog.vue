<template>
  <q-dialog ref="dialog" @hide="onDialogHide" position="right" persistent>
    <q-card>
      <q-card-section>
        <div class="text-h6" v-if="traderName">
          {{ traderName }} proposed a trade!
        </div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
        <div class="text-h6">You receive</div>
        <q-card-section horizontal class="q-ma-md justify-center">
          <div
            v-bind:key="resource"
            class="row"
            no-wrap
            v-for="resource in this.getResourceTypes()"
          >
            <ResourceChip
              :resourceType="resource"
              :amount="getResourceVal(resource, 'give')"
              v-if="getResourceVal(resource, 'give')"
            />
          </div>
        </q-card-section>
        <p v-if="!canAccept" class="text-red">
          You do not have enough resources to accept this trade!
        </p>

        <div class="text-h6">In return you give</div>

        <q-card-section horizontal class="q-ma-md justify-center">
          <div
            v-bind:key="resource"
            class="row"
            no-wrap
            v-for="resource in this.getResourceTypes()"
          >
            <ResourceChip
              :resourceType="resource"
              :amount="getResourceVal(resource, 'get')"
              v-if="getResourceVal(resource, 'get')"
            />
          </div>
        </q-card-section>
      </q-card-section>
      <q-card-actions align="right" class="bg-white">
        <q-btn flat color="negative" label="Decline" @click="onDeclineClick" />
        <q-btn flat label="Accept" @click="onAcceptClick" :disable="!canAccept" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<style>
</style>
<script>
import { mapGetters } from "vuex";
import ResourceChip from "../ResourceChip";
import GameService from "../../services/game.service";
import UserService from "../../services/user.service";
import { getLoggedInPlayer, getAIName } from "../../services/player-methods";
export default {
  components: {
    ResourceChip,
  },
  data() {
    return {
      traderName: "",
    };
  },
  computed: {
    traderequest() {
      return this.$store.getters.getTradeRequest;
    },
    askingPlayer() {
      return this.$store.getters.getGamePlayerByPlayerId(
        this.traderequest.askingPlayer
      );
    },
    canAccept() {
      var resources = getLoggedInPlayer().resources;
      var acceptable = true;
      this.getResourceTypes().forEach((element) => {
        if (this.traderequest.toReceiveResources[element] > resources[element]) {
          acceptable = false;
        }
      });
      return acceptable;
    },
  },
  created() {
    this.setTraderName();
  },
  methods: {
    ...mapGetters(["getResourceTypes"]),
    getResourceVal(resource, obj) {
      if (obj == "get") {
        return this.traderequest.toReceiveResources[resource];
      }
      if (obj == "give") {
        return this.traderequest.toSendResources[resource];
      }
    },
    show() {
      this.$refs.dialog.show();
    },

    hide() {
      this.$refs.dialog.hide();
    },

    onDialogHide() {
      this.$emit("hide");
    },

    onAcceptClick() {
      GameService.acceptTrade(this.traderequest.id);

      this.$emit("ok");
      this.hide();
    },
    setTraderName() {
      if (this.askingPlayer.ai) {
        this.traderName = getAIName(this.askingPlayer.userId);
      } else
        UserService.getUsername(this.askingPlayer.userId).then((result) => {
          this.traderName = result;
        });
    },

    onDeclineClick() {
      this.hide();
    },
  },
};
</script>