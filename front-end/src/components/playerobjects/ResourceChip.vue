<template>
<q-chip>
        <q-avatar size="lg" :color="getColor()" >
            <object  :class="resourceType.toLowerCase()" >  </object>
        <q-tooltip  content-style="font-size: 16px" :offset="[10, 10]">
         {{ resourceType.toLowerCase() }}
        </q-tooltip>
        </q-avatar>
        2
      </q-chip>    
</template>
<style scoped lang="scss">
.wool{
    width: 40px;
    height: 30px;
    background: url('../../assets/tiles/Schaap.svg') no-repeat center center;
}
.ore{
    width: 35px;
    height: 30px;
background: url('../../assets/tiles/erts.svg') no-repeat center center;
}
.grain{
    width: 35px;
    height: 35px;
  background: url('../../assets/tiles/Graan.svg') no-repeat center center;
}
.lumber{
    width: 40px;
    height: 30px;
  background: url('../../assets/tiles/Hout.svg') no-repeat center center;
}
.brick{
    width: 35px;
    height: 30px;
  background: url('../../assets/tiles/Steen.svg') no-repeat center center;
}


</style>
<script>
export default {
    name: "ResourceChip",
    props:{
        resourceType: String
    },
    methods:{
        getColor(){
            if(this.resourceType.toLowerCase()==="wool"){
                return 'green-3';
            }
            if(this.resourceType.toLowerCase()==="ore"){
                return 'grey-10';
            }
            if(this.resourceType.toLowerCase()==="grain"){
                return 'amber-4';
            }
            if(this.resourceType.toLowerCase()==="lumber"){
                return 'light-green-10';
            }
            if(this.resourceType.toLowerCase()==="brick"){
                return 'red-10';
            }

        }
    }
    
}
</script>