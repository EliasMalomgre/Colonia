<template>
  <div class="column items-center">
    <h5 class="text-h4">Login</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm"  @submit="login()">
          <q-input
            filled
            clearable
            clear-icon="close"
            v-model="username"
            :rules="[(val) => !!val || 'Field is required']"
            label="Username"
          />
          <q-input
            filled
            clearable
            clear-icon="close"
            v-model="password"
            :rules="[(val) => !!val || 'Field is required']"
            type="password"
            label="Password"
          />

          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="lg"
            class="full-width"
            label="Login"
            type="submit"
           
          />
        </q-card-actions>
        </q-form>

        <q-card-section class="text-center q-pa-none text-grey-6">
          <router-link to="/register" class="text-grey-6"
            >No account yet?</router-link>
        </q-card-section>
        <q-card-section class="text-center q-pa-none text-grey-6">
          <router-link to="/requestPasswordReset" class="text-grey-6"
            >Forgot your password?</router-link>
        </q-card-section>
      </q-card-section>
    </q-card>
  </div>
</template>

<script>
export default {
  name: "Login",
  data: () => ({
    username: "",
    password: "",
  }),
  computed: {
    loggedIn() {
      return this.$store.state.auth.status.loggedIn;
    },
  },
  created() {
    if (this.loggedIn) {
      this.$router.push("/");
    }
  },
  methods: {
    login() {
      const user = { username: this.username, password: this.password };
      this.$store.dispatch("auth/login", user).then(
        () => {
          this.$router.push("/");
        },
        (error) => {
          this.handleError(error);
        }
      );
    },
    handleError() {
      this.$q.notify({
        color: "negative",
        message:
          "Oopsie... We don't seem to know this account. You can create an account by clicking the button!",
        multiLine: true,
        actions: [
          {
            label: "Create Account",
            color: "yellow",
            handler: () => {
              this.$router.push("/register");
            },
          },
          { label: "Dismiss", color: "white" },
        ],
      });
    },
  },
};
</script>
