<template>
  <div class="auth">
    <div>
      <p>Logged as : {{ userName }}</p>
    </div>
    <button class="btn_logout" v-on:click="onLogout">Logout</button>
  </div>
</template>

<script>
  import STORE_CONST from "../../const/store"

  export default {
    name: "HeaderAuth",
    computed: {
      userName() {
        return this.$store.state.user ? this.$store.state.user.username : "O_o?"
      }
    },
    methods: {
      onLogout() {
        this.$store.dispatch(STORE_CONST.ACTION_LOGOUT)
      }
    }
  }
</script>

<style lang="less">
  @import "HeaderAuth";
</style>
