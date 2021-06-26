<template>
  <div>
    <div class="row">
    <HomeActionsDrawer />
    <InvitationsOverview class="q-ma-lg" style="width: 25rem" />
    <FriendList class="q-ma-lg" style="width: 25rem" />
    <FriendRequestsOverview class="q-ma-lg" style="width: 25rem" />
    <AddFriend class="q-ma-lg" style="width: 25rem" />
    
    </div>
    <GameOverview />
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
import { mapActions } from "vuex";
import HomeActionsDrawer from "../components/homecomponents/HomeActionsDrawer";
import InvitationsOverview from "../components/homecomponents/InvitationsOverview";
import FriendRequestsOverview from "../components/homecomponents/FriendsRequestsOverview"
import FriendList from "../components/homecomponents/FriendList"
import AddFriend from "../components/homecomponents/AddFriend"
import { getLoggedInUser } from "../services/player-methods"
import GameOverview from "../components/homecomponents/GameOverview"

const io = require("socket.io-client");
const socket = io("http://localhost:8082/menu", {
  reconnectionDelayMax: 10000,
});


export default {
  name: "Home",
  components: {
    HomeActionsDrawer,
    InvitationsOverview,
    FriendRequestsOverview,
    FriendList,
    AddFriend,
    GameOverview,
  },
  async created() {
    await this.getLoggedInUserData()
    await this.getInvitations()
    await this.getFriendRequests()

    socket.connect();
    const userId = await getLoggedInUser().id;
    socket.emit("create", userId);

    socket.on('friendRequestInvite', () =>{
         this.getInvitations()
         this.getFriendRequests()
    });
  },
  beforeDestroy(){
    socket.disconnect();
  },
  methods: {
    ...mapActions([
      "getLoggedInUserData",
      "getInvitations",
      "getFriendRequests",
    ]),
  },
};
</script>