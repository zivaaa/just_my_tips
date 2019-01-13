<template>
  <div id="app">

    <v-header></v-header>
    <router-view/>
    <v-footer v-bind:status="status"></v-footer>

    <modal v-bind:disposable="false" v-bind:show="needBlockScreen">
      <template slot="header">
        <p>Loading...</p>
      </template>
      <template>
        <div class="spinner-wrap">
          <div class="cheap-spinner">
            <p>?</p>
          </div>
        </div>

      </template>
    </modal>

  </div>
</template>

<script>
  import VHeader from "./containers/VHeader";
  import VFooter from "./components/VFooter";
  import STORE_CONST from "./const/store"
  import Modal from "./components/modals/Modal";

  export default {
    name: 'App',
    components: {Modal, VFooter, VHeader},
    mounted() {
      this.$store.state.connector.init("http://127.0.0.1:3000", this.$store);
    },
    computed : {
      status() {
        switch (this.$store.state.status) {
          case STORE_CONST.STATUS_NORMAL:
            return "idle";
          case STORE_CONST.STATUS_LOADING:
            return "loading";
          default:
            return "unknown";
        }
      },
      needBlockScreen() {
        return this.$store.state.status == STORE_CONST.STATUS_LOADING
      }
    }
  }
</script>

<style lang="less">
  @import "../node_modules/normalize.css/normalize.css";
  @import "./styles/Main";

  @keyframes spin {
    0% {
      transform: rotateZ(0deg);
    }

    100% {
      transform: rotateZ(360deg);
    }
  }

  .spinner-wrap {
    text-align: center;
  }

  .cheap-spinner {
    animation: spin 2s infinite;
    border-radius: 50%;
    margin: 15px auto;
    border: 2px solid @ColorAccent;
    color: @ColorAccent;
    background: @ColorPrimary;
    padding: 10px;
    display: inline-block;
    font-size: 3rem;
    width: 75px;
    height: 75px;
    position: relative;

    p {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0;
      text-shadow: 1px 1px 2px @ColorAccent;
    }
  }
</style>
