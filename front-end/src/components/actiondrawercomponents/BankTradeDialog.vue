<template>
  <q-dialog ref="dialog" @hide="onDialogHide">
    <q-stepper
      v-model="step"
      ref="stepper"
      color="primary"
      animated
      vertical
      class="q-dialog-plugin"
    >
      <q-step
        :name="1"
        title="Choose the resource you want to give up"
        icon="create"
        :done="step > 1"
      >
        <div class="column">
          <q-radio
            keep-color
            v-model="resourceToGive"
            val="WOOL"
            label="Wool"
            color="light-green"
          />
          <q-radio
            keep-color
            v-model="resourceToGive"
            val="LUMBER"
            label="Lumber"
            color="green-9"
          />
          <q-radio
            keep-color
            v-model="resourceToGive"
            val="BRICK"
            label="Brick"
            color="red-9"
          />
          <q-radio
            keep-color
            v-model="resourceToGive"
            val="GRAIN"
            label="Grain"
            color="yellow-8"
          />
          <q-radio
            keep-color
            v-model="resourceToGive"
            val="ORE"
            label="Ore"
            color="grey-14"
          />
        </div>
        <q-stepper-navigation>
          <q-btn
            :disable="resourceToGive == null"
            @click="getRatio()"
            color="primary"
            :label="'Continue'"
          />
        </q-stepper-navigation>
      </q-step>

      <q-step
        :name="2"
        title="Trade ratio"
        icon="create_new_folder"
        :done="step > 2"
      >
        The traderatio for this resource is :
        <br />
        <b> 1 : {{ ratio }} </b><br /> (You get 1 chosen resource when you give up {{ ratio }} {{ resourceToGive }} )
        <br />
        Would you like to continue with the trade?
        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            color="primary"
            :label="'Continue'"
          />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>

      <q-step
        :name="3"
        title="Choose the resource you want to receive"
        icon="create"
        :done="step >3"
      >
      <div v-if="tradePossible()" class="column">
      <p >Select the resource you want to receive:</p>
        
          <q-radio
            keep-color
            v-model="resourceToGet"
            val="WOOL"
            label="Wool"
            color="light-green"
          />
          <q-radio
            keep-color
            v-model="resourceToGet"
            val="LUMBER"
            label="Lumber"
            color="green-9"
          />
          <q-radio
            keep-color
            v-model="resourceToGet"
            val="BRICK"
            label="Brick"
            color="red-9"
          />
          <q-radio
            keep-color
            v-model="resourceToGet"
            val="GRAIN"
            label="Grain"
            color="yellow-8"
          />
          <q-radio
            keep-color
            v-model="resourceToGet"
            val="ORE"
            label="Ore"
            color="grey-14"
          />
        </div>

          <div v-if="!tradePossible()" class="row">

          <q-img
          class="col"
          :src="require('../../assets/sad_bunny.png')"
          :ratio="1"
          style="height: 150px; width: 75px;"
        ></q-img>

        <p class="col q-mt-md q-mr-sm"> Oh no, its seems that you don't have enough {{resourceToGive}} to trade right now! You can always try again later!</p>
        
        </div>


        

        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            color="primary"
            :label="'Continue'"
            :disable="!tradePossible() || resourceToGet == null"
          />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>

      <q-step :name="4" title="Confirmation" icon="done_outline">
         <div class="q-gutter-md q-ma-xs">
      Trade overview: <br/>
        You give: {{ratio}} {{resourceToGive}}<br/>
        You get: 1 {{resourceToGet}}<br>
        <br />
        Are you sure you want to proceed with this trade?
        </div>

        <q-btn @click="sendTrade()" class="q-mt-sm" color="primary" :label="'Make trade'" />
        <q-btn
          flat
          color="primary"
          @click="$refs.stepper.previous()"
          label="Back"
          class="q-ml-sm q-mt-sm"
        />
      </q-step>
    </q-stepper>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>
<script>
import TradeService from "../../services/trade.service";
import { getLoggedInPlayer } from "../../services/player-methods";
export default {
  name: "BankTradeDialog",
  data() {
    return {
      step: 1,
      resourceToGet: null,
      resourceToGive: null,
      ratio: null,
    };
  },
  computed: {
    player() {
      return getLoggedInPlayer();
    }
  },
  methods: {
    show() {
      this.$refs.dialog.show();
    },
    hide() {
      this.$refs.dialog.hide();
    },

    onDialogHide() {
      this.$emit("hide");
    },

    onCancelClick() {
      this.hide();
    },

    getRatio() {
      this.ratio = TradeService.getTradeRatio(this.resourceToGive).then((r) => {
        this.ratio = r;
        this.$refs.stepper.next();
      });
    },
    tradePossible(){
      if (this.player.resources[this.resourceToGive] >= this.ratio) {
        return true
      }else return false


    },

    sendTrade() {
      TradeService.tradeWithBank(this.resourceToGive, this.resourceToGet);
      this.$emit("ok");
      this.hide();
    },
  },
};
</script>