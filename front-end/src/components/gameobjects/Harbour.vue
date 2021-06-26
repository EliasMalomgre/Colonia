<template>
<div class="harbour">
    <div :class="getClasses()">
        <q-tooltip  content-style="font-size: 16px" :offset="[10, 10]">
          Resource: {{ getResourceText() }} <br>
          Ratio: 1-{{ harbour.ratio }}
        </q-tooltip>
    </div>
</div>

    
</template>
<style lang="scss">

.harbour {
  position: absolute;
  left:45px;
  top: 0px;
  text-align: center;
  transform: rotate(90deg);
  z-index: 600;
}

.harbour::before, .harbour::after { 
  font-size: 20px;
  line-height: 25px;
  height:25px;
  width: 125px;
}

.harbour::before {
  top:-15px;
  position: absolute;
}
.harbour::after { 
  top: 10px;
  position: absolute;
}

.harbour-piece {
  border-radius: 7px;
  background-color: #ebebeb;
  position: absolute;
  width: 18px;
  height: 75px;
  z-index: 150;
}

.harbour-piece.tl {
  top: -74px;
  left: 30px;
  transform: rotate(60deg);
}

.non-water-tl{
    top: -105px  ;
  left: 12px ;
  transform: rotate(60deg);
}

.harbour-piece.l {
  top: -29px;
  left: 5px;
  transform: rotate(180deg);
}

.non-water-l{
top: -29px;
  left: -30px;
  transform: rotate(180deg);
}
.harbour-piece.tr {
  top: -73px;
  left: 80px;
  transform: rotate(300deg);
}

.non-water-tr{
    top: -105px;
  left: 98px;
  transform: rotate(300deg);
}



</style>
<script>
export default {
    name: "Harbour",
    props:{
        harbour: Object,
        tiletype: String
    },
    methods:{
        getClasses(){
            if(this.harbour!==undefined){
            var classes = "harbour-piece "
            if(this.tiletype.toLowerCase()=="water"){
                if(this.harbour.coordinate.cardDir=="WEST"){
                    classes += "l "
                }
                if(this.harbour.coordinate.cardDir=="NORTH_WEST"){
                    classes += "tl "
                }
                if(this.harbour.coordinate.cardDir=="NORTH_EAST"){
                    classes += "tr "
                }
                
            }else{
                if(this.harbour.coordinate.cardDir=="WEST"){
                    classes += "non-water-l "
                }
                if(this.harbour.coordinate.cardDir=="NORTH_WEST"){
                    classes += "non-water-tl "
                }
                if(this.harbour.coordinate.cardDir=="NORTH_EAST"){
                    classes += "non-water-tr "
                }
            }

            if(this.harbour.resource=="LUMBER"){
                classes += "forest "

            }
            if(this.harbour.resource=="WOOL"){
                classes += "plains "
            }
            if(this.harbour.resource=="ORE"){
                classes += "mountain "
            }
            if(this.harbour.resource=="BRICK"){
                classes += "hills "
            }
            if(this.harbour.resource=="GRAIN"){
                classes += "field "
            }

            return classes;
            }

        },
        getResourceText(){
            if(this.harbour.resource=="NOTHING"){
                return "Any"
            }
            else return this.harbour.resource.toLowerCase()
        }
    }

    
}
</script>