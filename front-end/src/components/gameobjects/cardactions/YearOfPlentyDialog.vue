<template>
  <q-dialog ref="dialog" @hide="onDialogHide" persistent>
    <q-card class="q-dialog-plugin">
      <q-card-section>
        <div class="text-h6">Play a Year of Plenty Card</div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
        <div>Select 2 resources:</div>
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
              :isMax="isMax()"
              @add="addResource(resource)"
              @remove="removeResource(resource)"
            />
          </div>
        </q-card-section>
      </q-card-section>

      <q-card-actions align="right" class="bg-white text-teal">
        <q-btn flat label="cancel" @click="onCancelClick()" />
        <q-btn flat :disable="total<2" label="get resources"  @click="onUseClick()" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>
<script>
import { mapGetters } from "vuex";
import ResourceChip from "../../ResourceChip";
import PlusMinusButton from "../../actiondrawercomponents/PlusMinusButton";
export default {
  name: "YearOfPlentyDialog",
  components: {
    ResourceChip,
    PlusMinusButton,
  },
  created() {
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
      total: 0
    };
  },
  methods: {
    ...mapGetters(["getResourceTypes"]),
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
    isMax(){
        if(this.total>=2){
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
      var res1=null
      var res2=null

        for (var resource of this.getResourceTypes()) {
            if(this.chosenResources[resource]==2){
                res1=resource
                res2=resource
            }
            if(this.chosenResources[resource]==1){
                if(res1==null){
                    res1=resource
                }else{ res2=resource}
            }
        }
      this.$emit('ok',{res1, res2})
      this.hide()
    },
    onCancelClick () {
      this.hide()
    }
  },
};
</script>
