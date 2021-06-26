<template>
<ul class="card-stack">
    <Card v-for="card in cards" v-bind:key="card"
    :card="card"/>


</ul>
</template>
<style lang="scss" scoped>
.card-stack{
    float: left;
  margin: 0px;
  padding: 0px;
  margin-right: 30px;
}
</style>
<script>
import Card from './Card.vue'
export default {
  
    name:"CardStack",
    components: { Card },
    props:{
        cards: Array
    }
    

    
}
</script>