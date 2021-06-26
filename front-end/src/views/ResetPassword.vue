<template>
  <div class="column items-center">
    <h5 class="text-h4">Reset Password</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm"  @submit="reset()">
          <q-input
            filled
            clearable
            clear-icon="close"
            v-model="password"
            :rules="[(val) => !!val || 'Field is required']"
            type="password"
            label="New Password"
          />

          <q-card-actions class="q-px-sm">
          <q-btn
            color="primary"
            size="lg"
            class="full-width"
            label="Reset password"
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
  name: "ResetPassword",
  props:{
     token:{
        type:String,
        required:true 
     }
  },
  data: () => ({
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
    reset() {
    
      const passwordReset={
          password:this.password,
          resetToken:this.token
          }  
      this.$store.dispatch("auth/resetPassword", passwordReset).then(
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
