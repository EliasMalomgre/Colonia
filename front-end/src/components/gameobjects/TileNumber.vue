<template>
<div class='number' :style="textColorObject">{{ number }}</div>
    
</template>
<style lang="scss">
$number-size: 45px;
.number {
  position: absolute;
  font-size: 25px;
  line-height: ($number-size - 5);
  text-align: center;
  z-index: 200;
  height: $number-size;
  width: $number-size;
  border-radius: $number-size;
  border: 2px solid;
  top: 60px;
  left: -10px;
  transform: rotate(90deg);
}


</style>
<script>
export default {
    name:"TileNumber",
    props:{
        textColor: String,
        number: Number
    },
    data () {
      return{
        textColorObject:{
            border: '2px solid ' + this.textColor,
            color: this.textColor
        }
      }
    }
    
}
</script>