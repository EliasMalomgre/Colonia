<template>
  <div class="column items-center">
    <h5 class="text-h4">Request Password Reset</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm"  @submit="request()">
          <q-input
            filled
            clearable
            clear-icon="close"
            v-model="email"
            :rules="[(val) => !!val || 'Field is required']"
            type="email"
            label="Your account's email"
          />

          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="lg"
            class="full-width"
            label="Request password reset"
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
  name: "RequestPasswordReset",
  data: () => ({
    email: "",
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
    request() {

      this.$store.dispatch("auth/requestPasswordReset", this.email).then(
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
          "Oopsie... This request does not seem to be valid try asking for a new reset email.",
        multiLine: true
      });
    },
  },
};
</script>
