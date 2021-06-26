<template>
  <div class="col q-ma-sm q-mb-lg">
    <q-btn-toggle
      v-model="inviteOption"
      class="my-custom-toggle"
      no-caps
      rounded
      unelevated
      toggle-color="primary"
      color="white"
      text-color="primary"
      :options="[
        { label: 'AI', value: 'AI' },
        { label: 'Friend', value: 'friend' },
        { label: 'Email', value: 'email' },
      ]"
      @input="$emit('invite-method', inviteOption)"
    />

    <q-select
      v-if="friendsList.length > 0"
      :disable="inviteOption != 'friend'"
      v-model="friend"
      :options="friendsList"
      label="Friend"
    />
    <q-select
      v-if="friendsList.length == 0"
      disable
      v-model="friend"
      :options="friendsList"
      label="No friends to select "
    >
      <template v-slot:append>
        <q-icon name="sentiment_very_dissatisfied" />
      </template>
    </q-select>

    <q-input
      :disable="inviteOption != 'email'"
      v-model="email"
      label="Email invite"
    />
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
export default {
  name: "SelectOpponent",
  props: {
    friendsList: Array,
    email: String,
    friend: String,
    inviteOption: String,
  },
  watch: {
    email: function (val) {
      this.$emit("invite-email", val);
    },
    friend: function (val) {
      this.$emit("invite-friend", val);
    },
  },
};
</script>