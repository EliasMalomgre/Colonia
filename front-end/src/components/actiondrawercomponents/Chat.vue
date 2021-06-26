<template>
<q-dialog v-model="dialog">
  <q-card  style="width: 700px; max-width: 80vw;">
    <q-card-section class="row">
      <div class="text-h6">Game Chat</div>
      <q-space />
          <q-btn icon="close" flat round dense v-close-popup @click="$emit('close-chat')"/>
    </q-card-section>
    <q-separator />
    <q-scroll-area id="chatWindow" ref="chatScroll" v-if="chat != undefined && chat.amountOfMessages>0">
      <div v-for="message in chat.chatMessages" :key="message.chatNumber">
        <q-chat-message
          v-if="message.playerId != currentUserId"
          avatar="https://cdn.quasar.dev/img/boy-avatar.png"
          size="5"
          :name="message.playerName"
          :text="[message.message]"
          :stamp="formatDate(message.timeSent)"
          bg-color="primary"
          class="chatMessage"
        />
        <q-chat-message
          v-else
          avatar="https://cdn.quasar.dev/img/boy-avatar.png"
          size="5"
          :name="message.playerName"
          :text="[message.message]"
          :stamp="formatDate(message.timeSent)"
          sent
          bg-color="accent"
          class="chatMessage"
        />
      </div>
    </q-scroll-area>
    <q-card-section v-if="chat == undefined || chat.amountOfMessages==0">
      
      <img src='@/assets/no_messages.png'>
      <div class="text-center text-h5">Nothing to show ...</div>
      <div class="text-center text-h6">Get the conversation going by sending a message!</div>
      </q-card-section>
    <q-card-section id="typeBar" class="row">
      <q-input
        outlined
        v-model="newMessage"
        label="Your message"
        class="col-10"
      />
      <q-btn
        id="chatbtn"
        round
        color="primary"
        icon="send"
        class="col q-ml-md q-mr-sm"
        @click="sendMessage(newMessage)"
      />
    </q-card-section>
  </q-card>
  </q-dialog>
</template>

<style scoped>
#chatbtn {
  max-width: 4em;
}

.chatMessage {
  margin-top: 5%;
  margin-left: 5%;
  margin-right: 5%;
}

#chatWindow {
  height: 30em;

}

#typeBar {
  height: 10%;
}
</style>

<script>
import {getLoggedInPlayer} from "../../services/player-methods"
import ChatService from "../../services/chat-service.js";
const io = require("socket.io-client");
const socket = io("http://localhost:8082/chat", {
  reconnectionDelayMax: 10000,
});

export default {
  name: "Chat",
  props: {
    dialog: Boolean,
    gameId: String
  },
  data() {
    return {
      chat: {},
      newMessage: "",
      
    };
  },
  computed:{
    currentUserId () {
      return getLoggedInPlayer().userId
    }

  },
  methods: {
    sendMessage(message) {
      if (message != "") {
        if (
          ChatService.sendMessage(message).then(function () {
            socket.emit("message", message);
          })
        ) {
          this.newMessage = "";
        } else {
          this.$q.notify({
        type: 'negative',
        message: `Message could not be send. Please check your internet connection.`
      })
        }
      }
    },
    async reloadData() {
      this.chat = await ChatService.getChatByGameId(this.gameId);
    },
    formatDate(date) {
      var formattedDate = new Date(date);
      return formattedDate.getHours() + ":" + formattedDate.getMinutes();
    }
  },
  async created() {
    await this.reloadData();
    socket.connect();
    socket.emit("create", this.gameId);

    socket.on("reloadData", async () => {
      this.$emit("new-message")
      await this.reloadData();
    });
  },
};
</script>