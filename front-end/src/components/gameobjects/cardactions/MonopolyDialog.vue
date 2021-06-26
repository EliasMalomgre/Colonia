<template>
<q-dialog ref="dialog" @hide="onDialogHide" persistent>
    <q-card class="q-dialog-plugin">
      <q-card-section>
        <div class="text-h6">Monopoly Card</div>
      </q-card-section>
      <q-separator />

      <q-card-section vertical>
        <div>Choose the resource you want to steal from all your opponents</div>
        <br>

        <q-card-section horizontal>
            <div class="column">
         
      <q-radio keep-color v-model="resource" val="WOOL" label="Wool" color="light-green" />
      <q-radio keep-color v-model="resource" val="LUMBER" label="Lumber" color="green-9" />
      <q-radio keep-color v-model="resource" val="BRICK" label="Brick" color="red-9" />
      <q-radio keep-color v-model="resource" val="GRAIN" label="Grain" color="yellow-8" />
      <q-radio keep-color v-model="resource" val="ORE" label="Ore" color="grey-14" />
    </div>
        </q-card-section>
      </q-card-section>

      <q-card-actions align="right" class="bg-white text-teal">
        <q-btn flat label="close" @click="onCloseClick()" />
        <q-btn flat label="choose resource" :disable="resource==null"  @click="onChooseResourceClick(resource)" />
      </q-card-actions>
    </q-card>

</q-dialog>
</template>
<style lang="scss" scoped>

</style>
<script>
export default {
    name: "MonopolyDialog",
    data () {
        return{
            resource: null
        }
    },
    methods: {
    show () {
      this.$refs.dialog.show()
    },

    hide () {
      this.$refs.dialog.hide()
    },

    onDialogHide () {
      this.$emit('hide')
    },

    onChooseResourceClick (resource) {

      this.$emit('ok', resource)

      this.hide()
    },

    onCloseClick () {
      this.hide()
    }
  }
    
}
</script>