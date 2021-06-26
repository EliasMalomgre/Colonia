<template>
  <div>
    <q-list bordered>
      <q-item-label header text-bold class="bg-secondary text-white"
        >Send a friend request</q-item-label>
        <q-item class="column">
          <q-form  @submit="sendFriendRequest()">
        <q-input
        class="q-ma-sm"
            v-model="username"
            type="text"
            label="Username of friend"/>
          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="md"
            label="Send friend request"
            @click="sendFriendRequest(username)"
          />
        </q-card-actions>
        </q-form>
        </q-item>
        
        </q-list >
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
import UserService from '../../services/user.service'
export default {
  name: "AddFriend",
  data(){
      return{
          username:""
      }
  },
  methods: {
    async sendFriendRequest(username){
        return UserService.sendFriendRequest(username)
        .then(()=>{
          this.$q.notify("Sent friend request.");
          this.username="";
          })
    }
  },
};
</script>