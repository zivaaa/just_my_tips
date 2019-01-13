<template>
  <div class="non_auth">
    <button class="btn_login" v-on:click="onLogin">Login</button>
    <modal v-bind:disposable=true ref="loginModal">
      <template slot="header">
        <p>Login form</p>
      </template>

      <template>
        <login-form v-on:onSubmit="onSubmit"></login-form>
      </template>

    </modal>
  </div>
</template>

<script>
  import Modal from "../modals/Modal";
  import LoginForm from "../forms/LoginForm";
  import STORE_CONST from "../../const/store"

  export default {
    name: "HeaderNonAuth",
    components: {LoginForm, Modal},
    methods: {
      onLogin() {
        this.$refs.loginModal.showModal()
      },
      onSubmit(data) {
        console.log("login fired");
        this.$store.dispatch(STORE_CONST.ACTION_LOGIN, data)
      }
    }
  }
</script>

<style lang="less">
  @import "HeaderNonAuth";
</style>
