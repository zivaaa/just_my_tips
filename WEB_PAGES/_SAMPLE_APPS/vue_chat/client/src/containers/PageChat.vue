<template>
  <div class="content chat">
    <div class="chat_wrapper">
      <div class="chat_list_wrapper" ref="chat_list_wrapper">
        <div ref="chat_list_content" class="chat_list_content" id="test">
          <message-list v-bind:me="$store.state.user" v-bind:messages="$store.state.messages" v-on:onMessagesChanged="onMessagesChanged"
          ></message-list>
        </div>
      </div>
      <chat-input v-on:onSend="onSendMessage"></chat-input>
    </div>
  </div>
</template>

<script>
  import MessageList from "../components/chat/MessageList";
  import ChatInput from "../components/chat/ChatInput";
  import STORE_CONST from "../const/store"

  export default {
    name: "PageChat",
    components: {ChatInput, MessageList},
    methods: {
      onSendMessage(message) {
        this.$store.dispatch(STORE_CONST.ACTION_SEND_MESSAGE, message)
      },
      onMessagesChanged(messages) {
        const h = this.$refs.chat_list_wrapper.scrollHeight - this.$refs.chat_list_wrapper.clientHeight;
        this.$refs.chat_list_wrapper.scrollTop = h;
      }
    }
  }
</script>

<style lang="less">
  @import "PageChat";
</style>
