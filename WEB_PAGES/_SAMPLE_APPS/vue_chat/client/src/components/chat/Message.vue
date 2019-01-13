<template>
  <div v-bind:class="{
    chat_message: true,
    my: my
  }">
    <div class="message_wrapper">
      <div class="author">{{ username }}:</div>
      <div class="text">
        <p>{{ message.message }}</p>
        <p class="time">{{messageAt}}</p>
      </div>
    </div>
  </div>
</template>

<script>
  import moment from "moment"

  export default {
    name: "Message",
    props: {
      my: {
        type: Boolean,
        required: true
      },
      message: {
        type: Object,
        required: true
      }
    },
    computed: {
      username() {
        return this.message.user ? this.message.user.username : "?"
      },
      messageAt() {
        return moment(this.message.date).format("DD.MM.YY HH:mm:ss");
      }
    }
  }
</script>

<style lang="less">
@import "Message";
</style>
