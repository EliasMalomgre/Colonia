<template>
  <q-dialog position="right" seamless v-model="dialog" @hide="onHide()">
    <q-card style="max-width: 500px">
      <q-card-section>
        <div class="row">
          <div class="text-h6">Building Costs</div>
          <q-space />
          <q-btn icon="close" flat round dense v-close-popup />
        </div>
        <div class="text-grey">
          To build/upgrade a settlement, just click it when it is your turn!
          Only the positions available to build on will be shown. <br />
          <b>No targets will be shown if you do not have enough resources</b>
        </div>
      </q-card-section>
      <q-separator />
      <div v-if="buildingCosts">
        <div v-for="cost in buildingCosts" v-bind:key="cost.objName">
          <q-card-section vertical class="column items-center">
            <div class="text-h6 q-ma-xs self-center">{{ cost.objName }}</div>
            <q-card-section horizontal class="justify-center">
              <ResourceChip
              class="q-ma-sm"
                v-if="cost.woolCost != 0"
                :resourceType="'wool'"
                :amount="cost.woolCost"
              />

              <ResourceChip
                v-if="cost.brickCost != 0"
                :resourceType="'brick'"
                :amount="cost.brickCost"
              />
              <ResourceChip
                v-if="cost.grainCost != 0"
                :resourceType="'grain'"
                :amount="cost.grainCost"
              />

              <ResourceChip
                v-if="cost.oreCost != 0"
                :resourceType="'ore'"
                :amount="cost.oreCost"
              />
              <ResourceChip
                v-if="cost.lumberCost != 0"
                :resourceType="'lumber'"
                :amount="cost.lumberCost"
              />
            </q-card-section>
          </q-card-section>
        </div>
      </div>
    </q-card>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>>
<script>
import ResourceChip from "../ResourceChip";
export default {
  name: "BuildingCostDialog",
  components: {
    ResourceChip,
  },
  props: {
    dialog: Boolean,
  },
  computed: {
    buildingCosts() {
      return this.$store.getters.getBuildingCosts;
    },
  },
  methods: {
    onHide() {
      this.$emit("dialog-hide");
    },
  },
};
</script>
