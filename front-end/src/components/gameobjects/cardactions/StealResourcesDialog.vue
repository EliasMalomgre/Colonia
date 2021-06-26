<template>
  <q-dialog ref="dialog" @hide="onDialogHide" persistent>
    <q-card class="q-dialog-plugin">
      <q-card-section>
        <div class="text-h6">Steal resource</div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
        <div>
          Choose from which opponent you want to steal a random resource:
        </div>
        <br />
        <q-select
          v-if="options != null"
          v-model="opponent"
          :options="options"
          label="Opponents"
        />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn
          flat
          color="primary"
          label="choose opponents"
          :disable="opponent == null"
          @click="onChooseOpponentClick(opponent)"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>
<script>
import { mapGetters } from "vuex";
import {getAIName } from '../../../services/player-methods'
import userService from "../../../services/user.service";
export default {
  name: "StealResourceDialog",

  data() {
    return {
      opponent: null ,
      options: Array,
    };
  },
  computed: {
    opponents() {
      return this.getStealOpponents();
    },
  },
  watch: {
    opponents() {
      this.createOptions();
    },
  },
  created() {
    this.createOptions();
  },
  methods: {
    ...mapGetters(["getStealOpponents"]),
    createOptions() {
      this.options = []
      if (this.opponents != null) {
        this.opponents.forEach((player) => {
          var gamePlayer = this.$store.getters.getGamePlayerByPlayerId(player);
          if (!gamePlayer.ai) {
             userService.getUsername(gamePlayer.userId).then( result => {
              this.options.push({
              label: result,
              value: player
            })
            })
            
          } else {
            this.options.push({
              label: getAIName(gamePlayer.userId),
              value: player
            })
          }
        });
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

    onChooseOpponentClick(opponent) {
      this.$emit("ok", opponent.value);
      this.hide();
    },
  },
};
</script>