<template>
  <div class="column items-center">
    <h5 class="text-h4">Please activate your account</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm"  @submit="activate()">
          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="lg"
            class="full-width"
            label="Activate"
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
  name: "ConfirmAccount",
  props:{
     token:{
        type:String,
        required:true 
     }
  },
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
    activate() {
      console.log("Token being sent"+this.token)
      this.$store.dispatch("auth/activateAccount", {'token': this.token}).then(
        () => {
          this.$router.push("/login");
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
          "Oh no! Your activation token is invalid or the service was not available.",
        multiLine: true
      });
    },
  },
};
</script>
