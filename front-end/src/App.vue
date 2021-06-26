<template>
  <q-layout view="hHh Lpr lFf">
    <q-header elevated class="glossy">
      <q-toolbar>
        

<q-space />
        <q-toolbar-title>
          Colonia
        </q-toolbar-title>
        <q-space />
        

      </q-toolbar>
    </q-header>

     <q-page-container>

      <router-view></router-view>

    </q-page-container>



  </q-layout>
</template>
<style>

</style>

<script>

export default {
  name: 'LayoutDefault',

  
  data () {
    return {
      leftDrawerOpen: false
    }
  }
}
</script>


