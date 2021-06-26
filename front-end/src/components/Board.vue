<template>
  <div class="boardcontainer">
    <ol
      v-for="number in numbers"
      :key="number"
      :class="{ odd: number % 2 === 0, even: number % 2 !== 0 }"
    >
      <Tile v-if="isRowWithSpacer(number)" :isSpacer="true" />
      <Tile v-for="tile in getTilesForRow(number)" 
      v-on:click.native="tryMoveRobberHere(tile)"
      v-bind:key="tile.index"
      :tile="tile" />
    </ol>
  </div>

</template>
<style lang="scss">
$hex-size: 75px;

$number-size: 45px;

.boardcontainer {
  position: relative;
  width: 1000px !important;
  line-height: 1.3;
}
ol.even {
  position: relative;
  left: ($hex-size / 1.1);
}
ol.odd {
  position: relative;
  margin-top: -78px;
  margin-bottom: -80px;
}
</style>
<script>
import Tile from "./gameobjects/Tile";
import GameService from '../services/game.service'

export default {
  name: "Board",
  components: {
    Tile,
  },
  data () {
    return{
      numbers: new Array(-3, -2, -1, 0, 1, 2, 3)
    }

  },
  computed: {
    moveRobber (){
      return this.$store.getters.getMoveRobberAllowed
    }

  },
  methods: {
    getTilesForRow(zCoord) {
    var row = this.$store.getters.getTilesByZ(zCoord);

      if(row !== undefined){
         return row
      }
     
    },
    isRowWithSpacer(index) {
      const spacers = new Array(-3, -2, 2, 3);
      if (spacers.includes(index)) {
        return true;
      }
      return false;
    },
    tryMoveRobberHere(tile){
      if(this.moveRobber){
        GameService.moveRobber(tile.coordinate)
        this.$store.dispatch("updateMoveRobberAllowed", false)
      }
    }
  },
};
</script>