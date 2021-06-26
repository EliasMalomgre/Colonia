<template>
<div id="playerscontainer" class="q-ma-md row items-center justify-center">
    <Player
    v-for="player in getPlayers()"
    v-bind:key="player.playerId"
    :player="player"/>


</div>
</template>
<style scoped lang="scss">
#playerscontainer{
    min-width: 15em;
}


</style>
<script>
import Player from './playerobjects/Player'
export default {
    name:"PlayersColumn",
    components:{
        Player
    },
    methods:{
        getPlayers(){
            return this.$store.getters.getPlayers
        }
    }
    
}
</script>