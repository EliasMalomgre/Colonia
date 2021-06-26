<template>
  <div>
    <q-list bordered>
      <q-item-label header text-bold class="bg-secondary text-white"
        >FRIENDS</q-item-label>
      <q-item v-for="(username, index) in getFriends()" v-bind:key="index">
        <q-item-section wrap> {{ username }} </q-item-section>
      </q-item>
      <q-item v-if="getFriends().length==0">
        <img src="@/assets/sad_bunny.png">
        <div class="q-mt-lg">You don't seem to have any friends yet.
          <br><br>
          Ask someone you know for their username and send them a request!
        </div>

      </q-item>
    </q-list>
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
export default {
  name: "FriendRequestsOverview",
  methods: {
    getFriends() {
        var user = this.$store.getters.getLoggedInUser
        if(user == null){
          return []
        }else return user.friends;

        
    }
  },
};
</script>