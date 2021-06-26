<template>
  <div class="column items-center">
    <h5 class="text-h4">My account</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm"  @submit="updateUser()">
        <q-input
            :disable="true"
            v-model="username"
            type="text"
            label="My username"
        />
        <q-input
            :disable="true"
            v-model="email"
            type="email"
            label="My email"
        />
        <q-input
            filled
            clearable
            clear-icon="close"
            v-model="password"
            type="password"
            label="Update password"
        />
          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="lg"
            class="full-width"
            label="Save user settings"
            type="submit"
          />
        </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </div>
</template>

<script>
export default {
  name: "MyAccount",
  data: () => ({
    password: "",
    username: "",
    email:""
  }),
  computed: {
    loggedIn() {
      return this.$store.state.auth.status.loggedIn;
    },
  },
  created() {
      const userdata = JSON.parse(localStorage.getItem('user'));
      this.username=userdata.username;
      this.email=userdata.email;
  },
  methods: {
    updateUser() {
      let changeRequest={
          newPassword:this.password
      }
      this.$store.dispatch("auth/updateAccount", changeRequest).then(
        () => {

        },
        () => {
          this.handleError();
        }
      );
    },
    handleError() {
      this.$q.notify({
        color: "negative",
        message:
          "Oopsie... Something went wrong updating your user data",
        multiLine: true
      });
    },
  },
};
</script>
