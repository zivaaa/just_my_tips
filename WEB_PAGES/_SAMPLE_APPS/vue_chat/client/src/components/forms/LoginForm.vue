<template>
  <div class="form">
    <div class="form-control">
      <input ref="usernameInput" type="text" placeholder="Username" @keyup.enter="submit">
    </div>
    <div class="form-control">
      <button @click="submit">Submit</button>
    </div>

  </div>
</template>

<script>
  export default {
    name: "LoginForm",
    methods: {
      submit() {
        console.log("submit");
        if (!this.$refs.usernameInput.value) {
          return
        }
        this.$emit("onSubmit", {
          username: this.$refs.usernameInput.value
        })
      }
    }
  }
</script>

<style lang="less">

</style>
