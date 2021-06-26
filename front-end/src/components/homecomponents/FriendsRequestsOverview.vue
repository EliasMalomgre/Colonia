<template>
  <div>
    <q-list  bordered>
      <q-item-label header text-bold class="bg-secondary text-white"
        >FRIEND REQUESTS</q-item-label>
        <div v-if="requestsWithNames  && requestsWithNames.length != 0">
      <q-item v-for="request in this.requestsWithNames" v-bind:key="request.id">
        <q-item-section wrap> {{ request.asking }} sent you a friend request! </q-item-section>
        <q-item-section side>
          <q-btn
            flat
            round
            color="green"
            @click="accept(request.id)"
            icon="check_circle_outline"
          />
        </q-item-section>
        <q-item-section side>
          <q-btn flat round color="red" @click="decline(request.id)" icon="highlight_off" />
        </q-item-section>
      </q-item>
      </div>
      <q-item v-if=" requestsWithNames.length==0">
        <img src="@/assets/sad_bunny.png">
        <div class="q-mt-lg">You don't seem to have any friend requests right now 
          <br><br>
          Try sending someone a friend request.
        </div>

      </q-item>
    </q-list>
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
import UserService from "../../services/user.service";

export default {
  name: "FriendRequestsOverview",
  data(){
    return{
      requestsWithNames: []
    }
  },
 async created(){
     await this.getRequestsWithName();
  },
  methods: {
    accept(id) {
      UserService.acceptFriendRequest(id)
      .then(() => {
        this.$q.notify("Accepted friend request.")
      })  
    },
    getRequestingName(id){
      return UserService.getUsername(id)
        .then(result=>{
          return result;
        })
    },
    decline(id) {
      UserService.declineFriendRequest(id)
    },
    async getRequestsWithName(){
      let requests=await this.getRequests();
      Object.values(requests).forEach( request=>
                {this.getRequestingName(request.askingUserId)
                .then(result=>{
                  request.asking=result;
                  this.requestsWithNames.push(request);
                })});
    },
    async getRequests() {
      return await UserService.getFriendRequests()
      .then(result=>{
        return result;})
    }
  },
};
</script>