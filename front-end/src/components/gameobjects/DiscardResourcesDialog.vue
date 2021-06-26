<template>
  <q-dialog ref="dialog" @hide="onDialogHide" persistent>
    <q-card class="q-dialog-plugin">
      <q-card-section>
        <div class="text-h6">The robber has been moved!</div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
          <div>Because you have more than 7 resources, you have to give up half. </div>
        <div>Select {{amountToGive}} resources to discard:</div>
        <br>

        <q-card-section horizontal>
          <div
            v-bind:key="resource"
            class="row"
            no-wrap
            v-for="resource in this.getResourceTypes()"
          >
            <ResourceChip
              :resourceType="resource"
              :amount="getResourceVal(resource)"
            />
            <PlusMinusButton
              :isMin="getResourceVal(resource) < 1"
              :isMax="isMax(resource)"
              @add="addResource(resource)"
              @remove="removeResource(resource)"
            />
          </div>
        </q-card-section>
      </q-card-section>

      <q-card-actions align="right" class="bg-white text-teal">
        <q-btn flat :disable="total!= amountToGive" label="discard"  @click="onUseClick()" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>
<script>
import { mapGetters } from "vuex";
import ResourceChip from "../ResourceChip";
import PlusMinusButton from "../actiondrawercomponents/PlusMinusButton";
import {getLoggedInPlayer} from '../../services/player-methods'
import GameService from '../../services/game.service'
export default {
  name: "DiscardResourcesDialog",
  components: {
    ResourceChip,
    PlusMinusButton,
  },
  created() {
    this.setAmountToGive();
    this.chosenResources = {
      BRICK: 0,
      ORE: 0,
      LUMBER: 0,
      WOOL: 0,
      GRAIN: 0,
    };
  },
  data() {
    return {
      chosenResources: Array,
      total: 0,
      amountToGive: 0
    };
  },
  methods: {
    ...mapGetters(["getResourceTypes"]),
    setAmountToGive(){
        var player = getLoggedInPlayer()
        if(player !== undefined && player.resourcesTotal>7){
            this.amountToGive=Math.floor(player.resourcesTotal/2)
        }else{
            this.hide()
        }

    },
    getResourceVal(resource){
        return this.chosenResources[resource]
    },
    addResource(resource){
     this.chosenResources[resource] += 1
     this.total++
    },
    removeResource(resource){
      this.chosenResources[resource] -= 1
      this.total--
    },
    isMax(resource){
      var player = getLoggedInPlayer()
        if(this.total>=this.amountToGive){
            return true
        }
        if(player.resources[resource]<=this.chosenResources[resource]){
          return true
        }
        return false
    },
    show () {
      this.$refs.dialog.show()
    },

    hide () {
      this.$refs.dialog.hide()
    },

    onDialogHide () {
      this.$emit('hide')
    },

    onUseClick () {
    GameService.discardResources(this.chosenResources)
      
      this.$emit('ok', this.chosenResources)
      this.hide()
    }
  },
};
</script>
