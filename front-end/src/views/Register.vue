<template>
  <div class="column items-center">
    <h5 class="text-h4">Register</h5>
    <q-card bordered class="q-pa-lg shadow-1" style="min-width: 30%">
      <q-card-section>
        <q-form class="q-gutter-sm" @submit.prevent="handleRegister">
          <q-input
            v-model="user.username"
            type="text"
            label="Username"
            :rules="[
              (val) => !!val || 'Field is required',
              (val) =>
                val.length > 6 || 'Username must be at least 7 characters long',
            ]"
            filled
            clearable
            clear-icon="close"
          />
          <q-input
            v-model="user.email"
            type="email"
            label="E-mail address"
            :rules="[(val) => !!val || 'Field is required', isValidEmail]"
            filled
            clearable
            clear-icon="close"
          />

          <q-input
            v-model="user.password"
            :type="isPwd ? 'password' : 'text'"
            label="Password"
            :rules="[
              (val) => !!val || 'Field is required',
              (val) =>
                val.length > 6 || 'Password must be at least 7 characters long',
              (val) => /\d/.test(val) || 'Please include at least one number',
              (val) =>
                /[A-Z]/.test(val) ||
                'Please include at least one uppercase character',
              (val) =>
                /[a-z]/.test(val) ||
                'Please include at least one lowercase character',
            ]"
            filled
            clearable
            clear-icon="close"
            lazy-rules
          >
            <template v-slot:append>
              <q-icon
                :name="isPwd ? 'visibility_off' : 'visibility'"
                class="cursor-pointer"
                @click="isPwd = !isPwd"
              />
            </template>
          </q-input>

          <q-card-actions class="q-px-md">
            <q-btn
              type="submit"
              color="primary"
              size="lg"
              class="full-width"
              label="Create Account"
            />
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </div>
</template>

<script>
import User from "../models/user";

export default {
  name: "Register",
  props:{
    invitation:{
      required:false,
      type: String,
    }
  },
  data() {
    return {
      user: new User("", "", ""),
      submitted: false,
      isPwd: true,
    };
  },
  computed: {
    loggedIn() {
      return this.$store.state.auth.status.loggedIn;
    },
  },
  mounted() {
    if (this.loggedIn) {
      this.$router.push("/");
    }
  },
  methods: {
    handleRegister() {
      this.message = "";
      this.submitted = true;
      if(this.invitation==null||this.invitation==undefined){
      this.$store.dispatch("auth/register", this.user).then(
        (data) => {
          this.$q.notify({
            color: "positive",
            message: data.message
          });
          this.successful = true;
        },
        (error) => {
          this.$q.notify({
            color: "negative",
            message: (error.response && error.response.data) ||
            error.message ||
            error.toString()
          });
          this.successful = false;
        }
      );
      }
      //IF invited
      else{
      const registrationInvite={user: this.user,invite:this.invitation}
      this.$store.dispatch("auth/registerByInvite", registrationInvite).then(
        (data) => {
          this.$q.notify({
            color: "positive",
            message: data.message
          });
          this.successful = true;
        },
        (error) => {
          this.$q.notify({
            color: "negative",
            message: (error.response && error.response.data) ||
            error.message ||
            error.toString()
          });
          this.successful = false;
        }
      );
      }
    },
    isValidEmail(val) {
      const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
      return emailPattern.test(val) || "Invalid email";
    },
  },
};
</script>

