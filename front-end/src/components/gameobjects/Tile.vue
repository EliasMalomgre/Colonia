<template>
  <li :class="getClasses()" class="hex">
    <object :class="iconClass"></object>
    <TileNumber
      v-if="!isSpacer && this.tile.number !== 0"
      :number="this.tile.number"
      :textColor="this.textColor"
    />
    <Harbour
      v-if="!isSpacer && this.harbour !== undefined"
      :harbour="this.harbour"
      :tiletype="this.tile.tileType"
    />

    <div
      v-if="
        !isSpacer && SettlementTargets !== null && SettlementTargets.length > 0
      "
    >
      <Settlement
        v-for="(target, i) in SettlementTargets"
        v-bind:key="i"
        :settlement="target"
        :isCity="false"
        :isTarget="true"
        @build-settlement="build"
      />
    </div>
    <div v-if="!isSpacer && settlements !== null && settlements.length > 0">
      <Settlement
        v-for="(settlement, i) in settlements"
        v-bind:key="i"
        :settlement="settlement.coordinate"
        :isCity="settlement.city"
        :isTarget="false"
        :playerId="settlement.playerId"
        @upgrade-settlement="upgradeSettlement"
      />
    </div>
    <div v-if="!isSpacer && RoadTargets !== null && RoadTargets.length > 0">
      <Road
        v-for="(target, i) in RoadTargets"
        v-bind:key="i"
        :road="target"
        :isTarget="true"
        @build-road="build(target)"
      />
    </div>
    <div v-if="!isSpacer && roads !== null && roads.length > 0">
      <Road
        v-for="(road, i) in roads"
        v-bind:key="i"
        :road="road.coordinate"
        :isTarget="false"
        :playerId="road.playerId"
      />
    </div>

    <transition name="fade">
      <Robber v-if="!isSpacer && isRobber" />
    </transition>
  </li>
</template>
<style lang="scss">
$hex-size: 75px;

/*Hex Style */
.hex {
  position: relative;
  margin: 25px auto;
  width: $hex-size;
  height: ($hex-size * 1.7);
  border-radius: 5px;
  background: rgb(204, 204, 204);
  transform: rotate(-90deg);
  display: inline-block;
  margin-right: 60px;
  transition: all 150ms ease-in-out;
}

.glow {
  animation: glow 3s 1;
}

.hex:before,
.hex:after {
  position: absolute;
  width: inherit;
  height: inherit;
  border-radius: inherit;
  background: inherit;
  content: "";
}
.hex:hover {
  cursor: pointer;
}

.hex:before {
  transform: rotate(60deg);
}
.hex:after {
  transform: rotate(-60deg);
}
.spacer {
  opacity: 0;
}
.water {
  background-color: $water;
}
.mountain {
  background-color: $mountain;
}

.field {
  background-color: $field;
}
.forest {
  background-color: $forest;
}
.hills {
  background-color: $hills;
}
.desert {
  background-color: $desert;
}
.plains {
  background-color: $plains;
}

/*ICONS */
.icon {
  transform: rotate(90deg);
  width: 70px;
  height: 70px;
  position: absolute;
  z-index: 150;
  top: 10px;
  left: 25px;
}

.ore {
  background: url("../../assets/tiles/erts.svg") no-repeat center center;
}
.grain {
  background: url("../../assets/tiles/Graan.svg") no-repeat center center;
}
.lumber {
  width: 55px;
  height: 55px;
  background: url("../../assets/tiles/Hout.svg") no-repeat center center;
}
.wool {
  width: 75px;
  height: 75px;
  background: url("../../assets/tiles/Schaap.svg") no-repeat center center;
}
.brick {
  background: url("../../assets/tiles/Steen.svg") no-repeat center center;
}
.wave {
  background: url("../../assets/tiles/wave.svg") no-repeat center center;
  width: 80px;
  height: 80px;
  top: 25px;
  left: 0px;
}

//Resource Animation
@keyframes glow {
  0% {
    box-shadow: none;
  }
  50% {
    box-shadow: 0 0 50px #fff, /* outer white */ -10px 0 80px #f0f,
      /* outer left magenta */ 10px 0 80px #0ff; /* outer right cyan */
  }
  100% {
    box-shadow: none;
  }
}

