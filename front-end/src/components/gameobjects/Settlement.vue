<template>
  <div :class="getClasses()" v-show="isShown" @click="upgradeSettlement()">
    <q-tooltip
      content-class="bg-purple"
      v-if="!isTarget && !isCity && canUpdate"
      content-style="font-size: 16px"
      :offset="[10, 10]"
    >
      Click to upgrade!
    </q-tooltip>
  </div>
</template>
<style lang="scss">
/*HOUSES*/
.village {
  position: absolute;
  height: 60px;
  width: 60px;
  z-index: 800;
  transform: rotate(90deg);
}

.village1 {
  background: url("../../assets/settlements/village1.svg") no-repeat center
    center;
}

.village2 {
  background: url("../../assets/settlements/village2.svg") no-repeat center
    center;
}

.village3 {
  background: url("../../assets/settlements/village3.svg") no-repeat center
    center;
}

.village4 {
  background: url("../../assets/settlements/village4.svg") no-repeat center
    center;
}

/*TARGETS */
.target {
  width: 20px;
  height: 20px;
  border: 2px dashed black;
  position: absolute;
  z-index: 800;
}

.target:hover {
  background-color: yellow;
}

.target::before {
  display: none;
}

/*CITIES */
.city {
  position: absolute;
  height: 60px;
  width: 60px;
  z-index: 800;
  transform: rotate(90deg);
}

.city1 {
  background: url("../../assets/settlements/city1.svg") no-repeat center center;
}

.city2 {
  background: url("../../assets/settlements/city2.svg") no-repeat center center;
}

.city3 {
  background: url("../../assets/settlements/city3.svg") no-repeat center center;
}

.city4 {
  background: url("../../assets/settlements/city4.svg") no-repeat center center;
}

/* POSITIONS */
.top {
  top: 35px;
  left: 100px;
}

.left {
  top: -30px;
  left: 65px;
}
.targettop {
  top: 55px;
  left: 100px;
}

.targetleft {
  top: -15px;
  left: 65px;
}
</style>
<script>
import {getLoggedInPlayer} from '../../services/player-methods'

export default {
  name: "Settlement",
  props: {
    settlement: Object,
    isCity: Boolean,
    isTarget: Boolean,
    playerId: Number,
  },
  computed: {
    isShown() {
      if (this.isTarget) {
        //if target only show when allowed
        return this.$store.getters.getShowSettlementTargets;
      }
      // if not a target, always show
      return true;
    },
    canUpdate(){
      var currPlayer = this.$store.getters.getCurrentPlayer
      if(getLoggedInPlayer().playerId == this.playerId && currPlayer.playerId == this.playerId){
        return true
      }
      else return false

    }
  },
  methods: {
    getClasses() {
      if (this.isTarget) {
        if (this.settlement.direction == "TOP") {
          return "target targettop";
        } else {
          return "target targetleft";
        }
      } else {
        return this.getTypeClass() + this.settlement.direction.toLowerCase();
      }
    },
    getTypeClass() {
      if (this.isCity) {
        switch (this.playerId) {
          case 1:
            return "city city1 ";
          case 2:
            return "city city2 ";
          case 3:
            return "city city3 ";
          case 4:
            return "city city4 ";
        }
      } else{
        switch (this.playerId) {
          case 1:
            return "village village1 ";
          case 2:
            return "village village2 ";
          case 3:
            return "village village3 ";
          case 4:
            return "village village4 ";
        }
      } 
    },
    upgradeSettlement() {
      if (this.isTarget) {
        // send "build" to backend
        this.$emit("build-settlement", this.settlement);
        this.isTarget = false;
      } else if (!this.isCity) {
        // send "upgrade" to backend
        if(this.canUpdate){
          this.$emit("upgrade-settlement", this.settlement);
        }
      }
    },
  },
};
</script>