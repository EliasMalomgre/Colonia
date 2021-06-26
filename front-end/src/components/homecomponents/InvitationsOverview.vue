<template>
  <div>
    <q-list bordered>
      <q-item-label header text-bold class="bg-secondary text-white"
        >INVITATIONS</q-item-label
      >
      <div
        v-if="invites  && invites.length != 0"
      >
        <q-item v-for="invite in invites" v-bind:key="invite.invitation.id">
          <q-item-section wrap>
            {{ invite.hostname }} invited you to play!
          </q-item-section>
          <q-item-section side>
            <q-btn
              flat
              round
              color="green"
              @click="accept(invite.invitation.id)"
              icon="check_circle_outline"
            />
          </q-item-section>
          <q-item-section side>
            <q-btn
              flat
              round
              color="red"
              @click="decline(invite.invitation.id)"
              icon="highlight_off"
            />
          </q-item-section>
        </q-item>
      </div>
      <q-item v-if="invites == null ||invites.length == 0">
        <img src="@/assets/sad_bunny.png" />
        <div class="q-mt-lg">
          You don't seem to have any invitations right now <br /><br />
          You can create a new game, and invite your friends to join you!
        </div>
      </q-item>
    </q-list>
  </div>
</template>
<style lang="scss" scoped>
</style>
<script>
import UserService from "../../services/user.service";

export default {
  name: "InvitationsOverview",
  computed: {
    invites() {
      return this.$store.getters.getInvitations;
    },
  },
  methods: {
    accept(id) {
      UserService.acceptInvitation(id).then((result) => {
        this.$router.push({ name: "Lobby", params: { id: result.id } });
      });
    },
    decline(id) {
      this.$store.dispatch("deleteInvitation", id);
    },
  },
};
</script>