//Robber Transition
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s ease-out;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>

<script>
import TileNumber from "./TileNumber";
import Harbour from "./Harbour.vue";
import Robber from "./Robber";
import Settlement from "./Settlement";
import GameService from "../../services/game.service";
import Road from "./Road.vue";
export default {
  name: "Tile",
  components: {
    TileNumber,
    Harbour,
    Settlement,
    Road,
    Robber,
  },
  created() {
    this.getHarbour();
  },
  props: {
    isSpacer: Boolean,
    tile: Object,
  },
  data() {
    return {
      iconClass: "",
      textColor: "",
      harbour: Object,
    };
  },
  computed: {
    isRobber() {
      return this.$store.getters.isRobberTile(
        this.tile.coordinate.x,
        this.tile.coordinate.y,
        this.tile.coordinate.z
      );
    },
    RoadTargets() {
      if (this.tile != undefined && this.tile != null && this.tile) {
        var targets = this.$store.getters.getRoadTargetsForTile(
          this.tile.coordinate.x,
          this.tile.coordinate.y,
          this.tile.coordinate.z
        );

        if (targets !== null && targets !== undefined) {
          return targets;
        }
      }
      return null;
    },
    SettlementTargets() {
      if (this.tile != undefined && this.tile != null && this.tile) {
        var targets = this.$store.getters.getSettlementTargetsForTile(
          this.tile.coordinate.x,
          this.tile.coordinate.y,
          this.tile.coordinate.z
        );

        if (targets !== null && targets !== undefined) {
          return targets;
        }
      }
      return null;
    },
    settlements() {
      if (this.tile != undefined && this.tile != null && this.tile) {
        var settlements = this.$store.getters.getSettlementsForTile(
          this.tile.coordinate.x,
          this.tile.coordinate.y,
          this.tile.coordinate.z
        );

        if (settlements !== null && settlements !== undefined) {
          return settlements;
        }
      }
      return null;
    },
    roads() {
      if (this.tile != undefined && this.tile != null && this.tile) {
        var roads = this.$store.getters.getRoadsForTile(
          this.tile.coordinate.x,
          this.tile.coordinate.y,
          this.tile.coordinate.z
        );

        if (roads !== null && roads !== undefined) {
          return roads;
        }
      }
      return null;
    },
  },
  methods: {
    getBackground() {
      if (this.isSpacer) {
        this.iconClass = "icon";
        return "spacer";
      } else if (this.tile.tileType.toLowerCase() == "water") {
        this.iconClass = "icon wave";
        return "water";
      } else if (this.tile.tileType.toLowerCase() == "mountains") {
        this.iconClass = "icon ore";
        this.textColor = "#ededeb";
        return "mountain";
      } else if (this.tile.tileType.toLowerCase() == "field") {
        this.textColor = "#595245";
        this.iconClass = "icon grain";
        return "field";
      } else if (this.tile.tileType.toLowerCase() == "forest") {
        this.textColor = "#d6c36d";
        this.iconClass = "icon lumber";
        return "forest";
      } else if (this.tile.tileType.toLowerCase() == "hills") {
        this.textColor = "#fcb93d";
        this.iconClass = "icon brick";
        return "hills";
      } else if (this.tile.tileType.toLowerCase() == "desert") {
        this.iconClass = "icon";
        return "desert";
      } else if (this.tile.tileType.toLowerCase() == "plains") {
        this.textColor = "#5e5c58";
        this.iconClass = "icon wool";
        return "plains";
      } else {
        this.iconClass = "icon";
      }
    },
    getClasses() {
      var numbers = new Array(2, 7, 12, 13, 25);
      if (this.tile !== undefined && numbers.includes(this.tile.index)) {
        return "glow " + this.getBackground();
      } else {
        return this.getBackground();
      }
    },
    getHarbour() {
      if (!this.isSpacer) {
        this.harbour = this.$store.getters.getHarbourForTile(
          this.tile.coordinate.x,
          this.tile.coordinate.y,
          this.tile.coordinate.z
        );
      }
    },
    build(coordinate) {
      GameService.build(coordinate);
    },
    upgradeSettlement(coordinate) {
      GameService.upgradeSettlement(coordinate);
    },
  },
};
</script>