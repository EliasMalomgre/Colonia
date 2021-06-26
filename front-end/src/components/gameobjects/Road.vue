<template>
  <div :class="getClasses()" v-show="isShown" @click="addRoad()"></div>
</template>
<style lang="scss">
/*ROADS */
.road {
  border-radius: 3px;
  position: absolute;
  width: 9px;
  height: 58px;
  z-index: 150;
}

/* TARGET*/
.road.target {
  background-color: transparent;
  position: absolute;
  border: 2px dashed black;
}

.road.target:hover {
  background-color: yellow;
}

/*POSITIONS */
.w {
  right: 34px;
  bottom: 102px;
  transform: rotate(90deg);
}
.nw {
  left: 91px;
  top: 1px;
  transform: rotate(150deg);
}
.ne {
  left: 91px;
  top: 69px;
  transform: rotate(30deg);
}

.player1{
  background-color: #de8a66;
}

.player2{
 background-color: #bab8ba;
}

.player3{
  background-color: #8292b8;
}

.player4{
  background-color: #c49dbe;
}

//Colors


</style>
<script>
export default {
  name: "Road",
  props: {
    road: Object,
    isTarget: Boolean,
    playerId: Number
  },
  computed: {
    isShown() {
      //if target check whether visible
      if (this.isTarget) {
        return this.$store.getters.getShowRoadTargets
      }
      //if not a target the road is always visible
      return true;
    },
  },
  methods: {
    getClasses() {
      if (this.isTarget) {
        return "road target " + this.getPosition(this.road.cardDir);
      } else {
        return "road " + this.getPosition(this.road.cardDir) + this.getPlayerColor();
      }
    },
    getPosition(cardDir) {
      if (cardDir == "WEST") {
        return "w";
      }
      if (cardDir == "NORTH_WEST") {
        return "nw";
      }
      if (cardDir == "NORTH_EAST") {
        return "ne";
      }
    },
    addRoad() {
      if (this.isTarget) {
        this.$emit("build-road");
        this.isTarget = false;
      }
    },
    getPlayerColor(){
      if(this.playerId==1){
        return " player1"
      }
      if(this.playerId==2){
        return " player2"
      }
      if(this.playerId==3){
        return " player3"
      }
      if(this.playerId==4){
        return " player4"
      }

    }
  },
};
</script>