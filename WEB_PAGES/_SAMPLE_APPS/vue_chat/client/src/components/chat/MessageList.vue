<template>
  <div class="message_list">
    <message v-for="(value, key, index) in messages" v-bind:message="value" v-bind:my="isMyMessage(value)" :key="index"></message>
  </div>
</template>

<script>
  import Message from "./Message";

  export default {
    components: {Message},
    props: {
      me: Object,
      messages: Array
    },
    name: "MessageList",
    data() {
      return {}
    },
    watch: {
      messages: function(newValue) {
        this.$nextTick(function() {
          this.$emit('onMessagesChanged', newValue)
        })
      }
    },
    methods: {
      isMyMessage(message) {
        if (!this.me) {
          return false;
        }

        if (this.me.id == message.user.id) {
          return true
        }
        return false;
      }
    }
  }
</script>

<style lang="less">
@import "MessageList";
</style>
