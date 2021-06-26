<template>
  <q-drawer
    v-model="drawer"
    show-if-above
    :mini="!drawer || miniState"
    @click.capture="drawerClick"
    :width="200"
    :breakpoint="500"
    bordered
    content-class="bg-grey-3"
  >
    <q-scroll-area class="fit">
      <q-list padding>
        <q-item clickable v-ripple @click="openCreateDialog()">
          <q-item-section avatar>
            <q-icon name="create" />
          </q-item-section>

          <q-item-section> New Game </q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="myAccount()">
          <q-item-section avatar>
            <q-icon name="account_circle" />
          </q-item-section>

          <q-item-section > My Account </q-item-section>
        </q-item>

        <q-item clickable v-ripple  @click="logout()">
          <q-item-section avatar>
            <q-icon name="exit_to_app" />
          </q-item-section>

          <q-item-section> Logout </q-item-section>
        </q-item>
      </q-list>
    </q-scroll-area>

    <div class="q-mini-drawer-hide absolute" style="top: 15px; right: -17px">
      <q-btn
        dense
        round
        unelevated
        color="accent"
        icon="chevron_left"
        @click="miniState = true"
      />
    </div>
  </q-drawer>
</template>
<style lang="scss" scoped>
</style>
<script>
import CreateGameDialogVue from './CreateGameDialog.vue';
export default {
  name: "HomeActionsDrawer",
  data() {
    return {
      drawer: false,
      miniState: false,
    };
  },

  methods: {
    drawerClick(e) {
      // if in "mini" state and user
      // click on drawer, we switch it to "normal" mode
      if (this.miniState) {
        this.miniState = false;

        // notice we have registered an event with capture flag;
        // we need to stop further propagation as this click is
        // intended for switching drawer to "normal" mode only
        e.stopPropagation();
      }
    },
    openCreateDialog() {
      this.$q.dialog({
        component: CreateGameDialogVue,
        parent: this,
      });
    },
    logout(){
      this.$store.dispatch("auth/logout");
      this.$router.push({name:"Login"})
    },
    myAccount(){
      this.$router.push({name:"MyAccount"})
    }
  },
};
</script>