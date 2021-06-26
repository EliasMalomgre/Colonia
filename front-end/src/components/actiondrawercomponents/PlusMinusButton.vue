<template>
    <div>
         <q-btn flat round :disable="this.isMax" color="primary" icon="add" @click="$emit('add')"/>
          <q-btn flat round :disable="this.isMin" color="primary" icon="remove" @click="$emit('remove')"/>
          <div :style="{visibility: isMax ? 'visible' : 'hidden'}" class="text-caption text-red">Max available!</div>
    </div>
</template>
<style lang="scss" scoped>

</style>
<script>
export default {
    name: "PlusMinusButton",
    props:{
        isMin: Boolean,
        isMax: Boolean
    }
    
}
</script>