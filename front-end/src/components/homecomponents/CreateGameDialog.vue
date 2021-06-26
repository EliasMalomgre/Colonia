<template>
  <q-dialog ref="dialog" @hide="onDialogHide">
    <q-stepper
      v-model="step"
      ref="stepper"
      color="accent"
      vertical
      class="q-dialog-plugin"
      animated
    >
      <q-step
        :name="1"
        title="Amount of opponents"
        icon="settings"
        color="secondary"
        :done="step > 1"
      >
        How many opponents do you want to play against? (AI opponents included)
        <br />

        <q-radio
          keep-color
          v-model="amountOfOpponents"
          val="1"
          label="1"
          color="light-green"
        />
        <q-radio
          keep-color
          v-model="amountOfOpponents"
          val="2"
          label="2"
          color="green-9"
        />
        <q-radio
          keep-color
          v-model="amountOfOpponents"
          val="3"
          label="3"
          color="red-9"
        />

        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            color="primary"
            label="Continue"
            :disable="amountOfOpponents == 0"
          />
        </q-stepper-navigation>
      </q-step>

      <q-step
        :done="step > 2"
        :name="2"
        color="secondary"
        title="Select first opponent"
        icon="person_add"
      >
        <SelectOpponent
          :inviteOption="method1"
          :friend="friend1"
          :email="email1"
          :friendsList="getAvailableFriends()"
          @invite-method="setInviteMethod"
          @invite-friend="setFriend"
          @invite-email="setEmail"
        />

        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            :disable="friend1 == null && email1 == null && method1 != 'AI'"
            color="primary"
            label="Continue"
          />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>
      <q-step
        :done="step > 3"
        :name="3"
        color="secondary"
        :disable="amountOfOpponents < 2"
        title="Select second opponent"
        icon="person_add"
      >
        <SelectOpponent
          :inviteOption="method2"
          :friend="friend2"
          :email="email2"
          :friendsList="getAvailableFriends()"
          @invite-method="setInviteMethod"
          @invite-friend="setFriend"
          @invite-email="setEmail"
        />

        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            :disable="friend2 == null && email2 == null && method2 != 'AI'"
            color="primary"
            label="Continue"
          />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>
      <q-step
        :done="step > 4"
        :name="4"
        color="secondary"
        :disable="amountOfOpponents < 3"
        title="Select third opponent"
        icon="person_add"
      >
        <SelectOpponent
          :inviteOption="method3"
          :friend="friend3"
          :email="email3"
          :friendsList="getAvailableFriends()"
          @invite-method="setInviteMethod"
          @invite-friend="setFriend"
          @invite-email="setEmail"
        />

        <q-stepper-navigation>
          <q-btn
            @click="$refs.stepper.next()"
            :disable="friend3 == null && email3 == null && method3 != 'AI'"
            color="primary"
            label="Continue"
          />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>
      <q-step
        :name="5"
        title="Create Game"
        icon="videogame_asset"
        color="secondary"
      >
        <p>Opponents overview:</p>
        <p>{{ getOpponent(1) }}</p>
        <p v-if="amountOfOpponents > 1">{{ getOpponent(2) }}</p>
        <p v-if="amountOfOpponents > 2">{{ getOpponent(3) }}</p>

        <q-stepper-navigation>
          <q-btn @click="createLobby" color="primary" label="Create" />
          <q-btn
            flat
            color="primary"
            @click="$refs.stepper.previous()"
            label="Back"
            class="q-ml-sm"
          />
        </q-stepper-navigation>
      </q-step>
    </q-stepper>
  </q-dialog>
</template>
<style lang="scss" scoped>
</style>
<script>
import SelectOpponent from "./SelectOpponent";
import UserService from "../../services/user.service";

export default {
  name: "CreateGameDialog",
  components: { SelectOpponent },
  data() {
    return {
      step: 1,
      amountOfOpponents: 0,
      method1: null,
      friend1: null,
      email1: null,
      method2: null,
      friend2: null,
      email2: null,
      method3: null,
      friend3: null,
      email3: null,
      possibleFriends: null,
    };
  },
  created() {
    this.possibleFriends = this.$store.getters.getLoggedInUserFriends;
  },
  methods: {
    show() {
      this.$refs.dialog.show();
    },
    hide() {
      this.$refs.dialog.hide();
    },

    onDialogHide() {
      this.$emit("hide");
    },

    setInviteMethod(method) {
      if (this.step == 2) {
        this.method1 = method;
      }
      if (this.step == 3) {
        this.method2 = method;
      }
      if (this.step == 4) {
        this.method3 = method;
      }
    },
    setFriend(username) {
      if (this.step == 2) {
        this.friend1 = username;
      }
      if (this.step == 3) {
        this.friend2 = username;
      }
      if (this.step == 4) {
        this.friend3 = username;
      }
    },
    getOpponent(number) {
      if (number == 1) {
        if (this.method1 == "AI") return "AI";
        if (this.method1 == "friend") return this.friend1;
        if (this.method1 == "email") return this.email1;
      }

      if (number == 2) {
        if (this.method2 == "AI") return "AI";
        if (this.method2 == "friend") return this.friend2;
        if (this.method2 == "email") return this.email2;
      }

      if (number == 3) {
        if (this.method3 == "AI") return "AI";
        if (this.method3 == "friend") return this.friend3;
        if (this.method3 == "email") return this.email3;
      }
    },
    setEmail(email) {
      if (this.step == 2) {
        this.email1 = email;
      }
      if (this.step == 3) {
        this.email2 = email;
      }
      if (this.step == 4) {
        this.email3 = email;
      }
    },
    getAvailableFriends() {
      var filteredFriends = this.possibleFriends;
      if (this.method1 == "friend") {
        filteredFriends = filteredFriends.filter((e) => {
          return e !== this.friend1;
        });
      }
      if (this.method2 == "friend") {
        filteredFriends = filteredFriends.filter((e) => {
          return e !== this.friend2;
        });
      }
      if (this.method3 == "friend") {
        filteredFriends = filteredFriends.filter((e) => {
          return e !== this.friend3;
        });
      }
      return filteredFriends;
    },

    createLobby() {
      //create Lobby
      var lobbyObj = {
        1: { inviteMethod: this.method1, credential: (this.method1=="friend")? this.friend1 : this.email1 },
        2:{ inviteMethod: this.method2, credential: (this.method2=="friend")? this.friend2 : this.email2},
        3:{inviteMethod: this.method3, credential: (this.method3=="friend")? this.friend3 : this.email3}
      }

      UserService.createLobby(lobbyObj).then((lobby) => {
        this.$store.dispatch('setLobby', lobby)
        this.$router.push({ name: "Lobby", params: { id: lobby.id } });

        //close dialog
        this.$emit("ok");
        this.hide();
      });
    },
  },
};
</script>