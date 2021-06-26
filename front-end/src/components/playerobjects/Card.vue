<template>
  <li
    class="card"
    :style="{ 'background-color': getBackground() }"
    @click="details"
  >
    <YearOfPlentyDialog />
    <MonopolyDialog />
  </li>
</template>
<style lang="scss" scoped>
.card {
  display: block;
  height: 30px;
  width: 20px;
  border: 1px solid black;
  border-radius: 5px;
  float: left;
  margin-left: -15px;
}

.card:hover {
  cursor: pointer;
}
</style>
<script>
import CardService from "../../services/card.service";
import YearOfPlentyDialog from "../gameobjects/cardactions/YearOfPlentyDialog";
import MonopolyDialog from "../gameobjects/cardactions/MonopolyDialog";

export default {
  name: "Card",
  components: {
    YearOfPlentyDialog,
    MonopolyDialog,
  },
  props: {
    card: Object,
  },
  data() {
    return {
      isBuilding: false,
    };
  },
  created() {
    this.unwatch = this.$store.watch(
      (state, getters) => getters.getShowRoadTargets,
      (newValue, oldValue) => {
        if (newValue == false && oldValue == true && this.isBuilding) {
          this.$store.dispatch("updateShowRoadTargets", true);
          this.isBuilding = false;
        }
      }
    );
  },
  methods: {
    getBackground() {
      if (this.card.cardType=="KNIGHT"){
        return "rgb(179, 32, 32)"
      }
      if (this.card.cardType=="VICTORY_POINT"){
        return "rgb(37,110,53)"
      }
      if (this.card.cardType=="MONOPOLY"){
        return "rgb(65,59,59)"
      }
      if (this.card.cardType=="ROAD_BUILDING"){
        return "rgb(83,152,192)"
      }
      if (this.card.cardType=="YEAR_OF_PLENTY"){
        return "rgb(216, 197, 27)"
      }
      return "rgb(200, 192, 168)";
    },
    details() {
      this.$q
        .dialog({
          title: this.getTitle(),
          message: this.getMessage(),
          ok: {
          label: "Use Card"
        },
        cancel: {
          label: "Go Back"
        },
        })
        .onOk(() => {
          // send use card to backend
          CardService.playCard(this.card.cardType).then( result => {
            if(result){
              this.processCard(this.card.cardType)
            }
          }
            
          )
        });
    },
    getMessage() {
      if (this.card.cardType == "ROAD_BUILDING") {
        return "After playing this card, you have to build two roads.";
      }
      if (this.card.cardType == "YEAR_OF_PLENTY") {
        return "You get to draw two resource cards of your choice from the bank";
      }
      if (this.card.cardType == "MONOPOLY") {
        return "After playing this card, you get to can claim all resource cards of a type.";
      }
      if (this.card.cardType == "VICTORY_POINT") {
        return "Playing this card gains you one additional victory point";
      }
      if (this.card.cardType == "KNIGHT") {
        return "This card let's you move the robber";
      }
    },
    getTitle() {
      if (this.card.cardType == "ROAD_BUILDING") {
        return "Road Building card";
      }
      if (this.card.cardType == "YEAR_OF_PLENTY") {
        return "Year of Plenty card";
      }
      if (this.card.cardType == "MONOPOLY") {
        return "Monopoly card";
      }
      if (this.card.cardType == "VICTORY_POINT") {
        return "Victory Point card";
      }
      if (this.card.cardType == "KNIGHT") {
        return "Knight card";
      }
    },
    processCard(card) {
      if (card == "YEAR_OF_PLENTY") {
        this.$q
          .dialog({
            component: YearOfPlentyDialog,
            parent: this,
          })
          .onOk((obj) => {
            CardService.playYearOfPlenty(obj.res1, obj.res2);
          });
      }
      if (card == "MONOPOLY") {
        this.$q
          .dialog({
            component: MonopolyDialog,
            parent: this,
          })
          .onOk((resource) => {
            CardService.playMonopoly(resource);
          });
      }
      if (card == "KNIGHT") {
        this.$store.dispatch("updateMoveRobberAllowed", true);
        this.$q.dialog({
          title: "You can now move the robber!",
          message: "Please click on the tile you want to move the robber to.",
        });
      }

      if (card == "ROAD_BUILDING") {
        this.isBuilding = true;
        this.$store.dispatch("updateShowRoadTargets", true);
        this.$q.dialog({
          title: "You may now build two roads!",
          message:
            "Roads can be build by clicking on the designated targets. Look out! Once a road is build, it cannot be undone",
        });
      }
    },
  },
  beforeDestroy() {
    this.unwatch();
  },
};
</script>