<template>
<div id="dicecontainer">
    <q-img
        class="q-ma-md"
          :src="getDiceSrc(firstDice)"
          :ratio="1"
          style="height: 100px; width: 100px;"
        >
    </q-img>
    <q-img
          :src="getDiceSrc(secondDice)"
          :ratio="1"
          style="height: 100px; width: 100px;"
    >
    </q-img>

    </div>
</template>
<style lang="scss">
#dicecontainer{
    z-index: 100000;
    justify-content: center;
    position: absolute;
    top:40%;
    left: 22%;

}
</style>
<script>
export default {
    name: "Dice",
    props: {
        firstDice: Number,
        secondDice: Number

    },
    data () {
        return {
            dice1: require("../../assets/dice/dice_1.png"),
            dice2: require("../../assets/dice/dice_2.png"),
            dice3: require("../../assets/dice/dice_3.png"),
            dice4: require("../../assets/dice/dice_4.png"),
            dice5: require("../../assets/dice/dice_5.png"),
            dice6: require("../../assets/dice/dice_6.png")
        }
    },
    methods: {
        getDiceSrc(diceNr){
            if(diceNr==1){
                return this.dice1
            } else   if(diceNr==2){
                return this.dice2
            } else   if(diceNr==3){
                return this.dice3
            } else   if(diceNr==4){
                return this.dice4
            } else   if(diceNr==5){
                return this.dice5
            } else   if(diceNr==6){
                return this.dice6
            }  
        }
    }
    
}
</script>