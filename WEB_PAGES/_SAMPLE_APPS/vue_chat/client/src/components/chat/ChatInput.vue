<template>
  <div class="chat_input form">
    <div class="form-control">
      <textarea placeholder="Enter something... (Ctrl + Enter)" ref="input" v-on:onSend="onSendMessage" @keyup.ctrl.enter="onSendMessage"></textarea>
    </div>
    <div class="form-control">
      <button @click="onSendMessage">Send</button>
    </div>

  </div>
</template>

<script>
  export default {
    name: "ChatInput",
    props: {

    },
    data() {
      return {

      }
    },
    methods: {
      onSendMessage() {
        if (!this.$refs.input.value.trim()) {
          return;
        }

        this.$emit("onSend", this.$refs.input.value.trim());
        this.$refs.input.value = "";
      }
    }
  }
</script>

<style lang="less">
  @import "ChatInput";

</style>
